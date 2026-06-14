package develop.x.core.dispatcher;

import develop.x.core.dispatcher.argumentresolver.XArgumentProvider;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.dispatcher.handler.XHandlerManager;
import develop.x.core.executor.BusinessXExecutor;
import develop.x.io.XRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * VirtualThreadDispatcher 의 Semaphore(100) 백프레셔/누수 동작을 검증한다.
 *
 * <p>핵심: 생산 코드 doRun 은 {@code semaphore.acquire()} 후 executor 에 비동기 제출하고,
 * release 는 워커 스레드 VirtualThreadRunnable.run() 의 finally 에서 일어난다.
 * 따라서 누수/백프레셔는 "동시에 여러 작업이 permit 을 점유한 채 release 가 늦어질 때"만 드러난다.
 * 동기 executor 로는 acquire→run→release 가 직렬로 완결되어 동시성이 만들어지지 않으므로,
 * 본 테스트는 워커가 실제 별도 스레드에서 latch 로 대기(=permit 점유 지속)하도록 만들어
 * backpressure(101번째 acquire 블로킹)와 정상 release 후 permit 복구를 결정적으로 검증한다.</p>
 */
class VirtualThreadDispatcherTest {

    /**
     * 제출된 작업을 매번 새 데몬 스레드에서 비동기로 실행하는 executor 더블.
     * 생산 BusinessXExecutor 의 고정 100 스레드풀과 달리, permit 점유가 동시에 누적되는
     * 상황을 결정적으로 재현하기 위해 무제한 스레드로 실행한다.
     */
    static class AsyncBusinessXExecutor extends BusinessXExecutor {
        private final ExecutorService delegate =
                Executors.newCachedThreadPool(r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                });

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }

        void shutdownDelegate() {
            delegate.shutdownNow();
        }
    }

    /** 핸들러 메서드의 호출 횟수를 세고, gate latch 로 실행을 의도적으로 지연시킨다. */
    public static class TargetBean {
        final AtomicInteger okCalls = new AtomicInteger();
        final AtomicInteger entered = new AtomicInteger();
        /** 핸들러가 permit 을 점유한 채 머무르도록 잡아두는 게이트. null 이면 즉시 반환. */
        volatile CountDownLatch gate;

        public void ok(Object arg) {
            okCalls.incrementAndGet();
            entered.incrementAndGet();
            CountDownLatch g = gate;
            if (g != null) {
                try {
                    g.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private AsyncBusinessXExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownDelegate();
            executor.shutdown();
        }
    }

    private static byte[] requestBytesWithUrl(String url) {
        return new XRequest.Builder()
                .header("url", url)
                .body(new byte[]{1})
                .build()
                .toByte();
    }

    private static Semaphore semaphoreOf(VirtualThreadDispatcher dispatcher) throws Exception {
        Field f = VirtualThreadDispatcher.class.getDeclaredField("semaphore");
        f.setAccessible(true);
        return (Semaphore) f.get(dispatcher);
    }

    private static XHandler handlerFor(Object bean, String methodName) throws Exception {
        Method method = bean.getClass().getMethod(methodName, Object.class);
        XHandler handler = mock(XHandler.class);
        when(handler.bean()).thenReturn(bean);
        when(handler.method()).thenReturn(method);
        return handler;
    }

    private VirtualThreadDispatcher dispatcherFor(TargetBean bean, Object[] resolvedArgs) throws Exception {
        executor = new AsyncBusinessXExecutor();
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);
        XHandler handler = handlerFor(bean, "ok");
        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(resolvedArgs);
        return new VirtualThreadDispatcher(handlerManager, provider, executor);
    }

    @Test
    @DisplayName("doRun -> executor 에 제출된 VirtualThreadRunnable 이 handler.method().invoke 를 호출하고, provider 가 돌려준 인자를 그대로 전달한다.")
    void doRunInvokesHandlerMethodWithResolvedArgument() throws Exception {
        // given : ok(arg) 가 받은 인자를 캡처하도록 별도 빈 사용
        executor = new AsyncBusinessXExecutor();
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);

        CapturingBean bean = new CapturingBean();
        XHandler handler = handlerFor(bean, "ok");
        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(new Object[]{"payload"});

        VirtualThreadDispatcher dispatcher =
                new VirtualThreadDispatcher(handlerManager, provider, executor);

        // when
        dispatcher.invoke(requestBytesWithUrl("order.bet"));

        // then : 워커가 비동기이므로 호출 완료를 latch 로 대기
        assertThat(bean.done.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(bean.calls.get()).isEqualTo(1);
        assertThat(bean.captured).isEqualTo("payload");
    }

    public static class CapturingBean {
        final AtomicInteger calls = new AtomicInteger();
        final CountDownLatch done = new CountDownLatch(1);
        volatile Object captured = "<unset>";

        public void ok(Object arg) {
            captured = arg;
            calls.incrementAndGet();
            done.countDown();
        }
    }

    @Test
    @DisplayName("정상 실행(release) 후 Semaphore permit 이 100 으로 복구된다 — 비동기 반복에도 누수 없음.")
    void semaphorePermitsRestoredAfterSuccess() throws Exception {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            // given : gate 없음 -> 핸들러가 즉시 반환하며 release
            TargetBean bean = new TargetBean();
            VirtualThreadDispatcher dispatcher = dispatcherFor(bean, new Object[]{"payload"});
            Semaphore semaphore = semaphoreOf(dispatcher);
            assertThat(semaphore.availablePermits()).isEqualTo(100);

            // when : permit(100) 을 초과하는 횟수를 비동기 실행. 누수가 있으면 permit 고갈로 영구 블로킹.
            int total = 500;
            for (int i = 0; i < total; i++) {
                dispatcher.invoke(requestBytesWithUrl("order.bet"));
            }

            // then : 모든 작업이 끝나면 permit 은 정확히 100 으로 복구되어야 한다.
            //        availablePermits 가 100 에 도달할 때까지 대기(누수 시 도달 못 하고 타임아웃).
            long deadline = System.nanoTime() + Duration.ofSeconds(8).toNanos();
            while (semaphore.availablePermits() != 100 && System.nanoTime() < deadline) {
                Thread.sleep(5);
            }
            assertThat(semaphore.availablePermits()).isEqualTo(100);
            assertThat(bean.okCalls.get()).isEqualTo(total);
        });
    }

    @Test
    @DisplayName("backpressure: 동시에 100개가 permit 을 점유하면 101번째 invoke 의 acquire 가 블로킹되고, permit 해제 후 진행된다.")
    void backpressureBlocksWhenPermitsExhausted() throws Exception {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            // given : gate 로 핸들러를 잡아둬 permit 을 점유 상태로 유지
            TargetBean bean = new TargetBean();
            CountDownLatch gate = new CountDownLatch(1);
            bean.gate = gate;
            VirtualThreadDispatcher dispatcher = dispatcherFor(bean, new Object[]{"payload"});
            Semaphore semaphore = semaphoreOf(dispatcher);

            // when : 100개 제출 -> 모두 핸들러에 진입(gate 대기)하여 permit 0 이 된다.
            for (int i = 0; i < 100; i++) {
                dispatcher.invoke(requestBytesWithUrl("order.bet"));
            }
            // 100개가 모두 핸들러에 진입할 때까지 대기 -> permit 고갈 확인
            long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
            while (bean.entered.get() < 100 && System.nanoTime() < deadline) {
                Thread.sleep(5);
            }
            assertThat(bean.entered.get()).isEqualTo(100);
            assertThat(semaphore.availablePermits()).isEqualTo(0);

            // 101번째 invoke 는 호출 스레드에서 acquire 에 막혀야 한다.
            AtomicInteger state = new AtomicInteger(0); // 0=before, 1=returned
            Thread blocked = new Thread(() -> {
                dispatcher.invoke(requestBytesWithUrl("order.bet"));
                state.set(1);
            });
            blocked.setDaemon(true);
            blocked.start();

            // then : 잠시 기다려도 invoke 가 반환되지 않아야 한다(블로킹 증명).
            Thread.sleep(300);
            assertThat(state.get()).as("permit 고갈 상태에서 101번째 invoke 는 acquire 에 블로킹되어야 한다").isEqualTo(0);

            // gate 해제 -> 100개가 release -> 막혀있던 101번째가 진행되어 반환되어야 한다.
            gate.countDown();
            blocked.join(5000);
            assertThat(state.get()).as("permit 해제 후 막혀있던 invoke 가 진행되어야 한다").isEqualTo(1);
        });
    }

    @Test
    @DisplayName("doRun 의 acquire() 가 인터럽트되면 RuntimeException(cause=InterruptedException) 으로 래핑되어 throw 된다.")
    void acquireInterruptedIsWrappedAsRuntimeException() throws Exception {
        // doRun 은 protected 이고 본 테스트는 동일 패키지(develop.x.core.dispatcher)라 직접 호출한다.
        // invoke() 를 통하면 AbstractXDispatcher 의 try/catch(RuntimeException) 가 이 래핑 예외를
        // 삼키므로, acquire 인터럽트 래핑 경로 자체를 검증하려면 doRun 을 직접 호출해야 한다.
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            // given : permit 을 모두 소진시켜 다음 acquire 가 반드시 블로킹되게 한다.
            TargetBean bean = new TargetBean();
            CountDownLatch gate = new CountDownLatch(1);
            bean.gate = gate;
            VirtualThreadDispatcher dispatcher = dispatcherFor(bean, new Object[]{"payload"});
            Semaphore semaphore = semaphoreOf(dispatcher);

            Method method = bean.getClass().getMethod("ok", Object.class);
            XHandler handler = mock(XHandler.class);
            when(handler.bean()).thenReturn(bean);
            when(handler.method()).thenReturn(method);

            for (int i = 0; i < 100; i++) {
                dispatcher.doRun(handler, new Object[]{"payload"});
            }
            long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
            while (bean.entered.get() < 100 && System.nanoTime() < deadline) {
                Thread.sleep(5);
            }
            assertThat(semaphore.availablePermits()).isEqualTo(0);

            // when : doRun 의 블로킹된 acquire 를 인터럽트한다.
            AtomicInteger thrown = new AtomicInteger(0); // 1=RuntimeException(InterruptedException)
            Thread caller = new Thread(() -> {
                try {
                    dispatcher.doRun(handler, new Object[]{"payload"});
                } catch (RuntimeException e) {
                    thrown.set(e.getCause() instanceof InterruptedException ? 1 : 2);
                }
            });
            caller.setDaemon(true);
            caller.start();
            // acquire 진입을 잠시 기다린 뒤 인터럽트
            Thread.sleep(200);
            caller.interrupt();
            caller.join(5000);

            // then
            assertThat(thrown.get())
                    .as("acquire 인터럽트는 RuntimeException(cause=InterruptedException) 으로 래핑되어야 한다")
                    .isEqualTo(1);

            // cleanup
            gate.countDown();
        });
    }
}

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
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VirtualThreadDispatcherTest {

    /**
     * execute(r) 를 호출 스레드에서 즉시 실행하여 비동기성을 제거한 결정적 executor.
     * 부모 생성자가 실제 fixedThreadPool(100) 을 만들기 때문에 테스트 종료 시 shutdown 한다.
     */
    static class SynchronousBusinessXExecutor extends BusinessXExecutor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    /** 핸들러 메서드의 실제 호출 횟수를 세는 빈. */
    public static class TargetBean {
        final AtomicInteger okCalls = new AtomicInteger();

        public void ok(Object arg) {
            okCalls.incrementAndGet();
        }

        public void boom(Object arg) {
            throw new IllegalStateException("business boom");
        }
    }

    private SynchronousBusinessXExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
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

    @Test
    @DisplayName("doRun -> executor 에 제출된 VirtualThreadRunnable 이 handler.method().invoke 를 실제로 호출한다.")
    void doRunInvokesHandlerMethod() throws Exception {
        // given
        executor = new SynchronousBusinessXExecutor();
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);

        TargetBean bean = new TargetBean();
        XHandler handler = handlerFor(bean, "ok");

        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(new Object[]{"payload"});

        VirtualThreadDispatcher dispatcher =
                new VirtualThreadDispatcher(handlerManager, provider, executor);

        // when
        dispatcher.invoke(requestBytesWithUrl("order.bet"));

        // then
        assertThat(bean.okCalls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("정상 실행 후 Semaphore permit 이 100 으로 복구된다(누수 없음).")
    void semaphorePermitsRestoredAfterSuccess() throws Exception {
        // given
        executor = new SynchronousBusinessXExecutor();
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);

        TargetBean bean = new TargetBean();
        XHandler handler = handlerFor(bean, "ok");
        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(new Object[]{"payload"});

        VirtualThreadDispatcher dispatcher =
                new VirtualThreadDispatcher(handlerManager, provider, executor);
        Semaphore semaphore = semaphoreOf(dispatcher);
        assertThat(semaphore.availablePermits()).isEqualTo(100);

        // when : 여러 번 실행해도 누수가 없어야 한다.
        for (int i = 0; i < 250; i++) {
            dispatcher.invoke(requestBytesWithUrl("order.bet"));
        }

        // then
        assertThat(semaphore.availablePermits()).isEqualTo(100);
        assertThat(bean.okCalls.get()).isEqualTo(250);
    }

    @Test
    @DisplayName("핸들러가 예외(InvocationTargetException 유발)를 던져도 finally 의 release 로 permit 이 복구되고 invoke 는 전파하지 않는다.")
    void semaphoreReleasedEvenWhenHandlerThrows() throws Exception {
        // given
        executor = new SynchronousBusinessXExecutor();
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);

        TargetBean bean = new TargetBean();
        XHandler handler = handlerFor(bean, "boom");
        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(new Object[]{"payload"});

        VirtualThreadDispatcher dispatcher =
                new VirtualThreadDispatcher(handlerManager, provider, executor);
        Semaphore semaphore = semaphoreOf(dispatcher);

        // when : 200회 반복해도 permit 고갈이 없어야 한다(누수 시 100회 후 acquire 블로킹).
        for (int i = 0; i < 200; i++) {
            dispatcher.invoke(requestBytesWithUrl("order.bet"));
        }

        // then
        assertThat(semaphore.availablePermits()).isEqualTo(100);
    }
}

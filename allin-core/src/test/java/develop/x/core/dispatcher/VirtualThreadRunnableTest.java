package develop.x.core.dispatcher;

import develop.x.core.dispatcher.VirtualThreadDispatcher.VirtualThreadRunnable;
import develop.x.core.dispatcher.handler.XHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VirtualThreadRunnableTest {

    public static class TargetBean {
        final AtomicInteger calls = new AtomicInteger();

        public void ok(Object arg) {
            calls.incrementAndGet();
        }

        public void boom(Object arg) {
            throw new IllegalStateException("business boom");
        }
    }

    /**
     * private(비공개) 클래스의 public 메서드를 setAccessible 없이 reflective invoke 하면
     * IllegalAccessException 이 발생한다 -> VirtualThreadRunnable 의 첫 catch 경로를 검증한다.
     */
    private static class InaccessibleBean {
        public void method(Object arg) {
            // 호출되면 안 됨 (접근 단계에서 막힘)
        }
    }

    private static XHandler handlerFor(Object bean, String methodName) throws Exception {
        Method method = bean.getClass().getMethod(methodName, Object.class);
        XHandler handler = mock(XHandler.class);
        when(handler.bean()).thenReturn(bean);
        when(handler.method()).thenReturn(method);
        return handler;
    }

    @Test
    @DisplayName("정상 invoke: handler 메서드를 1회 호출하고 finally 에서 semaphore 를 1회 release 한다.")
    void normalInvokeReleasesOnce() throws Exception {
        // given
        TargetBean bean = new TargetBean();
        XHandler handler = handlerFor(bean, "ok");
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire(); // 점유 후 release 로 1 회복 확인
        assertThat(semaphore.availablePermits()).isZero();

        VirtualThreadRunnable runnable =
                new VirtualThreadRunnable(handler, new Object[]{"payload"}, semaphore);

        // when
        runnable.run();

        // then
        assertThat(bean.calls.get()).isEqualTo(1);
        assertThat(semaphore.availablePermits()).isEqualTo(1);
    }

    @Test
    @DisplayName("InvocationTargetException(핸들러 내부 예외) 발생 시에도 예외를 전파하지 않고 release 한다.")
    void invocationTargetExceptionIsSwallowedAndReleased() throws Exception {
        // given
        TargetBean bean = new TargetBean();
        XHandler handler = handlerFor(bean, "boom");
        Semaphore semaphore = new Semaphore(0);

        VirtualThreadRunnable runnable =
                new VirtualThreadRunnable(handler, new Object[]{"payload"}, semaphore);

        // when / then
        assertThatCode(runnable::run).doesNotThrowAnyException();
        assertThat(semaphore.availablePermits()).isEqualTo(1);
    }

    @Test
    @DisplayName("IllegalAccessException 발생 시에도 예외를 전파하지 않고 release 한다.")
    void illegalAccessExceptionIsSwallowedAndReleased() throws Exception {
        // given
        InaccessibleBean bean = new InaccessibleBean();
        XHandler handler = handlerFor(bean, "method");
        Semaphore semaphore = new Semaphore(0);

        VirtualThreadRunnable runnable =
                new VirtualThreadRunnable(handler, new Object[]{"payload"}, semaphore);

        // when / then : IllegalAccessException 은 내부 catch -> release
        assertThatCode(runnable::run).doesNotThrowAnyException();
        assertThat(semaphore.availablePermits()).isEqualTo(1);
    }
}

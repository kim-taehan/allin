package develop.x.core.executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractXExecutorTest {

    /** 테스트용 구체 서브클래스 (shutdown 은 단순 위임). */
    static class TestXExecutor extends AbstractXExecutor {
        TestXExecutor(ExecutorService es) {
            super(es);
        }

        @Override
        public void shutdown() {
            executorService.shutdownNow();
        }
    }

    private static ExecutorService internalExecutorService(AbstractXExecutor executor) throws Exception {
        Field f = AbstractXExecutor.class.getDeclaredField("executorService");
        f.setAccessible(true);
        return (ExecutorService) f.get(executor);
    }

    @Test
    @DisplayName("execute(Runnable) 로 제출한 작업이 실제로 실행된다.")
    void executeRunsSubmittedTask() throws Exception {
        // given
        TestXExecutor executor = new TestXExecutor(Executors.newCachedThreadPool());
        CountDownLatch latch = new CountDownLatch(1);

        // when
        executor.execute(latch::countDown);

        // then
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        executor.shutdown();
    }

    @Test
    @DisplayName("생성자는 ExecutorService 를 unconfigurableExecutorService 로 래핑한다 -> 원본과 다른 인스턴스이며 ThreadPoolExecutor 로 다운캐스트할 수 없다.")
    void constructorWrapsWithUnconfigurableExecutorService() throws Exception {
        // given
        ExecutorService original = Executors.newFixedThreadPool(2);
        TestXExecutor executor = new TestXExecutor(original);

        // when
        ExecutorService internal = internalExecutorService(executor);

        // then : 동일 인스턴스가 아니다 (불변 래퍼로 재생성).
        assertThat(internal).isNotSameAs(original);

        // then : 래퍼는 ThreadPoolExecutor 가 아니므로 다운캐스트 시 ClassCastException.
        assertThatThrownBy(() -> {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) internal;
            tpe.setCorePoolSize(1);
        }).isInstanceOf(ClassCastException.class);

        executor.shutdown();
    }
}

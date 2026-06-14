package develop.x.core.executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BusinessXExecutor 의 shutdown() 은 awaitTermination(3s/60s) 가 하드코딩되어 있어
 * 시간 기반 검증은 피하고, execute 위임과 shutdown 후 종료 상태(동작)만 가볍게 확인한다.
 */
class BusinessXExecutorTest {

    private static ExecutorService internalExecutorService(AbstractXExecutor executor) throws Exception {
        Field f = AbstractXExecutor.class.getDeclaredField("executorService");
        f.setAccessible(true);
        return (ExecutorService) f.get(executor);
    }

    @Test
    @DisplayName("execute(Runnable) 로 제출한 작업이 실제로 실행된다.")
    void executeRunsTask() throws Exception {
        BusinessXExecutor executor = new BusinessXExecutor();
        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(latch::countDown);

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        internalExecutorService(executor).shutdownNow();
    }

    @Test
    @DisplayName("작업이 없는 상태에서 shutdown() 을 호출하면 풀이 종료 상태가 된다(시간 검증 없이 동작만 확인).")
    void shutdownTerminatesPool() throws Exception {
        BusinessXExecutor executor = new BusinessXExecutor();
        ExecutorService internal = internalExecutorService(executor);

        // shutdown() 은 awaitTermination(3s) -> shutdown() -> awaitTermination(60s) 경로를 탄다.
        // 작업이 없어도 첫 awaitTermination(3s) 는 타임아웃되므로 최소 3초 소요된다.
        executor.shutdown();

        assertThat(internal.isShutdown()).isTrue();
    }
}

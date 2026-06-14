package develop.x.core.executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReceiverXExecutor / BlockingQueueXExecutor 의 빠른 shutdown 경로(즉시 shutdownNow)를 검증한다.
 * 두 클래스는 동일 구조(cachedThreadPool + 즉시 shutdownNow + awaitTermination(10s))이며,
 * 제출된 작업이 없으면 즉시 종료되어 결정적/빠르다.
 */
class ReceiverAndBlockingQueueXExecutorTest {

    private static ExecutorService internalExecutorService(AbstractXExecutor executor) throws Exception {
        Field f = AbstractXExecutor.class.getDeclaredField("executorService");
        f.setAccessible(true);
        return (ExecutorService) f.get(executor);
    }

    @Test
    @DisplayName("ReceiverXExecutor.shutdown(): 작업이 없으면 즉시 종료되어 isShutdown/isTerminated 가 true 다.")
    void receiverShutdownFastPath() throws Exception {
        // given
        ReceiverXExecutor executor = new ReceiverXExecutor();
        ExecutorService internal = internalExecutorService(executor);

        // when
        executor.shutdown();

        // then
        assertThat(internal.isShutdown()).isTrue();
        assertThat(internal.isTerminated()).isTrue();
    }

    @Test
    @DisplayName("BlockingQueueXExecutor.shutdown(): 작업이 없으면 즉시 종료되어 isShutdown/isTerminated 가 true 다.")
    void blockingQueueShutdownFastPath() throws Exception {
        // given
        BlockingQueueXExecutor executor = new BlockingQueueXExecutor();
        ExecutorService internal = internalExecutorService(executor);

        // when
        executor.shutdown();

        // then
        assertThat(internal.isShutdown()).isTrue();
        assertThat(internal.isTerminated()).isTrue();
    }

    @Test
    @DisplayName("ReceiverXExecutor.shutdown(): blocking queue take() 로 대기중인 작업은 인터럽트되어 종료된다.")
    void receiverShutdownInterruptsBlockedTask() throws Exception {
        // given
        ReceiverXExecutor executor = new ReceiverXExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);

        executor.execute(() -> {
            started.countDown();
            try {
                // hazelcast take() 처럼 무한 블로킹을 모사
                new java.util.concurrent.LinkedBlockingQueue<>().take();
            } catch (InterruptedException e) {
                interrupted.countDown();
            }
        });

        assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();

        // when : shutdownNow -> 블로킹 take() 에 InterruptedException 전달
        executor.shutdown();

        // then
        assertThat(interrupted.await(2, TimeUnit.SECONDS)).isTrue();
    }
}

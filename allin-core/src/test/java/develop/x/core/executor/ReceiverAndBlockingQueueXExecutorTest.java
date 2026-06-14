package develop.x.core.executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    @DisplayName("ReceiverXExecutor.shutdown(): awaitTermination 이 인터럽트되면 인터럽트 상태를 재설정한다.")
    void receiverShutdownReinterruptsOnInterrupt() throws Exception {
        assertShutdownReinterruptsOnInterruptedAwait(new ReceiverXExecutor());
    }

    @Test
    @DisplayName("BlockingQueueXExecutor.shutdown(): awaitTermination 이 인터럽트되면 인터럽트 상태를 재설정한다.")
    void blockingQueueShutdownReinterruptsOnInterrupt() throws Exception {
        assertShutdownReinterruptsOnInterruptedAwait(new BlockingQueueXExecutor());
    }

    /**
     * shutdown() 의 awaitTermination 이 InterruptedException 으로 빠질 때
     * catch 블록이 Thread.currentThread().interrupt() 로 인터럽트 상태를 복원하는지 검증한다.
     * <p>
     * shutdownNow 의 인터럽트를 무시하고 살아남는 작업을 제출해 풀을 깨어있게 만들고,
     * 호출 스레드를 미리 인터럽트하면 awaitTermination 이 즉시 InterruptedException 을 던져 catch 경로를 결정적으로 탄다.
     */
    private void assertShutdownReinterruptsOnInterruptedAwait(AbstractXExecutor executor) throws Exception {
        ExecutorService internal = internalExecutorService(executor);
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean keepRunning = new AtomicBoolean(true);

        executor.execute(() -> {
            started.countDown();
            while (keepRunning.get()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    // shutdownNow 의 인터럽트를 무시하고 풀을 살려둬 awaitTermination 이 실제로 블로킹되게 한다.
                }
            }
        });
        assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();

        // 호출 스레드를 미리 인터럽트 → shutdown() 내부 awaitTermination 이 InterruptedException 을 던지고 catch 진입
        Thread.currentThread().interrupt();
        try {
            executor.shutdown();

            assertThat(Thread.currentThread().isInterrupted())
                    .as("shutdown() 의 InterruptedException catch 가 인터럽트 상태를 재설정해야 한다")
                    .isTrue();
        } finally {
            Thread.interrupted();          // 후속 테스트 오염 방지: 인터럽트 상태 클리어
            keepRunning.set(false);        // 살아있는 워커 종료
            internal.awaitTermination(2, TimeUnit.SECONDS);
        }
    }
}

package develop.x.core.executor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BusinessXExecutor.shutdown() 은 표준 graceful 종료 패턴
 * (shutdown() -> awaitTermination(60s) -> 미종료 시 shutdownNow())을 따른다.
 * 시간 기반(고정 지연) 검증을 피하고, 다음을 결정적으로 검증한다.
 * - execute(Runnable) 위임으로 제출된 작업이 실제 실행된다.
 * - shutdown() 호출 시 이미 제출된 작업은 드레인(완료)된 뒤 풀이 종료(isShutdown && isTerminated)된다.
 */
class BusinessXExecutorTest {

    private BusinessXExecutor executor;

    @AfterEach
    void tearDown() throws Exception {
        // 단언 실패로 shutdown 이 호출되지 못한 경우의 스레드 누수 방지.
        if (executor != null) {
            internalExecutorService(executor).shutdownNow();
        }
    }

    private static ExecutorService internalExecutorService(AbstractXExecutor executor) throws Exception {
        Field f = AbstractXExecutor.class.getDeclaredField("executorService");
        f.setAccessible(true);
        return (ExecutorService) f.get(executor);
    }

    @Test
    @DisplayName("execute(Runnable) 로 제출한 작업이 실제로 실행된다.")
    void executeRunsTask() throws Exception {
        executor = new BusinessXExecutor();
        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(latch::countDown);

        assertThat(latch.await(2, TimeUnit.SECONDS))
                .as("제출한 작업이 실행되어 latch 가 0 이 되어야 한다")
                .isTrue();
    }

    @Test
    @DisplayName("shutdown(): 작업이 없으면 graceful 경로로 풀이 종료되어 isShutdown && isTerminated 가 true 다.")
    void shutdownTerminatesIdlePool() throws Exception {
        executor = new BusinessXExecutor();
        ExecutorService internal = internalExecutorService(executor);

        executor.shutdown();

        assertThat(internal.isShutdown()).as("shutdown() 호출 후 종료 요청 상태").isTrue();
        assertThat(internal.isTerminated())
                .as("작업이 없으므로 awaitTermination 이 즉시 만족되어 완전 종료되어야 한다")
                .isTrue();
    }

    @Test
    @DisplayName("shutdown(): 이미 제출된 작업은 드레인(완료)된 뒤 풀이 종료된다(graceful drain).")
    void shutdownDrainsInflightTaskThenTerminates() throws Exception {
        executor = new BusinessXExecutor();
        ExecutorService internal = internalExecutorService(executor);

        CountDownLatch started = new CountDownLatch(1);   // 작업이 실행을 시작했음
        CountDownLatch release = new CountDownLatch(1);   // 테스트가 작업 완료를 허용함
        AtomicBoolean completed = new AtomicBoolean(false);

        executor.execute(() -> {
            started.countDown();
            try {
                // shutdown() 이 호출될 때까지 실행 중 상태를 유지한다.
                release.await(5, TimeUnit.SECONDS);
                completed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 작업이 실행 중인 상태(in-flight)임을 보장한 뒤 shutdown 을 시작한다.
        assertThat(started.await(2, TimeUnit.SECONDS)).as("작업이 실행을 시작해야 한다").isTrue();

        // shutdown() 은 awaitTermination(60s) 동안 in-flight 작업의 완료를 기다린다(graceful).
        // 별도 스레드에서 shutdown 을 호출하고, 작업을 완료(release)시켜 드레인을 유도한다.
        Thread shutdownThread = new Thread(executor::shutdown);
        shutdownThread.start();

        // shutdown 진입 후 신규 작업은 거부되지만 in-flight 는 계속 실행되어야 한다 -> 완료 허용.
        release.countDown();

        // shutdown() 이 awaitTermination 으로 작업 완료를 기다린 뒤 반환되어야 한다.
        shutdownThread.join(10_000);

        assertThat(shutdownThread.isAlive()).as("shutdown() 이 드레인 후 반환되어야 한다").isFalse();
        assertThat(completed.get()).as("in-flight 작업이 인터럽트 없이 정상 완료되어야 한다(graceful drain)").isTrue();
        assertThat(internal.isShutdown()).isTrue();
        assertThat(internal.isTerminated()).as("드레인 후 풀이 완전 종료되어야 한다").isTrue();
    }
}

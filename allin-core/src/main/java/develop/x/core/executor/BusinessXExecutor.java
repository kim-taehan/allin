package develop.x.core.executor;

import develop.x.core.dispatcher.VirtualThreadDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BusinessXExecutor extends AbstractXExecutor {

    public BusinessXExecutor() {
        super(Executors.newFixedThreadPool(100));
    }

    @Override
    public void shutdown() {
        try {
            // 추가로 요청을 받지 않는다..
            this.executorService.shutdown();

            // 비지니스 모델은 1분동안 요청되어 있는 것들에 대한 처리를 수행한다.
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                List<Runnable> runnables = this.executorService.shutdownNow();
                log.error("비지니스 모델에서 처리하지 못한 데이터가 {} 건 존재합니다.", runnables.size());

                runnables.forEach(runnable -> {
                    if (runnable instanceof VirtualThreadDispatcher.VirtualThreadRunnable businessCallable) {
                        // 후처리 프로세스
                    }
                });

                if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("business exec가 정상 종료되지 않았습니다.");
                }
            }
            log.error("business shutdownAndAwaitTermination end");
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

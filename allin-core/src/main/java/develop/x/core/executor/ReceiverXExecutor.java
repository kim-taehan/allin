package develop.x.core.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReceiverXExecutor extends AbstractXExecutor {

    public ReceiverXExecutor() {
        super(Executors.newCachedThreadPool());
    }

    @Override
    public void shutdown() {

        log.error("receiver queue shutdown call");
        try {
            // hazelcast 는 서버 시작 시점 이후에는 추가로 요청되는 부분이 없음으로 바로 shutdownNow 호출한다.
            // hazelcast blocking queue 를 기다리고 있는 경우 InterruptedException 가 호출되고 종료되야 된다.
            List<Runnable> runnables = this.executorService.shutdownNow();
            log.error("receiver shutdownAndAwaitTermination call = {}", runnables.size());

            // 혹시 수행중인 경우 DispatcherServlet 을 호출하거나 직접 수행하는 경우도 3초가 limit
            if (!this.executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.error("receiver exec가 정상 종료되지 않았습니다.");
            }
            log.error("receiver shutdownAndAwaitTermination end");
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
    }
}

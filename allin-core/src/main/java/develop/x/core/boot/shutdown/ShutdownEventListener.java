package develop.x.core.boot.shutdown;

import develop.x.core.executor.XExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShutdownEventListener implements ApplicationListener<ContextClosedEvent> {

    private final Collection<XExecutor> xExecutors;

    public ShutdownEventListener(ApplicationContext context) {
        this.xExecutors = context.getBeansOfType(XExecutor.class).values();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("system 종료시도합니다...");
        try (ExecutorService closeExec = Executors.newCachedThreadPool()){
            for (XExecutor xExecutor : xExecutors) {
                closeExec.execute(xExecutor::shutdown);
            }

            try {
                if (!closeExec.awaitTermination(3, TimeUnit.SECONDS)) {
                    closeExec.shutdownNow();
                    if (!closeExec.awaitTermination(20, TimeUnit.SECONDS)){
                        System.err.println("Pool did not terminate");
                    }
                }
                log.info("system 이상없이 종료되었습니다.");
            } catch (InterruptedException e) {
                closeExec.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }
}

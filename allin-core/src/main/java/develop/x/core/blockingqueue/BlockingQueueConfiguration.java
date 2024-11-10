package develop.x.core.blockingqueue;

import develop.x.core.blockingqueue.runner.XBlockingQueueRunner;
import develop.x.core.executor.BlockingQueueXExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BlockingQueueConfiguration {

    @Bean
    public XBlockingQueueRunner xBlockingQueueRunner(ApplicationContext context, BlockingQueueXExecutor executor){
        return new XBlockingQueueRunner(context, executor);
    }


}

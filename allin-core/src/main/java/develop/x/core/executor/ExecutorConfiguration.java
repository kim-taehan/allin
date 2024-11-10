package develop.x.core.executor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExecutorConfiguration {

    @Bean
    public BusinessXExecutor businessXExecutor(){
        return new BusinessXExecutor();
    }

    @Bean
    public BlockingQueueXExecutor blockingQueueXExecutor(){
        return new BlockingQueueXExecutor();
    }

    @Bean
    public ReceiverXExecutor receiverXExecutor(){
        return new ReceiverXExecutor();
    }
}

package develop.x.core.receiver;

import develop.x.core.dispatcher.XDispatcher;
import develop.x.core.executor.ReceiverXExecutor;
import develop.x.core.receiver.hazelcast.HazelcastXReceiver;
import develop.x.core.receiver.hazelcast.HzReceivers;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(HzReceivers.class)
public class ReceiverConfiguration {

    private final HzReceivers hzReceivers;


    @Bean
    public HazelcastXReceiver hazelcastXReceiver(ReceiverXExecutor receiverXExecutor, XDispatcher xDispatcher) {
        return new HazelcastXReceiver(hzReceivers, receiverXExecutor, xDispatcher);
    }

}

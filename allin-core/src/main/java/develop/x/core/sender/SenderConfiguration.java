package develop.x.core.sender;

import develop.x.core.receiver.hazelcast.HzReceivers;
import develop.x.core.sender.hazelcast.HazelcastXSender;
import develop.x.core.sender.hazelcast.HzSenders;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(HzSenders.class)
@RequiredArgsConstructor
public class SenderConfiguration {

    private final HzSenders hzSenders;

    //@Bean
    //public XSender hazelcastXSender(){
    //    return new HazelcastXSender(hzSenders);
    //}
}

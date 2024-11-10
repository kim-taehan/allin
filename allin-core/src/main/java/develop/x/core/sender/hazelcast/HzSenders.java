package develop.x.core.sender.hazelcast;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "allin.hazelcast")
public record HzSenders(List<HzSender> senders) {
}

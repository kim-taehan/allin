package develop.x.core.receiver.hazelcast;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.List;

@ConfigurationProperties(prefix = "allin.hazelcast")
public record HzReceivers(List<HzReceiver> receivers) {
}

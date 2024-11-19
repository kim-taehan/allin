package develop.x.network.netty.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "allin.networks")
public record Networks (
        String host,
        int port,
        NettyInfo netty
) {
}

package develop.x.network.netty;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

    @Bean
    public EventExecutorGroup businessExecutorGroup() {
        return new DefaultEventExecutorGroup(4); // 4개의 스레드로 구성된 풀
    }
}

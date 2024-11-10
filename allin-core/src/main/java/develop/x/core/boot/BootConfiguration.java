package develop.x.core.boot;

import develop.x.core.boot.shutdown.ShutdownEventListener;
import develop.x.core.boot.warmup.impl.DatabaseWarmup;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BootConfiguration {

    @Bean
    public ShutdownEventListener shutdownEventListener(ApplicationContext context) {
        return new ShutdownEventListener(context);
    }

    @Bean
    public DatabaseWarmup databaseWarmup() {
        return new DatabaseWarmup();
    }
}

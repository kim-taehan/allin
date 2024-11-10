package develop.x.core.message;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MessageConfiguration {

    @Bean
    @ConditionalOnProperty(value = "allin.message", havingValue = "property", matchIfMissing = true)
    public XMessageFinder propertyMessageFinder(MessageSource source){
        return new PropertiesXMessageFinder(source);
    }

    @Bean
    @ConditionalOnProperty(value = "allin.message", havingValue = "database", matchIfMissing = false)
    public XMessageFinder dbMessageFinder(MessageSource source){
        return new DatabaseXMessageFinder();
    }

}

package develop.x.betting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
@Slf4j
@EnableAutoConfiguration
public class BettingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BettingApplication.class, args);
    }


}
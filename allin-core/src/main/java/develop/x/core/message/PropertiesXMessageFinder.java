package develop.x.core.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Optional;

@Slf4j
public class PropertiesXMessageFinder implements XMessageFinder {

    private final MessageSource messageSource;

    public PropertiesXMessageFinder(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Optional<String> find(String messageId, Locale locale, Object... args) {
        try {
            return Optional.of(messageSource.getMessage(messageId, args, locale));
        } catch (NoSuchMessageException noSuchMessageException) {
            log.error("properties 에 정의된 메시지를 찾지 못했습니다. {}", messageId, noSuchMessageException);
            return Optional.empty();
        }
    }
}

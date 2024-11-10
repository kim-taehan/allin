package develop.x.core.message;

import java.util.Locale;
import java.util.Optional;

public interface XMessageFinder {

    default Optional<String> find(String messageId, Object... args) {
        return find(messageId, Locale.KOREA, args);
    }

    Optional<String> find(String messageId, Locale locale, Object... args);
}

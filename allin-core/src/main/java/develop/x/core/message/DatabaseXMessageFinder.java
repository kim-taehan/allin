package develop.x.core.message;

import java.util.Locale;
import java.util.Optional;

public class DatabaseXMessageFinder implements XMessageFinder{
    @Override
    public Optional<String> find(String messageId, Locale locale, Object... args) {
        return Optional.empty();
    }
}

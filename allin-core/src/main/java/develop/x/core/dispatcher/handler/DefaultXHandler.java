package develop.x.core.dispatcher.handler;

import java.lang.reflect.Method;

public record DefaultXHandler(Object bean, Method method) implements XHandler {
}

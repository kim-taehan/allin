package develop.x.core.dispatcher.handler;

import java.lang.reflect.Method;

public record BqXHandler(Object bean, Method method, String queueName) implements XHandler{
}

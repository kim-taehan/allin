package develop.x.core.exception.advice.argumentresolver;

import develop.x.core.exception.advice.annotation.XExParam;

import java.lang.reflect.Parameter;
import java.util.Set;

public class MessageExArgumentResolver implements XExArgumentResolver {

    private final Set<String> argumentNames = Set.of("message", "msg");

    @Override
    public boolean support(Parameter parameter) {
        if (String.class.equals(parameter.getType()) && parameter.getAnnotatedType().isAnnotationPresent(XExParam.class)) {
            XExParam xExParam = parameter.getAnnotatedType().getAnnotation(XExParam.class);
            return argumentNames.contains(xExParam.value().toLowerCase());
        }
        return false;
    }

    @Override
    public Object convert(Throwable throwable, Object[] args) {
        return throwable.getMessage();
    }
}

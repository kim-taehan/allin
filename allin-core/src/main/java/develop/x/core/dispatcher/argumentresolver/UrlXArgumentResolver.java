package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
import develop.x.core.dispatcher.annotation.XParam;

import java.lang.reflect.Parameter;
import java.util.Set;

public class UrlXArgumentResolver implements XArgumentResolver {

    private final Set<String> argumentName = Set.of("url", "apiname");

    @Override
    public boolean support(Parameter parameter) {
        if (parameter.getType().equals(String.class) && parameter.getAnnotatedType().isAnnotationPresent(XParam.class)) {
            XParam xParam = parameter.getAnnotatedType().getAnnotation(XParam.class);
            return argumentName.contains(xParam.value());
        }
        return false;
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return request.getHeaders().get("url");
    }
}

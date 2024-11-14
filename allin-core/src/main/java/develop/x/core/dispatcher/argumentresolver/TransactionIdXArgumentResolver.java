package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.annotation.XParam;

import java.lang.reflect.Parameter;
import java.util.Set;

public class TransactionIdXArgumentResolver implements XArgumentResolver {

    private final Set<String> argumentName = Set.of("transactionid", "transid");

    @Override
    public boolean support(Parameter parameter) {
        if (parameter.getType().equals(String.class) && parameter.getAnnotatedType().isAnnotationPresent(XParam.class)) {
            XParam xParam = parameter.getAnnotatedType().getAnnotation(XParam.class);
            return argumentName.contains(xParam.value().toLowerCase());
        }
        return false;
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return request.getHeaders().get("transactionId");
    }
}

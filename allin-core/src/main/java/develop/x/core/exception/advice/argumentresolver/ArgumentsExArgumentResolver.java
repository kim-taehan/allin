package develop.x.core.exception.advice.argumentresolver;

import java.lang.reflect.Parameter;
import java.util.Set;

public class ArgumentsExArgumentResolver implements XExArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return Object[].class.isAssignableFrom(parameter.getType());
    }

    @Override
    public Object convert(Throwable throwable, Object[] args) {
        return args;
    }
}

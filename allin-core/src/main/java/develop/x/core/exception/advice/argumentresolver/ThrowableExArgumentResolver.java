package develop.x.core.exception.advice.argumentresolver;

import java.lang.reflect.Parameter;

public class ThrowableExArgumentResolver implements XExArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return Throwable.class.isAssignableFrom(parameter.getType());
    }

    @Override
    public Object convert(Throwable throwable, Object[] args) {
        return throwable;
    }
}

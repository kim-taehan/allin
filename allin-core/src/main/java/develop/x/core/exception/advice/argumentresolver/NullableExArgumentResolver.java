package develop.x.core.exception.advice.argumentresolver;

import java.lang.reflect.Parameter;

public class NullableExArgumentResolver implements XExArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return false;
    }

    @Override
    public Object convert(Throwable throwable, Object[] args) {
        return null;
    }
}

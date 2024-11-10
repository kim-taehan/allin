package develop.x.core.exception.advice.argumentresolver;

import java.lang.reflect.Parameter;

public interface XExArgumentResolver {

    boolean support(Parameter parameter);

    Object convert(Throwable throwable, Object[] args);
}

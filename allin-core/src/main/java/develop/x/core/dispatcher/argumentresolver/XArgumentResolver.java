package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;

import java.lang.reflect.Parameter;

public interface XArgumentResolver {
    boolean support(Parameter parameter);

    Object convert(Parameter parameter, XRequest request);
}

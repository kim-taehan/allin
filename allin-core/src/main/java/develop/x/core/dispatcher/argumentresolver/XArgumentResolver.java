package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;

import java.lang.reflect.Parameter;

public interface XArgumentResolver {
    boolean support(Parameter parameter);

    Object convert(Parameter parameter, XRequest request);
}

package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;

import java.lang.reflect.Parameter;

public class NullableXArgumentResolver implements XArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return false;
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return null;
    }

}

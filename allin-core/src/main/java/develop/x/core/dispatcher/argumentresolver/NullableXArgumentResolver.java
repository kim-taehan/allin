package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.XRequestOld;

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

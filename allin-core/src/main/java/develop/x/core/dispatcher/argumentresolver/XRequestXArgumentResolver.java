package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.annotation.XParam;

import java.lang.reflect.Parameter;
import java.util.Set;

public class XRequestXArgumentResolver implements XArgumentResolver {


    @Override
    public boolean support(Parameter parameter) {

        return parameter.getType().equals(XRequest.class);
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return request;
    }
}

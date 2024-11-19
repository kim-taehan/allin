package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;

import java.lang.reflect.Parameter;

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

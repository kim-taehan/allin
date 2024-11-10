package develop.x.core.dispatcher.argumentresolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.XRequestOld;
import develop.x.core.dispatcher.annotation.XModel;

import java.io.IOException;
import java.lang.reflect.Parameter;

public class ModelXArgumentResolver implements XArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return parameter.getAnnotatedType().isAnnotationPresent(XModel.class);
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(request.getBody(), parameter.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

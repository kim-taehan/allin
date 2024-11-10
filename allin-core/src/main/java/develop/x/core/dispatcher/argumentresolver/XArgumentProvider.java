package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.handler.XHandler;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XArgumentProvider {

    private final List<XArgumentResolver> resolvers;
    private final XArgumentResolver defaultResolver = new NullableXArgumentResolver();

    public XArgumentProvider(ApplicationContext context) {
        Map<String, XArgumentResolver> beansOfType = context.getBeansOfType(XArgumentResolver.class);
        resolvers = beansOfType.values().stream().toList();
    }

    public Object[] convertArguments(XHandler handler, XRequest request) {
        Parameter[] parameters = handler.method().getParameters();
        List<Object> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            XArgumentResolver resolver = findArgumentResolver(parameter);
            result.add(resolver.convert(parameter, request));
        }
        return result.toArray();
    }

    private XArgumentResolver findArgumentResolver(Parameter parameter) {
        for (XArgumentResolver resolver : resolvers) {
            if(resolver.support(parameter)){
                return resolver;
            }
        }
        return defaultResolver;
    }
}

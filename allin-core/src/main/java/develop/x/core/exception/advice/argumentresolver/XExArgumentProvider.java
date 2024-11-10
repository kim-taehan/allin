package develop.x.core.exception.advice.argumentresolver;

import develop.x.core.exception.advice.ExceptionHandler;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class XExArgumentProvider {

    private final List<XExArgumentResolver> resolvers;
    private final XExArgumentResolver defaultResolver = new NullableExArgumentResolver();

    public XExArgumentProvider(ApplicationContext context) {
        resolvers = context.getBeansOfType(XExArgumentResolver.class)
                .values().stream()
                .toList();
    }

    private XExArgumentResolver findArgumentResolver(Parameter parameter) {
        for (XExArgumentResolver resolver : resolvers) {
            if(resolver.support(parameter)){
                return resolver;
            }
        }
        return defaultResolver;
    }

    public Object[] convertArguments(ExceptionHandler exceptionHandler, Throwable throwable, Object[] args) {
        Parameter[] parameters = exceptionHandler.method().getParameters();
        List<Object> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            XExArgumentResolver resolver = findArgumentResolver(parameter);
            result.add(resolver.convert(throwable, args));
        }
        return result.toArray();
    }
}

package develop.x.core.dispatcher.handler;

import develop.x.core.dispatcher.annotation.XController;
import develop.x.core.dispatcher.annotation.XMapping;
import develop.x.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class XHandlerManager {

    private final Map<String, XHandler> handlers;

    public XHandlerManager(ApplicationContext context) {
        this.handlers = initializeHandlers(context);
    }

    private Map<String, XHandler> initializeHandlers(ApplicationContext context) {
        HashMap<String, XHandler> tempHandler = initHandlers(context);
        return Collections.unmodifiableMap(tempHandler);
    }

    private HashMap<String, XHandler> initHandlers(ApplicationContext context) {
        HashMap<String, XHandler> tempHandler = new HashMap<>();
        // @XController 정의한 bean
        for (Object bean : context.getBeansWithAnnotation(XController.class).values()) {
            for (Method method : ReflectionUtils.findNoProxyClass(bean).getMethods()) {
                registerHandlerMethod(bean, method, tempHandler);
            }
        }
        return tempHandler;
    }

    private void registerHandlerMethod(Object bean, Method method, HashMap<String, XHandler> tempHandler) {
        if(method.isAnnotationPresent(XMapping.class)){
            XMapping xMapping = method.getAnnotation(XMapping.class);
            String[] urls = xMapping.value();
            for (String url : urls) {
                if (tempHandler.containsKey(url)) {
                    throw new IllegalArgumentException("동일한 url을 등록할 수 없습니다.");
                }
                tempHandler.put(url, new DefaultXHandler(bean, method));
            }
        }
    }

    public XHandler findHandler(String url) {
        XHandler xHandler = handlers.get(url);
        if (xHandler == null) {
            throw new IllegalStateException("요청하신 api를 처리할 수 있는 handler가 존재하지 않습니다. url=" + url);
        }
        return xHandler;
    }


}

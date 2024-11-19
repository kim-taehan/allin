package develop.x.core.utils;

import lombok.experimental.UtilityClass;
import org.springframework.aop.support.AopUtils;

@UtilityClass
public class ReflectionUtils {

    public Class<?> findNoProxyClass(Object clazz){
        if (AopUtils.isAopProxy(clazz)) {
            return findNoProxyClass(clazz.getClass().getSuperclass());
        }
        if (clazz instanceof Class<?> innerClazz) {
            return innerClazz;
        }
        return clazz.getClass();
    }

}

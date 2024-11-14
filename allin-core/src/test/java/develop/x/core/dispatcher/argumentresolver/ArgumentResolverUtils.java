package develop.x.core.dispatcher.argumentresolver;


import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class ArgumentResolverUtils {

    public static Method findMethod(Class<?> clazz, String methodName){
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                return declaredMethod;
            }
        }
        throw new IllegalArgumentException("메서드를 찾을 수 없습니다.");
    }

    public static Parameter findParameter(Class<?> clazz, String methodName, String paramName){
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                for (Parameter parameter : declaredMethod.getParameters()) {
                    if (parameter.getName().equals(paramName)) {
                        return parameter;
                    }
                }
            }
        }
        throw new IllegalArgumentException("파라메터를 찾을 수 없습니다.");
    }
}

package develop.x.core.exception.advice.argumentresolver;


import java.lang.reflect.Method;

public final class ArgumentResolverUtils {

    public static Method findMethod(Class<?> clazz, String methodName){
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                return declaredMethod;
            }
        }
        throw new IllegalArgumentException("메서드를 찾을 수 없습니다.");
    }
}

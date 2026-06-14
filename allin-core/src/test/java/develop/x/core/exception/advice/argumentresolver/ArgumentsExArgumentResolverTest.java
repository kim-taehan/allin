package develop.x.core.exception.advice.argumentresolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static develop.x.core.exception.advice.argumentresolver.ArgumentResolverUtils.findMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("ArgumentsExArgumentResolver — Object[] 파라미터에 원본 args 를 그대로 주입")
class ArgumentsExArgumentResolverTest {

    ArgumentsExArgumentResolver argumentResolver = new ArgumentsExArgumentResolver();

    @Test
    @DisplayName("Object[] 타입만 지원하고 List 등 비배열 타입은 지원하지 않는다.")
    void supportObjectArrayType() {
        Method method = findMethod(ObjectService.class, "method");
        assertAll(
                () -> assertThat(argumentResolver.support(method.getParameters()[0])).isTrue(),
                () -> assertThat(argumentResolver.support(method.getParameters()[1])).isFalse()
        );
    }

    @Test
    @DisplayName("[현 동작 명시] Object[].isAssignableFrom 은 공변성으로 String[] 등 참조 배열도 true 로 본다.")
    void supportIsTrueForReferenceArraysDueToCovariance() {
        Method method = findMethod(ArrayService.class, "method");
        assertAll(
                // String[] 은 Object[] 의 하위 타입이므로 isAssignableFrom 이 true (현 생산 동작)
                () -> assertThat(argumentResolver.support(method.getParameters()[0])).isTrue(),
                // int[] 는 참조 배열이 아니므로 false
                () -> assertThat(argumentResolver.support(method.getParameters()[1])).isFalse()
        );
    }

    @Test
    @DisplayName("convert 는 throwable 과 무관하게 전달받은 args 동일 인스턴스를 그대로 반환한다.")
    void convertReturnsSameArgsInstance() {
        Object[] args = {"kim", 1234};

        Object result = argumentResolver.convert(new RuntimeException("ignored"), args);

        assertThat(result).isSameAs(args);
    }

    @Test
    @DisplayName("args 가 null 이면 null 을 그대로 반환한다.")
    void convertReturnsNullWhenArgsNull() {
        Object result = argumentResolver.convert(new RuntimeException(), null);

        assertThat(result).isNull();
    }

    static abstract class ObjectService {
        public abstract void method(Object[] objectArray, List<Object> objectList);
    }

    static abstract class ArrayService {
        public abstract void method(String[] stringArray, int[] intArray);
    }
}

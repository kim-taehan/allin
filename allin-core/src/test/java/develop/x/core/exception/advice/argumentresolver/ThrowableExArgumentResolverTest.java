package develop.x.core.exception.advice.argumentresolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static develop.x.core.exception.advice.argumentresolver.ArgumentResolverUtils.findMethod;
import static org.assertj.core.api.Assertions.assertThat;

class ThrowableExArgumentResolverTest {

    ThrowableExArgumentResolver argumentResolver = new ThrowableExArgumentResolver();

    @DisplayName("Throwable 하위 exception 들은 전부 지원한다.")
    @ParameterizedTest(name = "{index} method={0} 예상결과 {1}")
    @CsvSource({"'exception', true",
            "'runtimeException', true",
            "'illegalArgumentException', true",
            "'stringMethod', false"})
    void supportsThrowable(String methodName, boolean expected) {

        // given
        Method method = findMethod(ExceptionService.class, methodName);

        // when
        Parameter parameter = method.getParameters()[0];
        boolean ret = argumentResolver.support(parameter);

        // then
        assertThat(ret).isEqualTo(expected);
    }


    @Test
    @DisplayName("convert 는 전달받은 바로 그 Throwable 인스턴스를 그대로 반환한다(새 객체 생성/치환 금지).")
    void returnThrowable(){

        // given
        RuntimeException helloException = new RuntimeException("hello Exception");

        // when
        Object throwable = argumentResolver.convert(helloException, null);

        // then : 타입뿐 아니라 동일 인스턴스여야 한다.
        assertThat(throwable).isSameAs(helloException);
    }

    @Test
    @DisplayName("convert 는 args 와 무관하게 throwable 동일 인스턴스를 반환하며, 다른 Throwable 하위 타입도 그대로 보존한다.")
    void returnThrowablePreservesExactInstanceIgnoringArgs(){

        // given : checked Exception 하위 타입도 그대로 보존되어야 한다.
        IllegalStateException ex = new IllegalStateException("state");
        Object[] args = {"a", 1};

        // when
        Object resolved = argumentResolver.convert(ex, args);

        // then
        assertThat(resolved).isSameAs(ex);
    }


    static abstract class ExceptionService {
        public abstract void exception(Exception exception);
        public abstract void runtimeException(RuntimeException runtimeException);
        public abstract void illegalArgumentException(IllegalArgumentException illegalArgumentException);
        public abstract void stringMethod(String message);
    }

}
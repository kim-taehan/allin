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
    @DisplayName("Throwable 의 에러메시지를 변환해준다.")
    void returnThrowable(){

        // given
        String errorMsg = "hello Exception";
        RuntimeException helloException = new RuntimeException("hello Exception");

        // when
        Object message = argumentResolver.convert(helloException, null);

        // then
        assertThat(message).isInstanceOf(RuntimeException.class);
    }


    static abstract class ExceptionService {
        public abstract void exception(Exception exception);
        public abstract void runtimeException(RuntimeException runtimeException);
        public abstract void illegalArgumentException(IllegalArgumentException illegalArgumentException);
        public abstract void stringMethod(String message);
    }

}
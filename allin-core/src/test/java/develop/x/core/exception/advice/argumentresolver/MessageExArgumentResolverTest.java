package develop.x.core.exception.advice.argumentresolver;

import develop.x.core.exception.advice.annotation.XExParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static develop.x.core.exception.advice.argumentresolver.ArgumentResolverUtils.findMethod;
import static org.assertj.core.api.Assertions.assertThat;

class MessageExArgumentResolverTest {

    MessageExArgumentResolver argumentResolver = new MessageExArgumentResolver();

    @DisplayName("대소문자 구분없이 message, msg 이름의 String type은 지원한다.")
    @ParameterizedTest(name = "{index} method={0} 예상결과 {1}")
    @CsvSource({"'message', true",
            "'msg', true",
            "'Message', true",
            "'Msg', true",
            "'MESSAGE', true",
            "'MSG', true",
            "'noMessage', false",
            "'noXExParam', false"})
    void supportedMessageAndMsg(String methodName, boolean expected) {

        // given
        Method method = findMethod(MessageService.class, methodName);

        // when
        Parameter parameter = method.getParameters()[0];
        boolean ret = argumentResolver.support(parameter);

        // then
        assertThat(ret).isEqualTo(expected);
    }

    @Test
    @DisplayName("Throwable 의 에러메시지를 변환해준다.")
    void returnErrorMsg(){

        // given
        String errorMsg = "hello Exception";
        RuntimeException helloException = new RuntimeException("hello Exception");

        // when
        Object message = argumentResolver.convert(helloException, null);

        // then
        assertThat(message).isEqualTo(errorMsg);
    }


    static abstract class MessageService {
        public abstract void message(@XExParam("message") String message);
        public abstract void msg(@XExParam("msg") String msg);
        public abstract void Message(@XExParam("Message") String Message);
        public abstract void Msg(@XExParam("Msg") String Msg);
        public abstract void MESSAGE(@XExParam("MESSAGE") String MESSAGE);
        public abstract void MSG(@XExParam("MSG") String MSG);
        public abstract void noMessage(@XExParam("noMessage") String noMessage);
        public abstract void noXExParam(String message);
    }
}
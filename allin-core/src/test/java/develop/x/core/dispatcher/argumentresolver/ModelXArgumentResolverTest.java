package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
import develop.x.core.dispatcher.annotation.XModel;
import develop.x.core.utils.JsonUtils;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelXArgumentResolverTest {


    private final XArgumentResolver resolver = new ModelXArgumentResolver();

    @Test
    @DisplayName("@XModel 이 정의된 파라메터를 지원한다.")
    void support(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xModel", "xModel");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isTrue();
    }

    @Test
    @DisplayName("@XModel 이 정의되지 않으면 지원하지 않는다.")
    void notSupport(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xModel", "xModel2");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("@XModel 형태로 파라매터를 받을 수 있다.")
    void convert() {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xModel", "xModel");
        TestDto testDto = new TestDto();
        testDto.setAge(40);
        testDto.setName("kimtaehan");
        XRequest xRequest = new XRequest.Builder()
                .header("transactionId", UUID.randomUUID().toString())
                .body(JsonUtils.toByte(testDto))
                .build();

        // when
        TestDto convert = (TestDto) resolver.convert(parameter, xRequest);

        // then
        assertThat(convert).isEqualTo(testDto);
    }

    @Test
    @DisplayName("body 가 깨진 JSON 이면 IOException 을 RuntimeException 으로 래핑해 던진다.")
    void convertWrapsIOExceptionAsRuntimeException() {
        // given : '{' 만 있는 불완전 JSON
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xModel", "xModel");
        XRequest xRequest = new XRequest.Builder()
                .header("transactionId", UUID.randomUUID().toString())
                .body(new byte[]{0x7B}) // "{"
                .build();

        // when / then
        assertThatThrownBy(() -> resolver.convert(parameter, xRequest))
                .isInstanceOf(RuntimeException.class);
    }

    static abstract class Controller {
        public abstract void xModel(@XModel TestDto xModel, TestDto xModel2);
    }

    @Data
    static class TestDto {
        private String name;
        private int age;
    }

}
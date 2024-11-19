package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class XRequestXArgumentResolverTest {
    private final XArgumentResolver resolver = new XRequestXArgumentResolver();

    @Test
    @DisplayName("파라메터 타입이 XRequest 인 경우 지원하는 형태이다.")
    void support(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xRequest", "xRequest");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isTrue();
    }

    @Test
    @DisplayName("파라메터 타입이 XRequest 인 경우 지원하지 않는다.")
    void notSupport(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xRequest", "xRequest2");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("parameter, xRequest 를 가지고 xRequest 데이터를 가져올 수 있다.")
    void convert() {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "xRequest", "xRequest");
        String url = "/test-api";
        XRequest xRequest = new XRequest.Builder()
                .header("url", url)
                .body(new byte[10])
                .build();

        // when
        XRequest convert = (XRequest) resolver.convert(parameter, xRequest);

        // then
        assertThat(convert).isEqualTo(xRequest);

    }

    static abstract class Controller {
        public abstract void xRequest(XRequest xRequest, String xRequest2);
    }
}
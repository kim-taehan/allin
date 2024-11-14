package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.annotation.XParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UrlXArgumentResolverTest {

    private final XArgumentResolver resolver = new UrlXArgumentResolver();

    @Test
    @DisplayName("@XParam 가 선언되어 있고, String type parameter 지원하는 형태이다.")
    void support(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "url", "url");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isTrue();
    }

    @Test
    @DisplayName("@XParam 가 선언되어 있고, String type parameter 지원하는 형태이다.")
    void notSupport(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(TransactionIdXArgumentResolverTest.Controller.class, "transId", "transId2");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("parameter, xRequest 를 가지고 transactionId를 가져올 수 있다.")
    void convert() {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(TransactionIdXArgumentResolverTest.Controller.class, "transId", "transId");
        String url = "/test-api";
        XRequest xRequest = new XRequest.Builder()
                .header("url", url)
                .body(new byte[10])
                .build();

        // when
        String convert = (String) resolver.convert(parameter, xRequest);

        // then
        assertThat(convert).isEqualTo(url);

    }

    static abstract class Controller {
        public abstract void url(@XParam("url") String url, String url2);
    }

}
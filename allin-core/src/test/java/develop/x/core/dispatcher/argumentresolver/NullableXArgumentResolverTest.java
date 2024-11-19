package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
import develop.x.core.dispatcher.annotation.XParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NullableXArgumentResolverTest {

    private final XArgumentResolver resolver = new NullableXArgumentResolver();

    @Test
    @DisplayName("NullableXArgumentResolver 는 항상 support 는 false 을 응답하게 된다.")
    void support(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "transId", "transId");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("NullableXArgumentResolver convert 는 항상 null 을 반환한다.")
    void convert() {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(TransactionIdXArgumentResolverTest.Controller.class, "transId", "transId");
        String transId = UUID.randomUUID().toString();
        XRequest xRequest = new XRequest.Builder()
                .header("transactionId", transId)
                .body(new byte[10])
                .build();

        // when
        String convert = (String) resolver.convert(parameter, xRequest);

        // then
        assertThat(convert).isNull();
    }

    static abstract class Controller {
        public abstract void transId(@XParam("transactionId") String transId, String transId2);
    }
}
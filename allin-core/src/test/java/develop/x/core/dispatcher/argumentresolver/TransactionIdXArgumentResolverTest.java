package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.annotation.XParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionIdXArgumentResolverTest {

    private final XArgumentResolver resolver = new TransactionIdXArgumentResolver();

    @Test
    @DisplayName("@XParam 가 선언되어 있고, String type parameter 지원하는 형태이다.")
    void support(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "transId", "transId");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isTrue();
    }

    @Test
    @DisplayName("@XParam 가 선언되어 있고, String type parameter 지원하는 형태이다.")
    void notSupport(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "transId", "transId2");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("parameter, xRequest 를 가지고 transactionId를 가져올 수 있다.")
    void convert() {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "transId", "transId");
        String transId = UUID.randomUUID().toString();
        XRequest xRequest = new XRequest.Builder()
                .header("transactionId", transId)
                .body(new byte[10])
                .build();

        // when
        String convert = (String) resolver.convert(parameter, xRequest);

        // then
        assertThat(convert).isEqualTo(transId);

    }

    static abstract class Controller {
        public abstract void transId(@XParam("transactionId") String transId, String transId2);
    }
}
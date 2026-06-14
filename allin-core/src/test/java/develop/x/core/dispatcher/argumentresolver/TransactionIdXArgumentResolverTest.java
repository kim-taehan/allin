package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
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
    @DisplayName("@XParam 어노테이션이 없는 String 파라미터는 지원하지 않는다.")
    void notSupport(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "transId", "transId2");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("@XParam 이 등록되지 않은 이름(unknown)이면 이름 set 불포함으로 지원하지 않는다.")
    void notSupportUnknownName(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "unknownName", "foo");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("@XParam(transactionId) 이라도 String 이 아닌 타입(int)이면 지원하지 않는다.")
    void notSupportNonStringType(){
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "intParam", "transId");

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
        public abstract void unknownName(@XParam("foo") String foo);
        public abstract void intParam(@XParam("transactionId") int transId);
    }
}
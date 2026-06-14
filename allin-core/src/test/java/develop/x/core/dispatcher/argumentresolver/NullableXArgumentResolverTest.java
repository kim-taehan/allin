package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
import develop.x.io.id.XIdGenerator;
import develop.x.core.dispatcher.annotation.XModel;
import develop.x.core.dispatcher.annotation.XParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NullableXArgumentResolver — 항상 미지원이며 convert 는 항상 null(기본 fall-through resolver)")
class NullableXArgumentResolverTest {

    private final XArgumentResolver resolver = new NullableXArgumentResolver();

    @DisplayName("어떤 종류의 파라미터(어노테이션 유/무, XRequest, @XModel)든 support 는 항상 false 다.")
    @ParameterizedTest(name = "{index} param={0}")
    @ValueSource(strings = {"annotated", "plain", "xRequest", "model"})
    void supportIsAlwaysFalse(String paramName) {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "handle", paramName);

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("convert 는 parameter/request 와 무관하게 항상 null 을 반환한다.")
    void convert() {
        // given : 자신의 Controller 파라미터 사용(타 테스트 클래스 의존 제거)
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "handle", "annotated");
        XRequest xRequest = new XRequest.Builder()
                .header("transactionId", XIdGenerator.nextTransactionId())
                .body(new byte[10])
                .build();

        // when
        Object convert = resolver.convert(parameter, xRequest);

        // then
        assertThat(convert).isNull();
    }

    static abstract class Controller {
        public abstract void handle(@XParam("transactionId") String annotated,
                                    String plain,
                                    XRequest xRequest,
                                    @XModel Object model);
    }
}

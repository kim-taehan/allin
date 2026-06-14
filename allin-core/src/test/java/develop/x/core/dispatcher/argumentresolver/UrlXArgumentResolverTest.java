package develop.x.core.dispatcher.argumentresolver;

import develop.x.io.XRequest;
import develop.x.core.dispatcher.annotation.XParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UrlXArgumentResolver — @XParam(url|apiName) 대소문자 무시 매칭")
class UrlXArgumentResolverTest {

    private final XArgumentResolver resolver = new UrlXArgumentResolver();

    @DisplayName("@XParam 값이 url/apiName 이면 대소문자 변형(URL, Url, apiName, APINAME)도 String 파라미터를 지원한다.")
    @ParameterizedTest(name = "{index} @XParam(\"{0}\") -> 지원={1}")
    @CsvSource({
            "url, true",
            "URL, true",
            "Url, true",
            "apiName, true",
            "apiname, true",
            "APINAME, true",
            "transactionId, false",
            "unknown, false"
    })
    void support(String paramValue, boolean expected) {
        // given : @XParam(paramValue) String 파라미터를 동적으로 만들 수 없으므로
        //         각 케이스를 미리 선언한 Controller 메서드에서 찾는다.
        Parameter parameter = SupportController.find(paramValue);

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isEqualTo(expected);
    }

    @Test
    @DisplayName("@XParam 어노테이션이 없는 String 파라미터는 지원하지 않는다.")
    void notSupportWithoutAnnotation() {
        // given : Controller.url 의 두 번째 파라미터(url2)는 어노테이션 없음
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "url", "url2");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("@XParam(url) 이지만 String 이 아닌 타입(int)은 지원하지 않는다.")
    void notSupportWhenNotString() {
        // given
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "urlInt", "url");

        // when
        boolean support = resolver.support(parameter);

        // then
        assertThat(support).isFalse();
    }

    @Test
    @DisplayName("convert 는 parameter 와 무관하게 url 헤더 값을 반환한다.")
    void convert() {
        // given : 자신의 Controller.url 파라미터를 사용(타 테스트 클래스 의존 제거)
        Parameter parameter = ArgumentResolverUtils.findParameter(Controller.class, "url", "url");
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
        public abstract void urlInt(@XParam("url") int url);
    }

    /** 다양한 @XParam 값/대소문자 케이스를 메서드로 선언해 둔 컨트롤러. */
    static abstract class SupportController {
        public abstract void url(@XParam("url") String url);
        public abstract void URL(@XParam("URL") String url);
        public abstract void Url(@XParam("Url") String url);
        public abstract void apiName(@XParam("apiName") String url);
        public abstract void apiname(@XParam("apiname") String url);
        public abstract void APINAME(@XParam("APINAME") String url);
        public abstract void transactionId(@XParam("transactionId") String url);
        public abstract void unknown(@XParam("unknown") String url);

        static Parameter find(String methodName) {
            return ArgumentResolverUtils.findParameter(SupportController.class, methodName, "url");
        }
    }
}

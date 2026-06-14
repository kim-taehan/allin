package develop.x.core.dispatcher.argumentresolver;

import develop.x.core.dispatcher.annotation.XModel;
import develop.x.core.dispatcher.annotation.XParam;
import develop.x.core.dispatcher.handler.DefaultXHandler;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.utils.JsonUtils;
import develop.x.io.XRequest;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * XArgumentProvider 통합 경로 검증: 여러 resolver 가 빈으로 등록된 상태에서
 * 혼합 파라미터(@XModel + @XParam(url) + @XParam(transactionId) + XRequest + 미매칭)가
 * 올바른 resolver 로 라우팅되고, 미매칭 파라미터는 NullableXArgumentResolver(default)로
 * fall-through 되어 null 이 주입되는지 확인한다(감사 '횡단 누락' 항목).
 */
@DisplayName("XArgumentProvider — 혼합 파라미터 resolver 조립 + Nullable default fall-through")
class XArgumentProviderTest {

    @Configuration
    static class ResolverConfig {
        @Bean XArgumentResolver modelXArgumentResolver() { return new ModelXArgumentResolver(); }
        @Bean XArgumentResolver urlXArgumentResolver() { return new UrlXArgumentResolver(); }
        @Bean XArgumentResolver transactionIdXArgumentResolver() { return new TransactionIdXArgumentResolver(); }
        @Bean XArgumentResolver xRequestXArgumentResolver() { return new XRequestXArgumentResolver(); }
        // NullableXArgumentResolver 는 빈 등록하지 않는다 — provider 의 defaultResolver 로만 동작해야 함.
    }

    @Data
    static class TestDto {
        private String name;
        private int age;
    }

    static class MixedController {
        public void handle(@XModel TestDto model,
                           @XParam("url") String url,
                           @XParam("transactionId") String txId,
                           XRequest request,
                           String unmatched) {
        }
    }

    private static XHandler handlerOf(String methodName) throws Exception {
        Method method = MixedController.class.getMethod(methodName,
                TestDto.class, String.class, String.class, XRequest.class, String.class);
        return new DefaultXHandler(new MixedController(), method);
    }

    @Test
    @DisplayName("각 파라미터가 올바른 resolver 로 변환되고, 미매칭 String 은 default(Nullable)로 null 이 된다.")
    void convertArgumentsRoutesEachResolverAndFallsThroughToNull() throws Exception {
        // given
        ApplicationContext context = new AnnotationConfigApplicationContext(ResolverConfig.class);
        XArgumentProvider provider = new XArgumentProvider(context);

        TestDto dto = new TestDto();
        dto.setName("kimtaehan");
        dto.setAge(40);

        String url = "/order-api";
        String txId = UUID.randomUUID().toString();

        XRequest request = new XRequest.Builder()
                .header("url", url)
                .header("transactionId", txId)
                .body(JsonUtils.toByte(dto))
                .build();

        XHandler handler = handlerOf("handle");

        // when
        Object[] args = provider.convertArguments(handler, request);

        // then : 파라미터 순서대로 각 resolver 결과가 매핑되어야 한다.
        assertAll(
                () -> assertThat(args).hasSize(5),
                () -> assertThat(args[0]).isInstanceOf(TestDto.class),
                () -> assertThat(args[0]).isEqualTo(dto),               // @XModel
                () -> assertThat(args[1]).isEqualTo(url),               // @XParam("url")
                () -> assertThat(args[2]).isEqualTo(txId),              // @XParam("transactionId")
                () -> assertThat(args[3]).isSameAs(request),            // XRequest
                () -> assertThat(args[4]).isNull()                      // 미매칭 -> Nullable default
        );
    }
}

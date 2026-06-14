package develop.x.core.exception.advice.argumentresolver;

import develop.x.core.exception.advice.ExAdviceOrder;
import develop.x.core.exception.advice.ExceptionHandler;
import develop.x.core.exception.advice.annotation.XExParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * XExArgumentProvider 통합 경로 검증: 여러 XExArgumentResolver 가 빈으로 등록된 상태에서
 * 혼합 파라미터(Throwable + @XExParam(message) String + Object[] args + 미매칭)가 올바른
 * resolver 로 라우팅되고, 미매칭 파라미터는 NullableExArgumentResolver(default)로 fall-through
 * 되어 null 이 주입되는지 확인한다(감사 '횡단 누락' 항목).
 */
@DisplayName("XExArgumentProvider — 혼합 파라미터 resolver 조립 + Nullable default fall-through")
class XExArgumentProviderTest {

    @Configuration
    static class ResolverConfig {
        @Bean XExArgumentResolver throwableExArgumentResolver() { return new ThrowableExArgumentResolver(); }
        @Bean XExArgumentResolver messageExArgumentResolver() { return new MessageExArgumentResolver(); }
        @Bean XExArgumentResolver argumentsExArgumentResolver() { return new ArgumentsExArgumentResolver(); }
        // NullableExArgumentResolver 는 빈 등록하지 않는다 — provider 의 defaultResolver 로만 동작해야 함.
    }

    static class Advice {
        public void handle(RuntimeException ex,
                           @XExParam("message") String message,
                           Object[] args,
                           Integer unmatched) {
        }
    }

    private static ExceptionHandler handlerOf() throws Exception {
        Method method = Advice.class.getMethod("handle",
                RuntimeException.class, String.class, Object[].class, Integer.class);
        return new ExceptionHandler(RuntimeException.class, new Advice(), method, ExAdviceOrder.BUSINESS);
    }

    @Test
    @DisplayName("각 파라미터가 올바른 resolver 로 변환되고, 미매칭 파라미터는 default(Nullable)로 null 이 된다.")
    void convertArgumentsRoutesEachResolverAndFallsThroughToNull() throws Exception {
        // given
        ApplicationContext context = new AnnotationConfigApplicationContext(ResolverConfig.class);
        XExArgumentProvider provider = new XExArgumentProvider(context);

        RuntimeException ex = new IllegalStateException("boom-message");
        Object[] args = {"kim", 1234};

        // when
        Object[] resolved = provider.convertArguments(handlerOf(), ex, args);

        // then
        assertAll(
                () -> assertThat(resolved).hasSize(4),
                () -> assertThat(resolved[0]).isSameAs(ex),             // Throwable
                () -> assertThat(resolved[1]).isEqualTo("boom-message"),// @XExParam("message")
                () -> assertThat(resolved[2]).isSameAs(args),           // Object[] args
                () -> assertThat(resolved[3]).isNull()                  // 미매칭 -> Nullable default
        );
    }
}

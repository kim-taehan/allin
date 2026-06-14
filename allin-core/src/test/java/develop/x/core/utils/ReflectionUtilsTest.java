package develop.x.core.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.*;

class ReflectionUtilsTest {


    @DisplayName("aop proxy 객체가 아니면 자신의 클래스 타입을 반환한다")
    @Test
    void returnOriginClass() {

        // given
        try (AnnotationConfigApplicationContext applicationContext =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {
            NoProxyService noProxyService = applicationContext.getBean(NoProxyService.class);

            // 전제 검증: NoProxyService 는 AOP advice 대상이 아니므로 프록시가 아니어야 한다.
            assertThat(AopUtils.isAopProxy(noProxyService)).isFalse();

            // when
            Class<?> noProxyClass = ReflectionUtils.findNoProxyClass(noProxyService);

            // then
            assertThat(noProxyClass).isSameAs(noProxyService.getClass());
            assertThat(noProxyClass).isSameAs(NoProxyService.class);
        }
    }

    @DisplayName("aop proxy 객체는 proxy 객체가 아닌 본래 클래스를 반환한다.")
    @Test
    void returnOriginClassByProxyInstance() {
        // given
        try (AnnotationConfigApplicationContext applicationContext =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {
            ProxyService proxyService = applicationContext.getBean(ProxyService.class);

            // 전제 검증: ProxyService 는 TestAspect 의 advice 대상이라 실제 CGLIB 프록시여야 한다.
            // (프록시가 만들어지지 않으면 unwrap 분기가 검증되지 않으므로 여기서 실패시킨다.)
            assertThat(AopUtils.isAopProxy(proxyService)).isTrue();
            assertThat(AopUtils.isCglibProxy(proxyService)).isTrue();

            // when
            Class<?> proxyClass = ReflectionUtils.findNoProxyClass(proxyService);

            // then
            // unwrap 결과는 프록시 클래스가 아닌 본래 클래스(ProxyService) 여야 한다(양방향 검증).
            assertThat(proxyClass).isSameAs(ProxyService.class);
            assertThat(proxyClass).isNotSameAs(proxyService.getClass());
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public NoProxyService noProxyService() {
            return new NoProxyService();
        }

        @Bean
        public ProxyService proxyService() {
            return new ProxyService();
        }

        @Bean
        public TestAspect testAspect() {
            return new TestAspect();
        }
    }


    static class NoProxyService {
    }

    static class ProxyService {
        public String hello() {
            return "hello";
        }
    }

    @Aspect
    static class TestAspect {
        // ProxyService 의 메서드 실행에 매칭되는 유효한 포인트컷 → 실제 프록시가 생성된다.
        @Around("execution(* develop.x.core.utils.ReflectionUtilsTest.ProxyService.*(..))")
        public Object processProxyService(ProceedingJoinPoint joinPoint) throws Throwable {
            return joinPoint.proceed();
        }
    }

}

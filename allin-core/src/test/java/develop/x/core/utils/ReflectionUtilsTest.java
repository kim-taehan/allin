package develop.x.core.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.*;

class ReflectionUtilsTest {
    
    
//    @DisplayName("aop proxy 클래스가 아닌 기본 클래스를 반환한다.")
    @DisplayName("aop proxy 객체가 아니면 자신의 클래스 타입을 반환한다")
    @Test
    void returnOriginClass(){

        // given
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestConfig.class);
        NoProxyService noProxyService = applicationContext.getBean(NoProxyService.class);

        // when
        Class<?> noProxyClass = ReflectionUtils.findNoProxyClass(noProxyService);

        // then
        assertThat(noProxyClass).isSameAs(noProxyService.getClass());
    }

    @DisplayName("aop proxy 객체는 proxy 객체가 아닌 본래 클래스를 반환한다.")
    @Test
    void returnOriginClassByProxyInstance() {
        // given
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestConfig.class);
        ProxyService proxyService = applicationContext.getBean(ProxyService.class);

        // when
        Class<?> proxyClass = ReflectionUtils.findNoProxyClass(proxyService);

        // then
        assertThat(proxyClass).isSameAs(proxyService.getClass());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public NoProxyService noProxyService(){
            return new NoProxyService();
        }

        @Bean
        public ProxyService proxyService(){
            return new ProxyService();
        }

        @Bean
        public TestAspect testAspect(){
            return new TestAspect();
        }
    }


    static class NoProxyService {
    }

    static class ProxyService {
    }

    @Aspect
    static class TestAspect {
        @Around("develop.x.core.utils.ReflectionUtilsTest.TestConfig")
        public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
            try{
                return joinPoint.proceed();
            } catch (Exception exception){
                return null;
            }
        }
    }

}
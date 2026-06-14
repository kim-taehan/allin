package develop.x.core.exception.advice;

import develop.x.core.exception.advice.annotation.XExAdvice;
import develop.x.core.exception.advice.annotation.XExHandler;
import develop.x.core.exception.advice.annotation.XExParam;
import develop.x.core.exception.advice.argumentresolver.ArgumentsExArgumentResolver;
import develop.x.core.exception.advice.argumentresolver.MessageExArgumentResolver;
import develop.x.core.exception.advice.argumentresolver.ThrowableExArgumentResolver;
import develop.x.core.exception.advice.argumentresolver.XExArgumentProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class XExceptionAdvisorTest {


    @Test
    @DisplayName("IllegalArgumentException 발생시 IllegalArgumentException 을 처리할 수 있는 ex handler 를 통해 예외처리가 된다.")
    void illegalArgumentException() {

        // given
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestConfig.class);
        XExceptionAdvisor xExceptionAdvisor = applicationContext.getBean(XExceptionAdvisor.class);
        ExAdviceA exAdviceA = applicationContext.getBean(ExAdviceA.class);

        Object[] args = {"kimtaehan", 1234};
        String message = "Exception test";

        // when
        boolean isCallExAdvice = xExceptionAdvisor.run(new IllegalArgumentException(message), args);

        // then
        Assertions.assertAll(
                () -> assertThat(isCallExAdvice).isTrue(),
                () -> assertThat(exAdviceA.throwable).isInstanceOf(IllegalArgumentException.class),
                () -> assertThat(exAdviceA.args).isEqualTo(args),
                () -> assertThat(exAdviceA.message).isEqualTo(message)
        );
    }

    @Test
    @DisplayName("동일한 Exception 형태를 처리하는 advice 는 존재할 수 없다")
    void duplicatedEx() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext(DuplicatedConfig.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("동일한 레벨을 같은 Throwable");
    }


    @Test
    @DisplayName("동일 RuntimeException 핸들러가 둘이면 ExAdviceOrder index 가 작은 쪽(BUSINESS<SYSTEM)이 선택되고, 진 advice(SYSTEM)는 호출되지 않는다.")
    void duplicatedExButAdviceLevel() {
        // given : ExAdviceA(BUSINESS, index=2) vs DuplicationAdviceDiffExAdviceOrder(SYSTEM, index=3)
        //         compareAdvice 는 index 오름차순 정렬 -> BUSINESS 가 find() 에서 먼저 매칭되어야 한다.
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestConfig.class);
        XExceptionAdvisor xExceptionAdvisor = applicationContext.getBean(XExceptionAdvisor.class);
        ExAdviceA exAdviceA = applicationContext.getBean(ExAdviceA.class);
        DuplicationAdviceDiffExAdviceOrder systemAdvice =
                applicationContext.getBean(DuplicationAdviceDiffExAdviceOrder.class);

        Object[] args = {"kimtaehan", 1234};
        String message = "Exception test";

        // when
        boolean isCallExAdvice = xExceptionAdvisor.run(new RuntimeException(message), args);

        // then : 양방향 단언 — 이긴 쪽(ExAdviceA)은 호출, 진 쪽(SYSTEM)은 미호출.
        Assertions.assertAll(
                () -> assertThat(isCallExAdvice).isTrue(),
                () -> assertThat(exAdviceA.throwable).isInstanceOf(RuntimeException.class),
                () -> assertThat(exAdviceA.args).isEqualTo(args),
                () -> assertThat(exAdviceA.message).isEqualTo(message),
                () -> assertThat(systemAdvice.called)
                        .as("index 가 더 큰 SYSTEM advice 는 우선순위에서 밀려 호출되면 안 된다")
                        .isFalse()
        );
    }


    @TestConfiguration
    static class TestConfig {
        @Bean
        public ExAdviceA exAdviceA(){
            return new ExAdviceA();
        }

        @Bean
        public DuplicationAdviceDiffExAdviceOrder duplicationAdviceDiffExAdviceOrder(){
            return new DuplicationAdviceDiffExAdviceOrder();
        }

        @Bean
        public XExArgumentProvider xExArgumentProvider(ApplicationContext context){
            return new XExArgumentProvider(context);
        }

        @Bean
        public ExceptionHandlerManager exceptionHandlerManager(ApplicationContext context) {
            return new ExceptionHandlerManager(context);
        }

        @Bean
        public XExceptionAdvisor xExceptionAdvisor(ExceptionHandlerManager exceptionHandlerManager, XExArgumentProvider exArgumentProvider) {
            return new XExceptionAdvisor(exArgumentProvider, exceptionHandlerManager);
        }

        @Bean
        public ThrowableExArgumentResolver throwableExArgumentResolver( ) {
            return new ThrowableExArgumentResolver();
        }

        @Bean
        public ArgumentsExArgumentResolver argumentsExArgumentResolver( ) {
            return new ArgumentsExArgumentResolver();
        }

        @Bean
        public MessageExArgumentResolver messageExArgumentResolver( ) {
            return new MessageExArgumentResolver();
        }
    }

    @TestConfiguration
    static class DuplicatedConfig {
        @Bean
        public DuplicationAdvice duplicationAdvice(){
            return new DuplicationAdvice();
        }

        @Bean
        public ExAdviceA exAdviceA(){
            return new ExAdviceA();
        }

        @Bean
        public XExArgumentProvider xExArgumentProvider(ApplicationContext context){
            return new XExArgumentProvider(context);
        }
        @Bean
        public ExceptionHandlerManager exceptionHandlerManager(ApplicationContext context) {
            return new ExceptionHandlerManager(context);
        }

        @Bean
        public XExceptionAdvisor xExceptionAdvisor(ExceptionHandlerManager exceptionHandlerManager, XExArgumentProvider exArgumentProvider) {
            return new XExceptionAdvisor(exArgumentProvider, exceptionHandlerManager);
        }
    }
    @XExAdvice
    static class ExAdviceA {

        public Throwable throwable;
        public String message;
        public Object[] args;

        @XExHandler({RuntimeException.class})
        public void runtimeException(RuntimeException ex, @XExParam("message") String message, Object[] args) {
            this.throwable = ex;
            this.message = message;
            this.args = args;
        }

        @XExHandler({IllegalArgumentException.class})
        public void illegalArgumentException(IllegalArgumentException ex, @XExParam("message") String message, Object[] args) {
            this.throwable = ex;
            this.message = message;
            this.args = args;
        }
    }

    @XExAdvice
    static class DuplicationAdvice {
        @XExHandler({IllegalArgumentException.class})
        public void illegalArgumentException() {
        }
    }

    @XExAdvice(ExAdviceOrder.SYSTEM)
    static class DuplicationAdviceDiffExAdviceOrder {
        public boolean called = false;

        @XExHandler({RuntimeException.class})
        public void runtime() {
            this.called = true;
        }
    }

}
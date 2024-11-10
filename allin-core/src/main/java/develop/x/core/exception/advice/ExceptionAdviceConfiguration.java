package develop.x.core.exception.advice;

import develop.x.core.exception.advice.argumentresolver.*;
import develop.x.core.exception.advice.aspect.ExceptionAdviceAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ExceptionAdviceConfiguration {

    @Bean
    public ExceptionAdviceAspect exceptionAdviceAspect(XExceptionAdvisor xExceptionAdvisor){
        return new ExceptionAdviceAspect(xExceptionAdvisor);
    }

    @Bean
    public XExceptionAdvisor xExceptionAdvisor(XExArgumentProvider xExArgumentProvider, ExceptionHandlerManager exceptionHandlerManager){
        return new XExceptionAdvisor(xExArgumentProvider, exceptionHandlerManager);
    }

    @Bean
    public CoreXExceptionAdvice coreXExceptionAdvice(){
        return new CoreXExceptionAdvice();
    }

    @Bean
    public ThrowableExArgumentResolver throwableExArgumentResolver(){
        return new ThrowableExArgumentResolver();
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
    public ArgumentsExArgumentResolver argumentsExArgumentResolver(){
        return new ArgumentsExArgumentResolver();
    }

    @Bean
    public MessageExArgumentResolver messageExArgumentResolver(){
        return new MessageExArgumentResolver();
    }



}

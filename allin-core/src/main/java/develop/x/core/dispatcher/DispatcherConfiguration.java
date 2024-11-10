package develop.x.core.dispatcher;

import develop.x.core.dispatcher.argumentresolver.*;
import develop.x.core.dispatcher.handler.XHandlerManager;
import develop.x.core.executor.BusinessXExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


@AutoConfiguration
public class DispatcherConfiguration {

    @Bean
    public XDispatcher xDispatcher(XHandlerManager handlerManager, XArgumentProvider xArgumentProvider, BusinessXExecutor executor){
        return new VirtualThreadDispatcher(handlerManager, xArgumentProvider, executor);
    }

    @Bean
    public XHandlerManager xHandlerManager(ApplicationContext context) {
        return new XHandlerManager(context);
    }

    @Bean
    public XArgumentProvider xArgumentProvider(ApplicationContext context){
        return new XArgumentProvider(context);
    }

    @Bean
    public XArgumentResolver modelXArgumentResolver() {
        return new ModelXArgumentResolver();
    }

    @Bean
    public XArgumentResolver urlXArgumentResolver() {
        return new UrlXArgumentResolver();
    }

}

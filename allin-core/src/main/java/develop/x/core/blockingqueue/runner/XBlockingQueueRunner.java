package develop.x.core.blockingqueue.runner;

import develop.x.core.blockingqueue.AbstractXBlockingQueue;
import develop.x.core.blockingqueue.XBlockingQueue;
import develop.x.core.blockingqueue.annotation.XBlockingQueueMapping;
import develop.x.core.executor.XExecutor;
import develop.x.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class XBlockingQueueRunner implements ApplicationRunner {

    private final ApplicationContext context;
    private final XExecutor executor;

    public XBlockingQueueRunner(ApplicationContext context, XExecutor executor) {
        this.context = context;
        this.executor = executor;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeBlockingQueueRunner();
    }

    private void initializeBlockingQueueRunner() {

        for (String beanDefinitionName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanDefinitionName);
            Class<?> clazz = ReflectionUtils.findNoProxyClass(bean);

            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> !Objects.isNull(method.getAnnotation(XBlockingQueueMapping.class)))
                    .forEach(method -> registerMethod(bean, method));
        }
    }

    private void registerMethod(Object bean, Method method) {
        XBlockingQueueMapping xBlockingQueueMapping = method.getAnnotation(XBlockingQueueMapping.class);
        Class<? extends XBlockingQueue<?>> xBlockingQueue = xBlockingQueueMapping.value();

        try {
            Object bqBean = context.getBean(xBlockingQueue);
            if (bqBean instanceof AbstractXBlockingQueue<?> bqInstance) {
                bqInstance.run(executor, item -> {
                    try {
                        method.invoke(bean, item);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalArgumentException e) {
                        log.error("");
                    }
                });
            }

        } catch (NoSuchBeanDefinitionException e) {
            log.error("bq가 bean 으로 등록되어 있지 않습니다." + e);
        }
    }
}

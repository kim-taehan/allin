package develop.x.core.exception.advice;

import develop.x.core.exception.advice.annotation.XExAdvice;
import develop.x.core.exception.advice.annotation.XExHandler;
import develop.x.core.utils.ReflectionUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ExceptionHandlerManager {

    private final Collection<ExceptionHandler> xExceptionAdvices;

    public ExceptionHandlerManager(ApplicationContext context) {
        this.xExceptionAdvices = Collections.unmodifiableCollection(initializeAdvices(context));
    }

    // 서버시작시에 exception advice 을 먼저 확인한다.
    private Collection<ExceptionHandler> initializeAdvices(ApplicationContext context) {
        return createExceptionAdviceMap(context.getBeansWithAnnotation(XExAdvice.class).values())
                .stream()
                .sorted(this::compareAdvice)
                .toList();
    }


    private List<ExceptionHandler> createExceptionAdviceMap(Collection<Object> beans) {
        List<ExceptionHandler> candidateAdvices = new ArrayList<>();
        for (Object bean : beans) {
            ExAdviceOrder exAdviceOrder = bean.getClass().getAnnotation(XExAdvice.class).value();
            for (Method method : ReflectionUtils.findNoProxyClass(bean).getDeclaredMethods()) {
                if(method.isAnnotationPresent(XExHandler.class)){
                    XExHandler exHandler = method.getAnnotation(XExHandler.class);
                    Class<? extends Throwable>[] value = exHandler.value();
                    for (Class<? extends Throwable> throwable : value) {
                        candidateAdvices.add(new ExceptionHandler(throwable, bean, method, exAdviceOrder));
                    }
                }
            }
        }
        return candidateAdvices;
    }

    private int compareAdvice(ExceptionHandler exceptionHandlerA, ExceptionHandler exceptionHandlerB) {

        if (exceptionHandlerA.equals(exceptionHandlerB)) {
            // 처리되는 Throwable, ExAdviceOrder 이 같은면 빌드 exception 을 발생시킴
            throw new IllegalStateException("동일한 레벨을 같은 Throwable("+exceptionHandlerA.throwable()+") 은 등록될 수 없습니다. " +
                    exceptionHandlerA.bean().getClass() + ", " + exceptionHandlerB.bean().getClass());
        }

        if (exceptionHandlerA.order() != exceptionHandlerB.order()) {
            return exceptionHandlerA.order().getIndex() - exceptionHandlerB.order().getIndex();
        }

        return exceptionHandlerA.throwable().isAssignableFrom(exceptionHandlerB.throwable()) ? 1 : -1;
    }

    public ExceptionHandler find(Class<? extends Throwable> throwable) {
        for (ExceptionHandler exceptionHandler : xExceptionAdvices) {
            if (exceptionHandler.throwable().isAssignableFrom(throwable)) {
                return exceptionHandler;
            }
        }
        throw new IllegalArgumentException("ex handler 를 찾을 수 없습니다. " + throwable.getSimpleName());
    }
}

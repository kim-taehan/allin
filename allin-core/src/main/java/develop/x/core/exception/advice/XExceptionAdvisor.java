package develop.x.core.exception.advice;

import develop.x.core.exception.advice.argumentresolver.XExArgumentProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class XExceptionAdvisor {

    private final XExArgumentProvider xExArgumentProvider;
    private final ExceptionHandlerManager exceptionHandlerManager;

    public XExceptionAdvisor(XExArgumentProvider xExArgumentProvider, ExceptionHandlerManager exceptionHandlerManager) {
        this.xExArgumentProvider = xExArgumentProvider;
        this.exceptionHandlerManager = exceptionHandlerManager;
    }

    public boolean run(Throwable throwable, Object[] args) {
        try {
            ExceptionHandler exceptionHandler = exceptionHandlerManager.find(throwable.getClass());
            Object[] params = xExArgumentProvider.convertArguments(exceptionHandler, throwable, args);
            return callReflectionMethod(exceptionHandler, params);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean callReflectionMethod(ExceptionHandler exceptionHandler, Object[] params) {
        try {
            exceptionHandler.method().invoke(exceptionHandler.bean(), params);
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("exception advice 호출중에 애러가 발생", e);
            return false;
        }
    }
}

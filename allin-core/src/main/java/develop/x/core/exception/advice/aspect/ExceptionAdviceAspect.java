package develop.x.core.exception.advice.aspect;

import develop.x.core.exception.advice.XExceptionAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class ExceptionAdviceAspect {

    private final XExceptionAdvisor xExceptionAdvisor;

    @Pointcut("@annotation(develop.x.core.dispatcher.annotation.XMapping)")
    public void xMapping(){}

    @Pointcut("@annotation(develop.x.core.blockingqueue.annotation.XBlockingQueueMapping)")
    public void xBlockingQueueMapping(){}

    @Around("xMapping() || xBlockingQueueMapping()")
    public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        try{
            return joinPoint.proceed();
        } catch (Exception exception){
            // Exception advice handler 중에 처리할 수 있는 handler 가 있는지 확인한다.
            if (!xExceptionAdvisor.run(exception, joinPoint.getArgs())) {
                log.error("처리하지 못한 예외가 발생했습니다.", exception);
                throw new RuntimeException("Exception advice 로 처리되지 못한 애러", exception);
            }
            return null;
        }
    }
}

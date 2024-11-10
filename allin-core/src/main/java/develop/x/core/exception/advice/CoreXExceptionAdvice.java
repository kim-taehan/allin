package develop.x.core.exception.advice;

import develop.x.core.exception.advice.annotation.XExAdvice;
import develop.x.core.exception.advice.annotation.XExHandler;
import develop.x.core.exception.advice.annotation.XExParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@XExAdvice(ExAdviceOrder.SYSTEM)
public class CoreXExceptionAdvice {

    @XExHandler({IllegalArgumentException.class, IllegalStateException.class})
    public void illegalXXXExAdviceHandler(RuntimeException runtimeException, @XExParam("message") String message) {
    }

    @XExHandler({RuntimeException.class})
    public void runtimeExAdviceHandler(RuntimeException runtimeException) {
    }
}

package develop.x.core.blockingqueue.annotation;

import develop.x.core.blockingqueue.XBlockingQueue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface XBlockingQueueMapping {
    Class<? extends XBlockingQueue<?>> value();
}

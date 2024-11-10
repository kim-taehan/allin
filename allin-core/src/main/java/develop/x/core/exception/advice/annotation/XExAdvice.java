package develop.x.core.exception.advice.annotation;

import develop.x.core.exception.advice.ExAdviceOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface XExAdvice {
    ExAdviceOrder value() default ExAdviceOrder.BUSINESS;
}

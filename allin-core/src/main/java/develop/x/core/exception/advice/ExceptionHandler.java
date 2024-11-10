package develop.x.core.exception.advice;

import java.lang.reflect.Method;
import java.util.Objects;

public record ExceptionHandler(Class<? extends Throwable> throwable, Object bean, Method method, ExAdviceOrder order) {

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ExceptionHandler that = (ExceptionHandler) object;
        return Objects.equals(throwable, that.throwable) && order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(throwable, order);
    }

    @Override
    public String toString() {
        return "ExceptionHandler{" +
                "throwable=" + throwable.getSimpleName() +
                ", bean=" + bean.getClass().getSimpleName() +
                ", method=" + method.getName() +
                ", order=" + order.name() +
                '}';
    }
}

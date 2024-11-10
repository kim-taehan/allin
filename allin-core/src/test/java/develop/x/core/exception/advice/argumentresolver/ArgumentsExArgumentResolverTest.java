package develop.x.core.exception.advice.argumentresolver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static develop.x.core.exception.advice.argumentresolver.ArgumentResolverUtils.findMethod;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ArgumentsExArgumentResolverTest {

    ArgumentsExArgumentResolver argumentResolver = new ArgumentsExArgumentResolver();

    @Test
    @DisplayName("Object Array type 만 러리가 가능한 형태이다.")
    void supportObjectArrayType() {
        Method method = findMethod(ObjectService.class, "method");
        assertAll(
                () -> assertThat(argumentResolver.support(method.getParameters()[0])).isTrue(),
                () -> assertThat(argumentResolver.support(method.getParameters()[1])).isFalse()
        );
    }


    static abstract class ObjectService {
        public abstract void method(Object[] objectArray, List<Object> objectList);
    }
}
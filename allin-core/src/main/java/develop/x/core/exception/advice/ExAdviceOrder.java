package develop.x.core.exception.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ExAdviceOrder {
    SYSTEM(3), BUSINESS(2), FIRST(1),
    ;

    @Getter
    private final int index;
}

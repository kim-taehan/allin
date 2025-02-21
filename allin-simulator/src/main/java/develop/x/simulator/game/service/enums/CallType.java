package develop.x.simulator.game.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallType {
    SINGLE("1회 호출"), MULTI("N번 호출");
    private final String text;
}

package develop.x.simulator.game.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AgencyInputType {
    AUTO("자동입력"),
    MANUAL("수동입력");

    private final String text;
}

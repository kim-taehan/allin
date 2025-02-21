package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.CallType;
import develop.x.simulator.game.service.enums.Target;
import lombok.Builder;

@Builder
public record CallMethod(
        Target target,
        CallType callType,
        Integer count,
        Integer delay
) {
    public static CallMethod init() {
        return CallMethod.builder()
                .target(Target.MCI)
                .callType(CallType.SINGLE)
                .count(1000)
                .delay(3)
                .build();
    }
}

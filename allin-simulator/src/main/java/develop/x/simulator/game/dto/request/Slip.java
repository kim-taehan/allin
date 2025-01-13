package develop.x.simulator.game.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Slip {
    private final String eventId;
    private final LLOption option;
}

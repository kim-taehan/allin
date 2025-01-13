package develop.x.simulator.game.dto.request;

import develop.x.simulator.network.target.Target;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class LLBuyRequest {

    private final Target target;

    // game id (24, 25, ...)
    private final String gameId = "24";

    private final String programNo;

    private final int round;

    private final String vendingMachine;

    private final Collection<Slip> slips;
}

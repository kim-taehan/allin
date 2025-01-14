package develop.x.betting.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class OrderLLRequest {

    // game id (24, 25, ...)
    private final String gameId = "24";

    private final String programNo;

    private final int round;

    private final String vendingMachine;

}

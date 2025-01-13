package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.request.LLBuyRequest;

public interface GameService {
    boolean buy(LLBuyRequest request);
}

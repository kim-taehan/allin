package develop.x.simulator.game.controller;

import develop.x.simulator.game.dto.request.LLBuyRequest;
import develop.x.simulator.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    @PostMapping
    public String buy(LLBuyRequest request) {
        boolean sendRequest = gameService.buy(request);

        return "ok";
    }
}

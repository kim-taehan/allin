package develop.x.simulator.game.controller;

import develop.x.simulator.game.LLEvent;
import develop.x.simulator.game.LLEventProvider;
import develop.x.simulator.game.dto.request.LLBuyRequest;
import develop.x.simulator.game.service.GameService;
import develop.x.simulator.network.target.Target;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    private final LLEventProvider llEventProvider;

    @GetMapping("/games/ll")
    public String renderingGamesLl(Model model, @ModelAttribute LLBuyRequest request) {
        model.addAttribute("request", request);
        return "games/ll";
    }

    @GetMapping("/games/wl")
    public String renderingGamesWl(Model model, @ModelAttribute LLBuyRequest request) {
        model.addAttribute("request", request);
        return "games/wl";
    }

    @PostMapping("/games/ll")
    public String buyGameLl(Model model, @ModelAttribute LLBuyRequest request, RedirectAttributes redirectAttributes) {
        log.info("request = {}", request);
        return "redirect:/games/ll";
    }

    /**
     * enum
     */
    @ModelAttribute("targets")
    public Target[] stages() {
        return Target.values();
    }

    @ModelAttribute("llEvents")
    public LLEvent[] llEvents() {
        return llEventProvider.get();
    }
}

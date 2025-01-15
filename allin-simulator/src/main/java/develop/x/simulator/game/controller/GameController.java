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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    private final LLEventProvider llEventProvider;

    @GetMapping("/games/ll")
    public String renderingGamesLl(Model model) {

        if (model.containsAttribute("request")) {
            model.addAttribute("request", model.asMap().get("request"));
        } else {
            model.addAttribute("request", LLBuyRequest.builder().build()); // 빈 객체로 초기화
        }
        return "games/ll";
    }


    @PostMapping("/games/ll")
    public String buyGameLl(Model model, @ModelAttribute @Validated LLBuyRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("request = {}", request);
        if (bindingResult.hasErrors()) {
            // 첫 번째 에러 메시지 가져오기
            String errorMessage = null;
            if (bindingResult.getFieldError() != null) {
                errorMessage = bindingResult.getFieldError().getDefaultMessage();
            }

            if (errorMessage != null) {
                // 에러 메시지 전달 (RedirectAttributes 활용)
                redirectAttributes.addFlashAttribute("alertMessage", errorMessage);
                redirectAttributes.addFlashAttribute("request", request); // 폼 데이터도 유지
                return "redirect:/games/ll"; // 리다이렉트
            }
        }

        gameService.buy(request);
        redirectAttributes.addFlashAttribute("request", request); // 폼 데이터도 유지
        redirectAttributes.addFlashAttribute("alertMessage", "정상적으로 요청하였습니다.");

        return "redirect:/games/ll"; // 리다이렉트
    }



    @GetMapping("/games/wl")
    public String renderingGamesWl(Model model, @ModelAttribute LLBuyRequest request) {
        model.addAttribute("request", request);
        return "games/wl";
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

package develop.x.simulator.game.controller;

import develop.x.simulator.game.dto.CancelRequest;
import develop.x.simulator.game.service.enums.CallType;
import develop.x.simulator.game.service.enums.CancelOption;
import develop.x.simulator.game.service.enums.Target;
import develop.x.simulator.game.service.CancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("api/cancel")
@RequiredArgsConstructor
public class CancelController {

    private final CancelService cancelService;

    @GetMapping
    public String render(Model model) {

        if (model.containsAttribute("request")) {
            model.addAttribute("request", model.asMap().get("request"));
        } else {
            model.addAttribute("request", CancelRequest.init()); // 빈 객체로 초기화
        }
        return "api/cancel";
    }

    @PostMapping("findTicket")
    public String findTicket(Model model, @ModelAttribute CancelRequest request, RedirectAttributes redirectAttributes) {

        // Todo find data
        CancelRequest updateRequest = cancelService.findTicket(request);
        redirectAttributes.addFlashAttribute("request", updateRequest);
        return "redirect:/api/cancel";
    }

    @ModelAttribute("cancelOptions")
    public CancelOption[] cancelOptions() {
        return CancelOption.values();
    }

    @ModelAttribute("callTypes")
    public CallType[] callTypes() {
        return CallType.values();
    }

    @ModelAttribute("targets")
    public Target[] targets() {
        return Target.values();
    }
}

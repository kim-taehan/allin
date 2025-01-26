package develop.x.simulator.game.controller;

import develop.x.simulator.game.dto.BankRequest;
import develop.x.simulator.game.dto.CancelRequest;
import develop.x.simulator.game.enums.BankOption;
import develop.x.simulator.game.enums.CallType;
import develop.x.simulator.game.enums.CancelOption;
import develop.x.simulator.game.enums.Target;
import develop.x.simulator.game.service.BankService;
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
@RequestMapping("api/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping
    public String render(Model model) {

        if (model.containsAttribute("request")) {
            model.addAttribute("request", model.asMap().get("request"));
        } else {
            model.addAttribute("request", BankRequest.init()); // 빈 객체로 초기화
        }
        return "api/bank";
    }

    @PostMapping("findTicket")
    public String findTicket(Model model, @ModelAttribute BankRequest request, RedirectAttributes redirectAttributes) {

        // Todo find data
        BankRequest updateRequest = bankService.findTicket(request);
        redirectAttributes.addFlashAttribute("request", updateRequest);
        return "redirect:/api/bank";
    }


    @ModelAttribute("bankOptions")
    public BankOption[] bankOptions() {
        return BankOption.values();
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

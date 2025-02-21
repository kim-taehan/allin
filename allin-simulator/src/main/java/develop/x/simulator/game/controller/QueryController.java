package develop.x.simulator.game.controller;

import develop.x.simulator.game.dto.QueryRequest;
import develop.x.simulator.game.service.enums.CallType;
import develop.x.simulator.game.service.enums.QueryOption;
import develop.x.simulator.game.service.enums.Target;
import develop.x.simulator.game.service.QueryService;
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
@RequestMapping("api/query")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @GetMapping
    public String render(Model model) {

        if (model.containsAttribute("request")) {
            model.addAttribute("request", model.asMap().get("request"));
        } else {
            model.addAttribute("request", QueryRequest.init()); // 빈 객체로 초기화
        }
        return "api/query";
    }

    @PostMapping("findTicket")
    public String findTicket(Model model, @ModelAttribute QueryRequest request, RedirectAttributes redirectAttributes) {

        // Todo find data
        QueryRequest updateRequest = queryService.findTicket(request);
        redirectAttributes.addFlashAttribute("request", updateRequest);
        return "redirect:/api/query";
    }

    @ModelAttribute("callTypes")
    public CallType[] callTypes() {
        return CallType.values();
    }

    @ModelAttribute("targets")
    public Target[] targets() {
        return Target.values();
    }

    @ModelAttribute("queryOptions")
    public QueryOption[] queryOptions() {
        return QueryOption.values();
    }
}

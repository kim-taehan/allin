package develop.x.simulator.game.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankOption {
    QUERY("조회"),
    REIMBURSEMENT("환급");
    private final String text;
}

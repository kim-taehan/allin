package develop.x.simulator.game.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelOption {
    CANCEL("취소"),
    REFUND("환불"),
    REIMBURSEMENT("환급");
    private final String text;
}

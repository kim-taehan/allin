package develop.x.simulator.game.service.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @Getter 를 보유한 trivial enum 5종(AgencyInputType, BankOption, CallType, CancelOption)의
 * 상수 개수와 getText() 텍스트 매핑을 통합 검증한다. (과투자 방지를 위해 1클래스로 통합)
 * QueryOption / Target 은 비자명 포인트가 있어 별도 테스트 클래스로 분리.
 */
class EnumTextMappingTest {

    @Test
    @DisplayName("AgencyInputType 은 AUTO/MANUAL 2종이며 getText() 가 매핑된다.")
    void agencyInputType() {
        assertThat(AgencyInputType.values()).hasSize(2);
        assertThat(AgencyInputType.AUTO.getText()).isEqualTo("자동입력");
        assertThat(AgencyInputType.MANUAL.getText()).isEqualTo("수동입력");
    }

    @Test
    @DisplayName("BankOption 은 QUERY/REIMBURSEMENT 2종이며 getText() 가 매핑된다.")
    void bankOption() {
        assertThat(BankOption.values()).hasSize(2);
        assertThat(BankOption.QUERY.getText()).isEqualTo("조회");
        assertThat(BankOption.REIMBURSEMENT.getText()).isEqualTo("환급");
    }

    @Test
    @DisplayName("CallType 은 SINGLE/MULTI 2종이며 getText() 가 매핑된다.")
    void callType() {
        assertThat(CallType.values()).hasSize(2);
        assertThat(CallType.SINGLE.getText()).isEqualTo("1회 호출");
        assertThat(CallType.MULTI.getText()).isEqualTo("N번 호출");
    }

    @Test
    @DisplayName("CancelOption 은 CANCEL/REFUND/REIMBURSEMENT 3종이며 getText() 가 매핑된다.")
    void cancelOption() {
        assertThat(CancelOption.values()).hasSize(3);
        assertThat(CancelOption.CANCEL.getText()).isEqualTo("취소");
        assertThat(CancelOption.REFUND.getText()).isEqualTo("환불");
        assertThat(CancelOption.REIMBURSEMENT.getText()).isEqualTo("환급");
    }
}

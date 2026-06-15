package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.BankRequest;
import develop.x.simulator.game.dto.CallMethod;
import develop.x.simulator.game.dto.Shop;
import develop.x.simulator.game.service.enums.BankOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization test - BankService.findTicket 은 현재 DB 조회 없이
 * 하드코딩 상수(winnings=100000)로 갱신하는 스텁(TODO)이다.
 * 향후 DB 연동 시 의도적으로 깨지며 변경을 알려주는 안전망 역할.
 */
class BankServiceTest {

    private final BankService bankService = new BankService();

    @Test
    @DisplayName("findTicket() 은 winnings 를 하드코딩 100000 으로 갱신한다.(스텁 고정)")
    void findTicket_returnsWinnings100000() {
        // given
        BankRequest request = BankRequest.init();

        // when
        BankRequest result = bankService.findTicket(request);

        // then
        assertThat(result.winnings()).isEqualTo(100000);
    }

    @Test
    @DisplayName("findTicket() 은 winnings 외 ticketNumber/option/shop/callMethod 를 입력 그대로 보존한다.")
    void findTicket_preservesOtherFields() {
        // given
        Shop shop = Shop.init();
        CallMethod callMethod = CallMethod.init();
        BankRequest request = BankRequest.builder()
                .ticketNumber("TICKET")
                .option(BankOption.REIMBURSEMENT)
                .winnings(1)
                .shop(shop)
                .callMethod(callMethod)
                .build();

        // when
        BankRequest result = bankService.findTicket(request);

        // then
        assertThat(result.ticketNumber()).isEqualTo("TICKET");
        assertThat(result.option()).isEqualTo(BankOption.REIMBURSEMENT);
        assertThat(result.shop()).isEqualTo(shop);
        assertThat(result.callMethod()).isEqualTo(callMethod);
    }

    @Test
    @DisplayName("findTicket() 은 입력 record 를 변경하지 않는다.(record 불변 회귀 가드)")
    void findTicket_doesNotMutateInput() {
        // given
        BankRequest request = BankRequest.init();

        // when
        bankService.findTicket(request);

        // then
        assertThat(request.winnings()).isEqualTo(0);
    }
}

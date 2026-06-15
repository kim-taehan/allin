package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.CallMethod;
import develop.x.simulator.game.dto.CancelRequest;
import develop.x.simulator.game.dto.Shop;
import develop.x.simulator.game.service.enums.CancelOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization test - CancelService.findTicket 은 현재 DB 조회 없이
 * 하드코딩 상수("24","1000","16024","16")로 갱신하는 스텁(TODO)이다.
 */
class CancelServiceTest {

    private final CancelService cancelService = new CancelService();

    @Test
    @DisplayName("findTicket() 은 gameId/bettingMoney/programNum/programTs 를 하드코딩 상수로 갱신한다.(스텁 고정)")
    void findTicket_returnsHardcodedTicketInfo() {
        // given
        CancelRequest request = CancelRequest.init();

        // when
        CancelRequest result = cancelService.findTicket(request);

        // then
        assertThat(result.gameId()).isEqualTo("24");
        assertThat(result.bettingMoney()).isEqualTo("1000");
        assertThat(result.programNum()).isEqualTo("16024");
        assertThat(result.programTs()).isEqualTo("16");
    }

    @Test
    @DisplayName("findTicket() 은 ticketNumber/option/shop/callMethod 를 입력 그대로 보존한다.")
    void findTicket_preservesTicketNumberOptionShopCallMethod() {
        // given
        Shop shop = Shop.init();
        CallMethod callMethod = CallMethod.init();
        CancelRequest request = CancelRequest.builder()
                .ticketNumber("TICKET")
                .option(CancelOption.REFUND)
                .gameId("x")
                .bettingMoney("x")
                .programNum("x")
                .programTs("x")
                .shop(shop)
                .callMethod(callMethod)
                .build();

        // when
        CancelRequest result = cancelService.findTicket(request);

        // then
        assertThat(result.ticketNumber()).isEqualTo("TICKET");
        assertThat(result.option()).isEqualTo(CancelOption.REFUND);
        assertThat(result.shop()).isEqualTo(shop);
        assertThat(result.callMethod()).isEqualTo(callMethod);
    }
}

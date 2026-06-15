package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.CallMethod;
import develop.x.simulator.game.dto.QueryRequest;
import develop.x.simulator.game.dto.Shop;
import develop.x.simulator.game.service.enums.QueryOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization test - QueryService.findTicket 은 현재 DB 조회 없이
 * 하드코딩 상수("24","2025","11","100","101")로 갱신하는 스텁(TODO)이다.
 */
class QueryServiceTest {

    private final QueryService queryService = new QueryService();

    @Test
    @DisplayName("findTicket() 은 gameId/programTsYear/programTs/programDtlTs/evntNum 를 하드코딩 상수로 갱신한다.(스텁 고정)")
    void findTicket_returnsHardcodedTicketInfo() {
        // given
        QueryRequest request = QueryRequest.init();

        // when
        QueryRequest result = queryService.findTicket(request);

        // then
        assertThat(result.gameId()).isEqualTo("24");
        assertThat(result.programTsYear()).isEqualTo("2025");
        assertThat(result.programTs()).isEqualTo("11");
        assertThat(result.programDtlTs()).isEqualTo("100");
        assertThat(result.evntNum()).isEqualTo("101");
    }

    @Test
    @DisplayName("findTicket() 은 ticketNumber/option/shop/callMethod 를 입력 그대로 보존한다.")
    void findTicket_preservesTicketNumberOptionShopCallMethod() {
        // given
        Shop shop = Shop.init();
        CallMethod callMethod = CallMethod.init();
        QueryRequest request = QueryRequest.builder()
                .ticketNumber("TICKET")
                .option(QueryOption.A008)
                .gameId("x")
                .programTsYear("x")
                .programTs("x")
                .programDtlTs("x")
                .evntNum("x")
                .shop(shop)
                .callMethod(callMethod)
                .build();

        // when
        QueryRequest result = queryService.findTicket(request);

        // then
        assertThat(result.ticketNumber()).isEqualTo("TICKET");
        assertThat(result.option()).isEqualTo(QueryOption.A008);
        assertThat(result.shop()).isEqualTo(shop);
        assertThat(result.callMethod()).isEqualTo(callMethod);
    }
}

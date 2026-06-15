package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.QueryOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRequestTest {

    @Test
    @DisplayName("init() 은 기본값(option=A001, String 5개=\"\", shop/callMethod init)으로 생성한다.")
    void init_setsDefaults() {
        // when
        QueryRequest request = QueryRequest.init();

        // then
        assertThat(request.ticketNumber()).isEmpty();
        assertThat(request.option()).isEqualTo(QueryOption.A001);
        assertThat(request.gameId()).isEmpty();
        assertThat(request.programTsYear()).isEmpty();
        assertThat(request.programTs()).isEmpty();
        assertThat(request.programDtlTs()).isEmpty();
        assertThat(request.evntNum()).isEmpty();
        assertThat(request.shop()).isEqualTo(Shop.init());
        assertThat(request.callMethod()).isEqualTo(CallMethod.init());
    }

    @Test
    @DisplayName("updateTicketInfo() 는 5개 인자를 정확한 필드에 위치-매핑한다.(인자 순서 어긋남 회귀)")
    void updateTicketInfo_mapsFiveArgsByPosition() {
        // given - 5개 인자에 서로 구별 가능한 값을 부여
        QueryRequest origin = QueryRequest.init();

        // when
        QueryRequest updated = origin.updateTicketInfo("G", "Y", "TS", "DTL", "EV");

        // then - 각 인자가 시그니처 순서(gameId, programTsYear, programTs, programDtlTs, evntNum)대로 매핑
        assertThat(updated.gameId()).isEqualTo("G");
        assertThat(updated.programTsYear()).isEqualTo("Y");
        assertThat(updated.programTs()).isEqualTo("TS");
        assertThat(updated.programDtlTs()).isEqualTo("DTL");
        assertThat(updated.evntNum()).isEqualTo("EV");
    }

    @Test
    @DisplayName("updateTicketInfo() 는 ticketNumber/option/shop/callMethod 를 원본 그대로 보존한다.")
    void updateTicketInfo_preservesUnrelatedFields() {
        // given - 보존 대상이 init 기본값과 구별되도록 별도 값으로 구성
        Shop shop = Shop.builder()
                .agencyInputType(develop.x.simulator.game.service.enums.AgencyInputType.MANUAL)
                .agencyId("agency-X")
                .shopId("SHOP")
                .tagId("TAG")
                .build();
        CallMethod callMethod = CallMethod.builder()
                .target(develop.x.simulator.game.service.enums.Target.LOCALHOST)
                .callType(develop.x.simulator.game.service.enums.CallType.MULTI)
                .count(5)
                .delay(7)
                .build();
        QueryRequest origin = QueryRequest.builder()
                .ticketNumber("TICKET")
                .option(QueryOption.A008)
                .gameId("old-g")
                .programTsYear("old-y")
                .programTs("old-ts")
                .programDtlTs("old-dtl")
                .evntNum("old-ev")
                .shop(shop)
                .callMethod(callMethod)
                .build();

        // when
        QueryRequest updated = origin.updateTicketInfo("G", "Y", "TS", "DTL", "EV");

        // then
        assertThat(updated.ticketNumber()).isEqualTo("TICKET");
        assertThat(updated.option()).isEqualTo(QueryOption.A008);
        assertThat(updated.shop()).isEqualTo(shop);
        assertThat(updated.callMethod()).isEqualTo(callMethod);
    }

    @Test
    @DisplayName("updateTicketInfo() 는 새 인스턴스를 반환하고 원본 record 는 불변이다.")
    void updateTicketInfo_returnsNewInstance_originalUnchanged() {
        // given
        QueryRequest origin = QueryRequest.init();

        // when
        QueryRequest updated = origin.updateTicketInfo("G", "Y", "TS", "DTL", "EV");

        // then
        assertThat(updated).isNotSameAs(origin);
        assertThat(origin.gameId()).isEmpty();
        assertThat(origin.evntNum()).isEmpty();
    }
}

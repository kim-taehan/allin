package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.AgencyInputType;
import develop.x.simulator.game.service.enums.CallType;
import develop.x.simulator.game.service.enums.CancelOption;
import develop.x.simulator.game.service.enums.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CancelRequestTest {

    @Test
    @DisplayName("init() 은 기본값(option=CANCEL, String 5개=\"\", shop/callMethod init)으로 생성한다.")
    void init_setsDefaults() {
        // when
        CancelRequest request = CancelRequest.init();

        // then
        assertThat(request.ticketNumber()).isEmpty();
        assertThat(request.option()).isEqualTo(CancelOption.CANCEL);
        assertThat(request.gameId()).isEmpty();
        assertThat(request.bettingMoney()).isEmpty();
        assertThat(request.programNum()).isEmpty();
        assertThat(request.programTs()).isEmpty();
        assertThat(request.shop()).isEqualTo(Shop.init());
        assertThat(request.callMethod()).isEqualTo(CallMethod.init());
    }

    @Test
    @DisplayName("updateTicketInfo() 는 4개 인자를 정확한 필드에 위치-매핑한다.(인자 순서 어긋남 회귀)")
    void updateTicketInfo_mapsArgsByPosition() {
        // given - 4개 인자에 서로 구별 가능한 값을 부여
        CancelRequest origin = CancelRequest.init();

        // when
        CancelRequest updated = origin.updateTicketInfo("G", "M", "N", "T");

        // then - 시그니처 순서(gameId, bettingMoney, programNum, programTs)대로 매핑
        assertThat(updated.gameId()).isEqualTo("G");
        assertThat(updated.bettingMoney()).isEqualTo("M");
        assertThat(updated.programNum()).isEqualTo("N");
        assertThat(updated.programTs()).isEqualTo("T");
    }

    @Test
    @DisplayName("updateTicketInfo() 는 ticketNumber/option/shop/callMethod 를 원본 그대로 보존한다.")
    void updateTicketInfo_preservesUnrelatedFields() {
        // given
        Shop shop = Shop.builder()
                .agencyInputType(AgencyInputType.MANUAL)
                .agencyId("agency-X")
                .shopId("SHOP")
                .tagId("TAG")
                .build();
        CallMethod callMethod = CallMethod.builder()
                .target(Target.LOCALHOST)
                .callType(CallType.MULTI)
                .count(5)
                .delay(7)
                .build();
        CancelRequest origin = CancelRequest.builder()
                .ticketNumber("TICKET")
                .option(CancelOption.REFUND)
                .gameId("old-g")
                .bettingMoney("old-m")
                .programNum("old-n")
                .programTs("old-t")
                .shop(shop)
                .callMethod(callMethod)
                .build();

        // when
        CancelRequest updated = origin.updateTicketInfo("G", "M", "N", "T");

        // then
        assertThat(updated.ticketNumber()).isEqualTo("TICKET");
        assertThat(updated.option()).isEqualTo(CancelOption.REFUND);
        assertThat(updated.shop()).isEqualTo(shop);
        assertThat(updated.callMethod()).isEqualTo(callMethod);
    }

    @Test
    @DisplayName("updateTicketInfo() 는 새 인스턴스를 반환하고 원본 record 는 불변이다.")
    void updateTicketInfo_returnsNewInstance_originalUnchanged() {
        // given
        CancelRequest origin = CancelRequest.init();

        // when
        CancelRequest updated = origin.updateTicketInfo("G", "M", "N", "T");

        // then
        assertThat(updated).isNotSameAs(origin);
        assertThat(origin.gameId()).isEmpty();
        assertThat(origin.programTs()).isEmpty();
    }
}

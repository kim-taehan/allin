package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.AgencyInputType;
import develop.x.simulator.game.service.enums.BankOption;
import develop.x.simulator.game.service.enums.CallType;
import develop.x.simulator.game.service.enums.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankRequestTest {

    @Test
    @DisplayName("init() 은 기본값(ticketNumber=\"\", option=QUERY, winnings=0, shop/callMethod init)으로 생성한다.")
    void init_setsDefaults() {
        // when
        BankRequest request = BankRequest.init();

        // then
        assertThat(request.ticketNumber()).isEmpty();
        assertThat(request.option()).isEqualTo(BankOption.QUERY);
        assertThat(request.winnings()).isEqualTo(0);
        assertThat(request.shop()).isEqualTo(Shop.init());
        assertThat(request.callMethod()).isEqualTo(CallMethod.init());
    }

    @Test
    @DisplayName("updateTicketInfo() 는 winnings 만 교체하고 나머지 4필드를 원본 그대로 보존한다.")
    void updateTicketInfo_replacesOnlyWinnings() {
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
        BankRequest origin = BankRequest.builder()
                .ticketNumber("TICKET")
                .option(BankOption.REIMBURSEMENT)
                .winnings(123)
                .shop(shop)
                .callMethod(callMethod)
                .build();

        // when
        BankRequest updated = origin.updateTicketInfo(99999);

        // then
        assertThat(updated.winnings()).isEqualTo(99999);
        assertThat(updated.ticketNumber()).isEqualTo("TICKET");
        assertThat(updated.option()).isEqualTo(BankOption.REIMBURSEMENT);
        assertThat(updated.shop()).isEqualTo(shop);
        assertThat(updated.callMethod()).isEqualTo(callMethod);
    }

    @Test
    @DisplayName("updateTicketInfo() 는 새 인스턴스를 반환하고 원본 record 는 불변이다.")
    void updateTicketInfo_returnsNewInstance_originalUnchanged() {
        // given
        BankRequest origin = BankRequest.init();

        // when
        BankRequest updated = origin.updateTicketInfo(99999);

        // then
        assertThat(updated).isNotSameAs(origin);
        assertThat(origin.winnings()).isEqualTo(0);
    }
}

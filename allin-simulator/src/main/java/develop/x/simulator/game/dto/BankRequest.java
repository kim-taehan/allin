package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.BankOption;
import lombok.Builder;
import org.apache.logging.log4j.util.Strings;

@Builder
public record BankRequest(
        String ticketNumber,
        BankOption option,
        Integer winnings,
        Shop shop,
        CallMethod callMethod
) {
    public static BankRequest init() {
        return BankRequest.builder()
                .ticketNumber(Strings.EMPTY)
                .option(BankOption.QUERY)
                .winnings(0)
                .shop(Shop.init())
                .callMethod(CallMethod.init())
                .build();
    }

    public BankRequest updateTicketInfo(Integer winnings) {
        return BankRequest.builder()
                .ticketNumber(this.ticketNumber)
                .option(this.option)
                .winnings(winnings)
                .shop(this.shop)
                .callMethod(this.callMethod)
                .build();
    }
}

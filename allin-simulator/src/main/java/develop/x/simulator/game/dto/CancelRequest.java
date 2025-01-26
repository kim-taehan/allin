package develop.x.simulator.game.dto;

import develop.x.simulator.game.enums.CancelOption;
import lombok.Builder;
import org.apache.logging.log4j.util.Strings;

@Builder
public record CancelRequest(
        String ticketNumber,
        CancelOption option,
        String gameId,
        String bettingMoney,
        String programNum,
        String programTs,
        Shop shop,
        CallMethod callMethod
) {
    public static CancelRequest init() {
        return CancelRequest.builder()
                .ticketNumber(Strings.EMPTY)
                .option(CancelOption.CANCEL)
                .gameId(Strings.EMPTY)
                .bettingMoney(Strings.EMPTY)
                .programNum(Strings.EMPTY)
                .programTs(Strings.EMPTY)
                .shop(Shop.init())
                .callMethod(CallMethod.init())
                .build();
    }

    public CancelRequest updateTicketInfo(String gameId, String bettingMoney, String programNum, String programTs) {
        return CancelRequest.builder()
                .ticketNumber(this.ticketNumber)
                .option(this.option)
                .gameId(gameId)
                .bettingMoney(bettingMoney)
                .programNum(programNum)
                .programTs(programTs)
                .shop(this.shop)
                .callMethod(this.callMethod)
                .build();
    }
}

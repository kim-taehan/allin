package develop.x.simulator.game.dto;

import develop.x.simulator.game.enums.QueryOption;
import lombok.Builder;
import org.apache.logging.log4j.util.Strings;

@Builder
public record QueryRequest(
        String ticketNumber,
        QueryOption option,
        String gameId,
        String programTsYear,
        String programTs,
        String programDtlTs,
        String evntNum,
        Shop shop,
        CallMethod callMethod
) {
    public static QueryRequest init() {
        return QueryRequest.builder()
                .ticketNumber(Strings.EMPTY)
                .option(QueryOption.A001)
                .gameId(Strings.EMPTY)
                .programTsYear(Strings.EMPTY)
                .programTs(Strings.EMPTY)
                .programDtlTs(Strings.EMPTY)
                .evntNum(Strings.EMPTY)
                .shop(Shop.init())
                .callMethod(CallMethod.init())
                .build();
    }

    public QueryRequest updateTicketInfo(String gameId, String programTsYear, String programTs, String programDtlTs, String evntNum) {
        return QueryRequest.builder()
                .ticketNumber(this.ticketNumber)
                .option(this.option)
                .gameId(gameId)
                .programTsYear(programTsYear)
                .programTs(programTs)
                .programDtlTs(programDtlTs)
                .evntNum(evntNum)
                .shop(this.shop)
                .callMethod(this.callMethod)
                .build();
    }
}

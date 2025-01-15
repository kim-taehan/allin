package develop.x.simulator.game.dto.request;

import develop.x.simulator.game.LLEvent;
import develop.x.simulator.network.target.Target;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Builder
public record LLBuyRequest(
    Target target,
    String gameId,
    @NotNull
    Integer amount,
    String programNo,
    Integer round,
    String vendingMachine,
    List<String> eventIds, // eventId 리스트
    List<String> options
) {

    // 명시적인 생성자 정의
    public LLBuyRequest {
        // 생성자에서 유효성 검사 추가 가능
        if (eventIds == null  ) {
            eventIds = new ArrayList<>();
        }
        if (options == null  ) {
            options = new ArrayList<>();
        }
    }
}

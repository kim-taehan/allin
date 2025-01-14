package develop.x.simulator.game.dto.request;

import develop.x.simulator.game.LLEvent;
import develop.x.simulator.network.target.Target;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Data
public class LLBuyRequest {
        Target target;
        String gameId;
        Integer amount;
        String programNo;
        Integer round;
        String vendingMachine;

        List<String> eventIds; // eventId 리스트
        List<String> options;  // 옵션 리스트
      List<LLEvent> events;
 }

package develop.x.simulator.game;

import org.springframework.stereotype.Component;

@Component
public class LLEventProvider {

    // todo db 조회로 변경 필요

    public LLEvent[] get() {

        return new LLEvent[] {
                new LLEvent("한국:일본(일반)", "A0001", LLType.WDL),
                new LLEvent("한국:일본(홀짝)", "A0002", LLType.SNIFFLING),
                new LLEvent("대구삼성:광주기아(일반)", "B0001", LLType.WDL),
                new LLEvent("대구삼성:광주기아(핸디캡)", "B0002", LLType.HANDICAP),
        };

    }
}

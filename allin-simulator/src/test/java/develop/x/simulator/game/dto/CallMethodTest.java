package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.CallType;
import develop.x.simulator.game.service.enums.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallMethodTest {

    @Test
    @DisplayName("init() 은 기본값(target=MCI, callType=SINGLE, count=1000, delay=3)으로 생성한다.")
    void init_setsDefaults() {
        // when
        CallMethod callMethod = CallMethod.init();

        // then
        assertThat(callMethod.target()).isEqualTo(Target.MCI);
        assertThat(callMethod.callType()).isEqualTo(CallType.SINGLE);
        assertThat(callMethod.count()).isEqualTo(1000);
        assertThat(callMethod.delay()).isEqualTo(3);
    }
}

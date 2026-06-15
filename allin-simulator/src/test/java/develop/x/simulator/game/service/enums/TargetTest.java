package develop.x.simulator.game.service.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Target 은 protocol/host/port 필드가 private 이고 getter 가 없다.
 * → 필드값(127.0.0.1, 3333 등) 검증은 캡슐화상 불가하다.
 *   테스트가 캡슐화를 깨지 않도록 reflection 검증은 하지 않고,
 *   values()/name() 수준의 상수 구성만 단언한다. (한계 명시)
 */
class TargetTest {

    @Test
    @DisplayName("Target 은 MCI/LOCALHOST 2개 상수를 가진다.")
    void hasTwoConstants() {
        assertThat(Target.values()).hasSize(2);
        assertThat(Target.valueOf("MCI")).isEqualTo(Target.MCI);
        assertThat(Target.valueOf("LOCALHOST")).isEqualTo(Target.LOCALHOST);
    }
}

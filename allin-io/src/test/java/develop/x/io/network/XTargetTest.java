package develop.x.io.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XTargetTest {

    @Test
    @DisplayName("valueOf 는 유효한 enum 이름으로 해당 상수를 반환한다.")
    void valueOf() {
        assertThat(XTarget.valueOf("ORDER")).isEqualTo(XTarget.ORDER);
        assertThat(XTarget.valueOf("RISK")).isEqualTo(XTarget.RISK);
    }

    @Test
    @DisplayName("valueOf 는 존재하지 않는 이름이면 IllegalArgumentException 을 던진다.")
    void valueOfInvalid() {
        assertThatThrownBy(() -> XTarget.valueOf("dsf"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("명시 생성자를 사용한 ORDER 는 지정된 mapName/queueName 을 갖는다.")
    void orderMapAndQueueName() {
        assertThat(XTarget.ORDER.getMapName()).isEqualTo("MAP_ORDER");
        assertThat(XTarget.ORDER.getQueueName()).isEqualTo("MQ_ORDER");
    }

    @Test
    @DisplayName("기본 생성자를 사용한 RISK 는 이름 기반 mapName/queueName 을 갖는다.")
    void riskMapAndQueueName() {
        assertThat(XTarget.RISK.getMapName()).isEqualTo("MAP_RISK");
        assertThat(XTarget.RISK.getQueueName()).isEqualTo("MQ_RISK");
    }
}

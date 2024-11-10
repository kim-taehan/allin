package develop.x.io.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum XTarget {

    ORDER("MAP_ORDER", "MQ_ORDER"),
    RISK
    ;

    private final String mapName;
    private final String queueName;

    XTarget() {
        this.mapName = "MAP_" + this.name();
        this.queueName = "MQ_" + this.name();
    }
}

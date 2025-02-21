package develop.x.simulator.game.service.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Target {

    MCI(Protocol.TCP, "127.0.0.1", "3333"),
    LOCALHOST(Protocol.HTTP, "127.0.0.1", "8081")
    ;

    private final Protocol protocol;
    private final String host;
    private final String port;


    enum Protocol {
        TCP, HTTP, HAZELCAST
    }
}

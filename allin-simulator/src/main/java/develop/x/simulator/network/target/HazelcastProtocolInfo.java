package develop.x.simulator.network.target;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HazelcastProtocolInfo implements ProtocolInfo {

    private final String mapName;

    private final String queueName;
}

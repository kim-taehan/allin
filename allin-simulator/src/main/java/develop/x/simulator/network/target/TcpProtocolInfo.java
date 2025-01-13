package develop.x.simulator.network.target;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TcpProtocolInfo implements ProtocolInfo {

    private final String ip;

    private final String port;
}

package develop.x.simulator.network.target;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Target {
    MCI_A(Protocol.TCP, new TcpProtocolInfo("127.0.0.1", "9999")),
    MCI_B(Protocol.TCP, new TcpProtocolInfo("127.0.0.1", "9999")),
    ;

    private final Protocol protocol;
    private final ProtocolInfo protocolInfo;
}

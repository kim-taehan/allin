package develop.x.network.netty.receiver.properties;

public record NettyInfo(
        int bossCount,
        int workerCount,
        boolean keepAlive,
        int backlog
) {
}

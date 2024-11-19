package develop.x.network.netty.properties;

public record NettyInfo(
        int bossCount,
        int workerCount,
        boolean keepAlive,
        int backlog
) {
}

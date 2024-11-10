package develop.x.core;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public final class HazelcastFactory {

    private final HazelcastInstance hazelcastInstance;

    private HazelcastFactory(){
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("hello-world");
        hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
    }

    private static class SingleTonHolder {
        private static final HazelcastFactory INSTANCE = new HazelcastFactory();
    }

    public static HazelcastInstance getInstance(){
        return SingleTonHolder.INSTANCE.hazelcastInstance;
    }
}

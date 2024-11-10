package develop.x.core.fakeHazelcast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HazelcastInstance {

    public static final HazelcastInstance instance = new HazelcastInstance();
    public static HazelcastInstance getInstance() {
        return instance;
    }

    private Map<String, IMap> iMaps = new ConcurrentHashMap<>();
    private Map<String, IQueue> iQueueMap = new ConcurrentHashMap<>();

    public IMap getMap(String name) {
        if (!iMaps.containsKey(name)) {
            IMap<String, Object> newMap = new IMap<>();
            iMaps.put(name, newMap);
        }
        return iMaps.get(name);
    }

    public IQueue getQueue(String name) {
        if (!iQueueMap.containsKey(name)) {
            IQueue iQueue = new IQueue();
            iQueueMap.put(name, iQueue);
        }
        return iQueueMap.get(name);
    }


}

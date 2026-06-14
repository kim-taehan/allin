package develop.x.core.cache;


import com.hazelcast.map.IMap;

import java.util.Map;
import java.util.function.Supplier;

public class MultiServerCache<K, V> implements XWritableCache<K, V> {

    private final IMap<K, V> items;

    public MultiServerCache(IMap<K, V> items) {
        this.items = items;
    }

    @Override
    public V get(K key) {
        return this.items.get(key);
    }

    @Override
    public boolean containsKey(K key) {
        return this.items.containsKey(key);
    }

    @Override
    public void clearCache() {
        this.items.clear();
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> items) {
        this.items.putAll(items);
    }

    @Override
    public void put(K key, V value) {
        this.items.put(key, value);
    }

    /**
     * IMap.computeIfAbsent 는 키 파티션 소유 노드에서 키 단위 락으로 put 을 원자적으로 처리하지만,
     * 매핑 함수(supplier)는 호출 측 JVM 에서 실행된다. 따라서 클러스터 전역에서 supplier 가
     * 정확히 1회 실행된다는 보장은 없고, 두 노드가 동시에 supplier 를 실행할 수 있다(결과 put 은 한쪽만 반영).
     * supplier 가 부수효과 없는 순수 함수임을 전제로 사용한다.
     */
    @Override
    public V computeIfAbsent(K key, Supplier<? extends V> supplier) {
        return this.items.computeIfAbsent(key, k -> supplier.get());
    }
}

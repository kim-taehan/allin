package develop.x.core.cache;

import java.util.Collections;
import java.util.Map;

public non-sealed abstract class SingleServerReadOnlyCache<K, V> implements XCache<K, V> {

    private final Map<K, V> items;

    public SingleServerReadOnlyCache() {
        items = Collections.unmodifiableMap(initialize());
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
        throw new IllegalStateException("readonly cache 는 제거할 수 없습니다.");
    }

    @Override
    public int size() {
        return this.items.size();
    }


    protected abstract Map<K, V> initialize();

}
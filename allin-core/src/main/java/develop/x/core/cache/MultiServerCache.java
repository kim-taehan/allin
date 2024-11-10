package develop.x.core.cache;


import com.hazelcast.map.IMap;

import java.util.Map;

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
}

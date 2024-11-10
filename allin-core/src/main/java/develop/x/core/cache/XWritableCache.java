package develop.x.core.cache;

import java.util.Map;
import java.util.function.Supplier;

public non-sealed interface XWritableCache<K, V> extends XCache<K,V> {

    void putAll(Map<? extends K, ? extends V> items);

    void put(K key, V value);

    default V getIfAbsent(K key, Supplier<? extends V> supplier){
        V newValue = getOrDefault(key, supplier);
        put(key, newValue);
        return newValue;
    }
}

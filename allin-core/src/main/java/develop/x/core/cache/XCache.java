package develop.x.core.cache;

import java.util.function.Supplier;

public sealed interface XCache<K, V> permits SingleServerReadOnlyCache, XWritableCache {

    V get(K key);

    default V getOrDefault(K key, Supplier<? extends V> supplier){
        return containsKey(key) ? get(key) : supplier.get();
    }

    boolean containsKey(K key);

    void clearCache();

    int size();
}

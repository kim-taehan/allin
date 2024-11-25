package develop.x.core.cache;

import org.springframework.lang.Nullable;

import java.util.function.Supplier;

/**
 * cache 기능을 정의한 최상위 인터페이스이다.
 * 기능만 정의되어 있고, 하위 클래스에서 어떤 방식의 cache 를 사용하는지는 제어하지 않는다.
 *
 * <p> sealed ~ permits 을 통해서 구현할 수 있는 클래스를 제한하였다.
 * {@link SingleServerReadOnlyCache}, {@link XWritableCache} 에서만 구현 or 상속이 가능하면 다른 하위 클래스는 이들을 구현 or 상속하도록 한다.
 *
 * @author  kimtaehan
 * @see     XWritableCache
 * @see     SingleServerReadOnlyCache
 * @since 1.0.0
 * @param <K> cache 에서 사용하는 key 에 해당한다.
 * @param <V> cache 에서 사용하는 value 에 해당한다.
 */
public sealed interface XCache<K, V> permits SingleServerReadOnlyCache, XWritableCache {

    /**
     * cache 에 key 인자로 조회하여 value 데이터를 리턴하는 메서드이다.
     * 만약 key 해당하는 item 이 없는 경우 null 를 리턴한다.
     *
     * @param key - cache 에 저장된 데이터를 조회하기 위한 key 데이터
     * @return V cache 에 저장된 value 데이터를 전달한다.
     */
    @Nullable
    V get(K key);

    default V getOrDefault(K key, Supplier<? extends V> supplier){
        return containsKey(key) ? get(key) : supplier.get();
    }

    boolean containsKey(K key);

    void clearCache();

    int size();
}

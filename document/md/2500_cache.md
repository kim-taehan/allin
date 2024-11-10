
### 2.5 Cache
- allin 시스템 business model 특징중 하나로 Cache 데이터를 많이 사용하고 있다.
- 단일 서버, 멀티 서버 Cache 을 business model 에서 별도 작업없이 spring di container 등록 만으로 사용할 수 있게 지원한다.

#### 2.5.1 architecture
- `상속보다 구현으로` 내부적으로 사용되는 `ConcurrentHashMap`, hazelcast `IMap` 등을 상속이 아니 내부 객체로 가진다.
- cache write 기능은 `XWritableCache` 별도의 인터페이스로 정의함
- SingleServer : `ConcurrentHashMap` 를 사용하여 단일 서버에서 사용할 수 있는 cache 를 제공한다.
- multiServer : hazelcast `IMap` 을 사용하여 여러서버에서 같이 사용할 수 있는 cache 를 제공한다.

```mermaid
classDiagram
    XCache <|-- XWritableCache
    XCache <|.. SingleServerReadOnlyCache
    XWritableCache <|.. SingleServerCache
    XWritableCache <|.. MultiServerCache
    
    class XCache {
        + V get(K key)
        + boolean containsKey(key)
        + V getOrDefault(K key, Supplier<? extends V> supplier)
    }
    class XWritableCache {
        void put(K key, V value)
        void putAll(Map<? extends K, ? extends V> items)
        V getIfAbsent(K key, Supplier<? extends V> supplier)
    }
    class SingleServerCache {
        Map<K, V> items
    }
    class MultiServerCache {
        IMap<K, V> items
    }
    class SingleServerReadOnlyCache {
        ConcurrentHashMap<K, V> items
    }

    <<interface>> XCache
    <<interface>> XWritableCache
    <<abstract>> SingleServerCache
    <<abstract>> MultiServerCache
    <<abstract>> SingleServerReadOnlyCache
```` 

#### 2.5.2 XCache && XWritableCache
- Cache 데이터를 사용하는 행위와, 생산하는 기능을 나눠서 인터페이스를 정의 (ISP 인터페이스 분리 원칙)
- business 개발자는 abstract class 중에 하나를 상속하여 편하게 사용할 수 있다


#### 2.5.3 XCache dependency
| type      | name                      | extends| desc                                      |
|-----------|---------------------------| --|-------------------------------------------|
| interface | XCache                    | N/A| Cache 데이터를 사용하는 행위 정의                     |
| interface  | XWritableCache           | XCache| Cache 데이터를 생산하는 행위 정의                     |
| abstract  | SingleServerReadOnlyCache | XCache| Single server cache 이며, read 기능만 제공       |
| abstract  | SingleServerCache         | XWritableCache| Single server cache 이며, read, write 기능 제공 |
| abstract  | MultiServerCache          | XWritableCache| Multi server cache 이며, read, write 기능 제공  |


#### 2.5.4 SingleServerReadOnlyCache
- Single Server 에서 최초 1회만 등록후 변경 불가능한 cache 형태이다. 
- 비지니스 특성상 이런 cache 는 불변 데이터로 저장하고, 동시성 이슈를 없애기 위해 사용한다. 
- [테스트 코드](..%2F..%2Fallin-core%2Fsrc%2Ftest%2Fjava%2Fdevelop%2Fx%2Fcore%2Fcache%2FSingleServerReadOnlyCacheTest.java)
```java
public non-sealed abstract class SingleServerReadOnlyCache<K, V> implements XCache<K, V> {
 
    private final Map<K, V> items;

    public SingleServerReadOnlyCache() {
        // (1) 멀티 스레드 환경에서 이슈가 생기지 않도록 불변 객체로 만들어 사용한다.
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
        // (2) SingleServerReadOnlyCache는 clearCache 를 지원하지 않는다.
        throw new IllegalStateException("readonly cache 는 제거할 수 없습니다.");
    }

    // (3) 구현 클래스에게 초기화를 위임하는 방식을 사용한다.
    protected abstract Map<K, V> initialize();
}
```
- (1) 멀티 스레드 환경에서 이슈가 생기지 않도록 불변 객체로 만들어 사용한다.
- (2) SingleServerReadOnlyCache는 clearCache 를 지원하지 않는다.
- (3) 구현 클래스에게 초기화를 위임하는 방식을 사용한다.

#### 2.5.5 SingleServerCache
- Single Server 에서 get, put 기능을 지원하는 cache 이다. 
- (1) 멀티 스레드 환경에서 이슈가 생기지 않도록 ConcurrentHashMap 을 사용한다.
- [테스트 코드](..%2F..%2Fallin-core%2Fsrc%2Ftest%2Fjava%2Fdevelop%2Fx%2Fcore%2Fcache%2FSingleServerCacheTest.java)

```java
public class SingleServerCache<K, V> implements XWritableCache<K, V> {
    // (1) 멀티 스레드 환경에서 이슈가 생기지 않도록 ConcurrentHashMap 을 사용한다.
    private final Map<K, V> items = new ConcurrentHashMap<>();

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
```

#### 2.5.6 MultiServerCache
- Multi server cache 이며, read, write 기능 제공하는 cache 이다. 
- 서버간에 cache 상태를 저장하기 위해 Hazelcast IMap 을 사용하여 구현하였다.

```java
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
```
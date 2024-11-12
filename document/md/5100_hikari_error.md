### 5.1 Hikari cp 오류
- allin system 은 hikari cp 를 사용중인데 계속해서 `add connection elided` 라는 메시지가 출력되고, 계속해서 다시 커넥션을 요청하는 것 처럼 보이는 현상이 발생하였다.
- 원인: max-lifetime 를 30000(30초)로 지정한 상태로 사용하니 idle 상태인 커넥션에 대한 재커넥션을 계속 요청하는 문제가 발생하였다.
- 해결방법: max-lifetime 를 default 값을 (30분) 으로 설정한다. (DB 타임아웃 시간보다는 적은게 좋다고 한다.)

#### 5.1.1 maxLifetime
- 커넥션 풀에서 살아있을 수 있는 커넥션의 최대 수명시간. 
- 사용중인 커넥션은 maxLifetime에 상관없이 제거되지않음. 사용중이지 않을 때만 제거됨. 
- 풀 전체가아닌 커넥션 별로 적용이되는데 그 이유는 풀에서 대량으로 커넥션들이 제거되는 것을 방지하기 위함임. 
- 강력하게 설정해야하는 설정 값으로 데이터베이스나 인프라의 적용된 connection time limit 보다 작아야함. 
- (default: 1800000 (30minutes))

#### 5.1.2 Logging 내용
- 기존 Connection 을 종료하고 새롭게 연결하는 것을 확인할 수 있다. 
```text
HikariPool-1 - Closing connection conn19: url=jdbc:h2:mem:~/langrisser user=SA: (connection has passed maxLifetime)
HikariPool-1 - Added connection conn39: url=jdbc:h2:mem:~/langrisser user=SA
HikariPool-1 - Connection not added, stats (total=10, active=0, idle=10, waiting=0)
HikariPool-1 - Pool stats (total=10, active=0, idle=10, waiting=0)
HikariPool-1 - Fill pool skipped, pool has sufficient level or currently being filled.
```

#### 5.1.3 hikari cp 설정 
- `max-lifetime: 30000` 여기 설정을 봐야 되는데 30초마다 기존 idle 상태인 스레트풀을 갱신하는 작업이 발생한다. 
```yml
    hikari:
      connection-timeout: 30000 #maximum number of milliseconds that a client will wait for a connection
      minimum-idle: 20 #minimum number of idle connections maintained by HikariCP in a connection pool
      maximum-pool-size: 20 #maximum pool size
      idle-timeout: 10000 #maximum idle time for connection
      max-lifetime: 30000 # maximum lifetime in milliseconds of a connection in the pool after it is closed.
      auto-commit: false #defau
```

#### 5.1.4 조치방안
- 사실 크게 문제되는 상태는 아니었다. 쉬고 있는 커넥션을 새로 가져오는데. 좀 자주 가져오는 상태였다. 
- 그래도 쓸데없는 리소스가 없어지게 되므로 기본값인 30분으로 변경하였다. 
- 변경한 hikari cp 설정
- `max-lifetime: 30000` 여기 설정을 봐야 되는데 30초마다 기존 idle 상태인 스레트풀을 갱신하는 작업이 발생한다.
```yml
    hikari:
      connection-timeout: 30000 #maximum number of milliseconds that a client will wait for a connection
      minimum-idle: 20 #minimum number of idle connections maintained by HikariCP in a connection pool
      maximum-pool-size: 20 #maximum pool size
      idle-timeout: 10000 #maximum idle time for connection
      # max-lifetime: 30000 # Default 30분
      auto-commit: false #defau
```

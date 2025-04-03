# Spring Integration

## **🔹 Spring Integration이란?**

Spring Integration은 **Spring 기반의 메시지 기반 통합 솔루션**입니다.\
쉽게 말하면, **다양한 시스템 간의 데이터 흐름을 연결**해주는 역할을 합니다.

### **주요 특징**

✅ **경량 프레임워크** → Spring 위에서 동작하므로 기존 Spring 환경과 쉽게 통합 가능\
✅ **Enterprise Integration Patterns(EIP) 지원** → 메시지 라우팅, 변환, 필터링 등 다양한 패턴 지원\
✅ **비동기 메시징 지원** → 이벤트 기반 아키텍처에 적합\
✅ **다양한 프로토콜 지원** → TCP, HTTP, WebSocket, JMS, Kafka, RabbitMQ 등

---

## **🔹 Spring Integration이 필요한 이유**

✔️ **여러 시스템과의 통합**\
✔️ **데이터 변환 및 라우팅**\
✔️ **비동기 처리 및 이벤트 기반 아키텍처 구축**\
✔️ **Spring Boot와 자연스럽게 통합**

---

## **🔹 주요 컴포넌트**

Spring Integration에는 다양한 컴포넌트가 있습니다.\
아래는 주요 컴포넌트들입니다!

| **컴포넌트**     | **설명**                                           |
| ------------ | ------------------------------------------------ |
| **Message**  | 전달되는 데이터 객체 (`Message<T>` 형식)                    |
| **Channel**  | 메시지를 전달하는 통로 (`DirectChannel`, `QueueChannel` 등) |
| **Endpoint** | 메시지를 처리하는 컴포넌트 (필터, 트랜스포머, 라우터 등)                |
| **Gateway**  | 외부 시스템과의 인터페이스 역할                                |
| **Adapter**  | 외부 시스템(TCP, HTTP, JMS 등)과 연결하는 역할                |

---

## **🔹 Spring Integration의 TCP 지원**

Spring Integration은 **TCP 및 UDP 통신을 쉽게 설정할 수 있도록 지원**합니다.\
TCP 통신을 위한 주요 클래스는 다음과 같습니다:

### **서버용 TCP 클래스**

| 클래스명                              | 설명                         |
| --------------------------------- | -------------------------- |
| `TcpNetServerConnectionFactory`   | 기본적인 Java Socket 기반 TCP 서버 |
| `TcpNioServerConnectionFactory`   | NIO 기반의 비동기 TCP 서버         |
| `TcpNettyServerConnectionFactory` | **Netty 기반의 TCP 서버**       |

### **클라이언트용 TCP 클래스**

| 클래스명                              | 설명                            |
| --------------------------------- | ----------------------------- |
| `TcpNetClientConnectionFactory`   | 기본적인 Java Socket 기반 TCP 클라이언트 |
| `TcpNioClientConnectionFactory`   | NIO 기반의 비동기 TCP 클라이언트         |
| `TcpNettyClientConnectionFactory` | **Netty 기반의 TCP 클라이언트**       |

🔥 **Spring Integration을 사용하면 TCP 서버 및 클라이언트를 쉽게 설정할 수 있습니다!** 🚀

---

## **🔹 간단한 Spring Integration TCP 서버 예제**

📌 **TCP 메시지를 받고 응답하는 서버를 만들어봅시다!**

### **1️⃣ 의존성 추가 (Maven)**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-ip</artifactId>
</dependency>
```

### **2️⃣ TCP 서버 설정 (Java Config)**

```java
@Configuration
public class TcpServerConfig {

    @Bean
    public TcpInboundGateway tcpInboundGateway(AbstractServerConnectionFactory connectionFactory) {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(connectionFactory);
        gateway.setRequestChannel(tcpRequestChannel());
        return gateway;
    }

    @Bean
    public MessageChannel tcpRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNetServerConnectionFactory factory = new TcpNetServerConnectionFactory(12345);
        factory.setSerializer(new ByteArrayCrLfSerializer());
        factory.setDeserializer(new ByteArrayCrLfSerializer());
        return factory;
    }

    @ServiceActivator(inputChannel = "tcpRequestChannel")
    public String handleTcpMessage(byte[] message) {
        String receivedMessage = new String(message, StandardCharsets.UTF_8);
        System.out.println("📥 Received: " + receivedMessage);
        return "Echo: " + receivedMessage;
    }
}
```

✔️ **12345 포트에서 TCP 요청을 받고, "Echo: " + 메시지를 응답하는 TCP 서버입니다!**

---

## **🔹 Spring Integration vs Netty 차이점**

| **비교 항목**  | **Spring Integration**   | **Netty**         |
| ---------- | ------------------------ | ----------------- |
| **주요 목적**  | **Spring 기반의 메시지 통합**    | **고성능 비동기 네트워크**  |
| **사용 방식**  | XML/Java Config로 설정      | 직접 이벤트 루프 핸들링     |
| **비동기 처리** | 일부 지원 (NIO 사용 시)         | **기본적으로 비동기 지원**  |
| **확장성**    | 간단한 설정으로 다양한 프로토콜 지원     | **복잡한 커스터마이징 가능** |
| **성능**     | 메시지 중심의 통합 솔루션 (비교적 무거움) | **고성능 TCP 서버 가능** |

✅ **Spring Integration은 설정이 쉽고 Spring 환경과 통합하기 편리하지만, 성능이 중요한 경우 Netty가 더 적합합니다.**

---

## **🔹 정리**

✔️ **Spring Integration은 다양한 시스템을 통합하는 프레임워크**\
✔️ **TCP, HTTP, WebSocket, JMS 등 다양한 프로토콜 지원**\
✔️ **Spring 기반의 설정 방식으로 사용이 간편함**\
✔️ **Spring Integration의 TCP 기능은 기본적인 메시징 처리에는 적합, 하지만 고성능이 필요하면 Netty가 더 유리함**

🔥 **Spring Integration은 메시지 기반 통합이 필요할 때 매우 유용합니다!** 🚀

## **🔹 기타 고려 하고 있는 내용**
✔️ **TCP 메시지 직렬화/역직렬화 담당**\
✔️ **헤더 손상 감지 기능 제공**\
✔️ **메시지 크기 검증 로직 포함**

## **🔹 주요 기능**

### 연결 관리

✔️ **정적 클라이언트 설정: 설정 파일에 정의된 클라이언트 자동 등록**\
✔️ **동적 클라이언트 연결: 런타임에 들어오는 연결 처리**\
✔️ **연결 상태 모니터링: 활성/비활성 연결 추적**\
✔️ **연결 이벤트 발행: 연결 개설/종료/오류 이벤트**\

### 메시지 송수신

✔️ **비동기/동기 메시지 처리**\
✔️ **상관관계 ID 기반 요청-응답 매핑**\
✔️ **메시지 라우팅: 클라이언트 ID 기반 라우팅**\
✔️ **오류 처리 및 로깅**\

### 연결 유지 관리

✔️ **하트비트 메커니즘: 주기적으로 연결 상태 확인**\
✔️ **비활성 클라이언트 정리: 일정 시간 이상 비활성인 연결 자동 정리**\
✔️ **만료된 상관관계 ID 정리: 응답 없는 요청 정리**\

### 모니터링 및 진단

✔️ **연결 상태 로깅: 모든 연결의 상세 정보 제공**\
✔️ **메시지 처리 통계: 메시지 수, 바이트 수 등 추적**\
✔️ **오류 이벤트 발행: 연결 및 메시지 처리 오류 알림**\

 

# Spring Integration이란
- Spring Integration은 Spring 기반의 메시지 기반 통합 솔루션으로 다양한 시스템 간의 데이터 흐름을 연결해주는 역할

## 주요 특징</br>
- 경량 프레임워크 → Spring 위에서 동작하므로 기존 Spring 환경과 쉽게 통합 가능</br>
- Enterprise Integration Patterns(EIP) 지원 → 메시지 라우팅, 변환, 필터링 등 다양한 패턴 지원</br>
- 비동기 메시징 지원 → 이벤트 기반 아키텍처에 적합</br>
- 다양한 프로토콜 지원 → TCP, HTTP, WebSocket, JMS, Kafka, RabbitMQ 등

## Spring Integration이 필요한 이유</br>
- 여러 시스템과의 통합</br>
- 데이터 변환 및 라우팅</br>
- 비동기 처리 및 이벤트 기반 아키텍처 구축</br>
- Spring Boot와 자연스럽게 통합</br>


## 주요 컴포넌트
| 컴포넌트| 설명|
| --| --|
Message	전달되는 데이터 객체 (Message<T> 형식)
Channel	메시지를 전달하는 통로 (DirectChannel, QueueChannel 등)
Endpoint	메시지를 처리하는 컴포넌트 (필터, 트랜스포머, 라우터 등)
Gateway	외부 시스템과의 인터페이스 역할
Adapter	외부 시스템(TCP, HTTP, JMS 등)과 연결하는 역할


## Spring Integration의 TCP 지원
- Spring Integration은 TCP 및 UDP 통신을 쉽게 설정할 수 있도록 지원

### 서버용 TCP 클래스
| 클래스명| 설명|
| --| --|
| TcpNetServerConnectionFactory|기본적인 Java Socket 기반 TCP 서버|
| TcpNioServerConnectionFactory|NIO 기반의 비동기 TCP 서버| 
| TcpNettyServerConnectionFactory|Netty 기반의 TCP 서버| 

### 클라이언트용 TCP 클래스
| 클래스명| 설명|
| --| --|
| TcpNetClientConnectionFactory|기본적인 Java Socket 기반 TCP 서버|
| TcpNioClientConnectionFactory|NIO 기반의 비동기 TCP 클라이언트| 
| TcpNettyClientConnectionFactory|Netty 기반의 TCP 클라이언트| 

	
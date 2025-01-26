# API 게이트웨이

## 요구사항 정리
- N개의 서버에서 TCP/IP 로 요청이 온 데이터를 파싱하여 요청에 맞는 메시지 브로커로 전달해야 한다.
- 메시지 브로커로 수신한 데이터를 TCP/IP 방식으로 N개의 서버에 분산해서 전달해야 한다.
- (방안1) 메시지 브로커는 각 서버에 기동되며, 신규로 도입해야 한다. (ActiveMQ)
- (방안2) 기존에 IPC인 hazelcast 를 활용해서 사용

## 고려사항
- TCP 프로토콜 사용시에 분산 처리 및 일부 노드 장애시 처리 방안 고려(분산 트랜잭션)
- 성능을 위해 기능의 구현을 WebFlux 기반(리액티브)으로 작성  
- 기존 모드에서는 키 데이터만 전송하는데 여기서는 전체 데이터를 전달해야 됨
- 기존 모드와 상이한 데이터 구조(직렬화, 역직렬화 문제)

## hazelcast 도커 설치 
- https://docs.hazelcast.com/hazelcast/5.5/getting-started/get-started-docker
- 명령어
> docker run -it --network hazelcast-network --rm -e HZ_NETWORK_PUBLICADDRESS=127.0.0.1:5701 -e HZ_CLUSTERNAME=hello-world -p 5701:5701 hazelcast/hazelcast
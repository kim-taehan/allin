package develop.x.betting.rmi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;

@Configuration
public class RmiServerConfig {

    @Bean
    public RmiServiceExporter rmiServiceExporter(MemoryService memoryService) {
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("MemoryService"); // RMI 서비스 이름
        exporter.setService(memoryService); // 실제 서비스 구현체
        exporter.setServiceInterface(MemoryService.class); // 서비스 인터페이스 지정
        exporter.setRegistryPort(1099); // RMI Registry 포트 설정
        return exporter;
    }
}
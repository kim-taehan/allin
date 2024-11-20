package develop.x.network.ipc.activemq;

import develop.x.io.XRequest;
import develop.x.io.network.XTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessagePublisher {

    private final JmsTemplate jmsTemplate;

    public MessagePublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(XRequest xRequest) {

        jmsTemplate.convertAndSend(XTarget.RISK.getQueueName(), xRequest.toByte());
    }

    @JmsListener(destination = "TEST",  concurrency = "5-10")
    public void reciveMessage1(byte[] messageDto) {
        log.info("Received1 message: {}", new XRequest(messageDto));
    }

}

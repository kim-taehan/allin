//package develop.x.study.activemq;
//
//import jakarta.jms.*;
//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.apache.activemq.pool.PooledConnectionFactory;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.jms.core.JmsTemplate;
//
//import java.nio.charset.StandardCharsets;
//
//public class ActiveMqTest {
//
//    @DisplayName("")
//    @Test
//    void test854() throws JMSException {
//        // given
//
//
//        // Create a connection factory.
//        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
//
//        // Create a pooled connection factory.
//        final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
//        pooledConnectionFactory.setConnectionFactory(connectionFactory);
//        pooledConnectionFactory.setMaxConnections(10);
//
//        // Establish a connection for the producer.
//        final Connection producerConnection = pooledConnectionFactory.createConnection();
//        producerConnection.start();
//
//
//
//        final Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//        // Create a queue named "MyQueue".
//        final Destination producerDestination = producerSession.createQueue("MyQueue");
//
//        // Create a producer from the session to the queue.
//        final MessageProducer producer = producerSession.createProducer(producerDestination);
//        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//
//        final String text = "Hello from Amazon MQ!";
//        TextMessage producerMessage = producerSession.createTextMessage(text);
//
//        BytesMessage bytesMessage = producerSession.createBytesMessage();
//        bytesMessage.writeBytes(text.getBytes(StandardCharsets.UTF_8));
//
//        // Send the message.
//        producer.send(producerMessage);
//
//
//
//        // Establish a connection for the consumer.
//        final Connection consumerConnection = connectionFactory.createConnection();
//        consumerConnection.start();
//
//        // Create a session.
//        final Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//        // Create a queue named "MyQueue".
//        final Destination consumerDestination = consumerSession.createQueue("MyQueue");
//
//        // Create a message consumer from the session to the queue.
//        final MessageConsumer consumer = consumerSession.createConsumer(consumerDestination);
//
//
//        // Begin to wait for messages.
//        final Message consumerMessage = consumer.receive(1000);
//
//        // Receive the message when it arrives.
//        final TextMessage consumerTextMessage = (TextMessage) consumerMessage;
//        System.out.println("Message received: " + consumerTextMessage.getText());
//
//        // when
//
//        // then
//        Assertions.assertThat("");
//    }
//
//}

package develop.x.core;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import develop.x.core.dispatcher.XRequest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class HazelcastTest {

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
//
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("hello-world");
//
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        IMap <String, Object> map = client.getMap("MAP_ORDER");
        IQueue<String> queue = client.getQueue("MQ_ORDER");

        String key = UUID.randomUUID().toString();

        // 표준을 먼저 정해야 된다..
        // api 전문은 header 가 별도로 존재해야 된다. body 데이터는 json 형태로 정의한다.
        // 데이터 전달은 header + body 를 byte[] 형태로 전환해서 전달한다.

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("username", "kimtaehan\nTest");
        objectNode.put("age", 30);
        byte[] bodyByte = objectMapper.writeValueAsBytes(objectNode);
        StringBuilder sb = new StringBuilder();
        sb.append("url=/normal-api\n");
        sb.append("transactionId=");
        sb.append(key);
        sb.append("\n");
        sb.append("contentType=JSON\n");
        sb.append("contentLength=");
        sb.append(bodyByte.length);
        sb.append("\n");
        sb.append("\n");
        byte[] headerByte = sb.toString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[headerByte.length + bodyByte.length]);
        buffer.put(headerByte);
        buffer.put(bodyByte);
        byte[] array = buffer.array();
//        XRequest xRequest = new XRequest(array);
//
//        System.out.println("xRequest = " + xRequest);

        // return
        map.put(key, array);
        queue.put(key);




//        FakeXData a001 = new FakeXData("A001", 1_000);
//        byte[] convertedA001 = byJson("/normal-api", a001, key);
//
//        map.put(key, convertedA001);
//        queue.put(key);

    }


//    private static byte[] byJson(String url, FakeXData a001, String transactionId) {
//        XHeader xHeader = new XHeader(JSON, url,transactionId);
//        byte[] header = xHeader.convert();
//        try {
//            byte[] body = new ObjectMapper().writeValueAsBytes(a001);
//            byte[] combined = new byte[header.length + body.length];
//
//            ByteBuffer buffer = ByteBuffer.wrap(combined);
//            buffer.put(header);
//            buffer.put(body);
//            return buffer.array();
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
}

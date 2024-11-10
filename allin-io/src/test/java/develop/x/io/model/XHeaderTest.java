package develop.x.io.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


class XHeaderTest {

    @DisplayName("XHeader byte 변환과 객체 변환을 확인한다.")
    @Test
    void test498() throws JsonProcessingException {


        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("username", "kimtaehan\nTest");
        objectNode.put("age", 30);
        byte[] bodyByte = objectMapper.writeValueAsBytes(objectNode);

        StringBuilder sb = new StringBuilder();
        sb.append("url=/test-api\n");
        sb.append("transactionId=");
        sb.append(UUID.randomUUID());
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


        String string = new String(array);
        String[] split = string.split("\n");
        for (String s : split) {
            System.out.println("s = " + s);
        }
//        "\n".getBytes(StandardCharsets.UTF_8)

    }



}
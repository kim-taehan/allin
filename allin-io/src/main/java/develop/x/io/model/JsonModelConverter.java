package develop.x.io.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class JsonModelConverter implements ModelConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T toModel(Class<T> type, byte[] bytes) {
        try {
            return (T) objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] toByte(T t) {
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}

package develop.x.io.model;

import java.io.*;

public class SerializableModelConverter implements ModelConverter {

    @Override
    public <T> T toModel(Class<T> type, byte[] bytes) {
        try(
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis)
        ){
            return (T) ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] toByte(T t) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try(ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(t);
                //직렬화(byte array)
                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }
}

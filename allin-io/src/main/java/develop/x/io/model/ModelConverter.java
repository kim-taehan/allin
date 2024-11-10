package develop.x.io.model;

public interface ModelConverter {

    <T> T toModel(Class<T> type, byte[] bytes);

    <T> byte[] toByte(T t);
}

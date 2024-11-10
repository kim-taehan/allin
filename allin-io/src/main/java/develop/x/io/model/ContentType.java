package develop.x.io.model;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
public enum ContentType  {
    JSON(new JsonModelConverter()), SERIALIZE(new SerializableModelConverter());

    private final ModelConverter modelConverter;

    public Object toModel(Class<?> type, byte[] body) {
        return modelConverter.toModel(type, body);
    }

    public <T> byte[] toByte(T t) {
        return modelConverter.toByte(t);
    }

}

package develop.x.simulator.game.entity;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class TopLlEvntId {

    private String eventId;

    private String eventName;

    public TopLlEvntId() {
    }

    public TopLlEvntId(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TopLlEvntId that = (TopLlEvntId) object;
        return Objects.equals(eventId, that.eventId) && Objects.equals(eventName, that.eventName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventName);
    }
}

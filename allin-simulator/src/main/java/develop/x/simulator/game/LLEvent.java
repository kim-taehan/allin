package develop.x.simulator.game;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
public class LLEvent {

    private String displayName;
    private String eventId;
    private LLType type;

    public LLEvent(String displayName, String eventId, LLType type) {
        this.displayName = displayName;
        this.eventId = eventId;
        this.type = type;
    }
}

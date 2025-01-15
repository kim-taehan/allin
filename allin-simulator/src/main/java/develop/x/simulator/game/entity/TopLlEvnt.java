package develop.x.simulator.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "TOP_LL_EVNT")
@IdClass(TopLlEvntId.class)
@NoArgsConstructor
public class TopLlEvnt {

    @Id
    @Column(name = "EVENT_ID")
    private String eventId;

    @Id
    @Column(name = "EVENT_NAME", nullable = false)
    private String eventName;

    @Column(name = "EVENT_DATE")
    private LocalDate eventDate;

    @ManyToOne(fetch = FetchType.LAZY)  // 다대일 관계
    @JoinColumn(name = "PRODUCT_CODE")
    private TopProd topProd;  // 연관된 제품 객체


    public TopLlEvnt(String eventId, String eventName, LocalDate eventDate) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }
}

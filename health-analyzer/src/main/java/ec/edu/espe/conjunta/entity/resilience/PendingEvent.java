package ec.edu.espe.conjunta.entity.resilience;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "pending_events")
@Data
@NoArgsConstructor
public class PendingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_json", nullable = false, length = 1024)
    private String eventJson;

    @Column(name = "exchange_name", nullable = false)
    private String exchangeName;

    @Column(name = "routing_key", nullable = false)
    private String routingKey;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PendingEvent(String eventJson, String exchangeName, String routingKey) {
        this.eventJson = eventJson;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.createdAt = Instant.now();
    }
}

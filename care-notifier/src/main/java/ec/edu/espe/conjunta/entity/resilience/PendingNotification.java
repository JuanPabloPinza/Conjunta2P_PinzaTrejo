package ec.edu.espe.conjunta.entity.resilience;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "pending_notifications")
@Data
@NoArgsConstructor
public class PendingNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT") // TEXT para cuerpos largos
    private String body;

    @Column(name = "channel", nullable = false) // EMAIL, SMS, PUSH
    private String channel;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PendingNotification(String recipient, String subject, String body, String channel) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.channel = channel;
        this.createdAt = Instant.now();
    }
}
package ec.edu.espe.conjunta.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    @Column(name = "notification_id")
    private String notificationId; // ID que viene del servicio, no autogenerado

    @Column(name = "event_type", nullable = false)
    private String eventType; // Tipo de evento que disparó la notificación (ej. CriticalHeartRateAlert, DailyReport)

    @Column(name = "recipient", nullable = false)
    private String recipient; // A quién se envió la notificación (ej. email del médico)

    @Column(name = "status", nullable = false)
    private String status; // Estado del envío (ej. "SENT", "FAILED")

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp; // Cuándo se registró la notificación

    // Constructor para facilitar la creación (opcional, Lombok AllArgsConstructor también lo hace)
    public Notification(String notificationId, String eventType, String recipient, String status, Instant timestamp) {
        this.notificationId = notificationId;
        this.eventType = eventType;
        this.recipient = recipient;
        this.status = status;
        this.timestamp = timestamp;
    }
}
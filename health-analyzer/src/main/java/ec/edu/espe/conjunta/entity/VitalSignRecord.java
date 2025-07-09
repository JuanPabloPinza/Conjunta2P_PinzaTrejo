package ec.edu.espe.conjunta.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "vital_sign_records") // Nombre de tabla para los registros históricos
@Data
@NoArgsConstructor
public class VitalSignRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autogenerado
    private Long id;

    @Column(nullable = false, name = "event_id") // ID original del evento, para idempotencia
    private String eventId;

    @Column(nullable = false, name = "device_id")
    private String deviceId;

    @Column(nullable = false)
    private String type; // e.g., "heart-rate", "oxygen-level"

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp; // Timestamp original de la lectura

    @Column(nullable = false, name = "received_at")
    private Instant receivedAt; // Cuando HealthAnalyzer recibió y registró el evento

    public VitalSignRecord(String eventId, String deviceId, String type, Double value, Instant timestamp) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
        this.receivedAt = Instant.now();
    }
}
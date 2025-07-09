package ec.edu.espe.conjunta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewVitalSignEvent {
    // Idempotencia: cada evento tiene un ID único
    private String eventId = "evt-" + UUID.randomUUID().toString();
    private String deviceId;
    private String type;
    private Double value;
    private Instant timestamp;
}
package ec.edu.espe.conjunta.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "medical_alerts")
@Data
public class MedicalAlert {
    @Id
    @Column(name = "alert_id")
    private String alertId; // Viene del evento, no es autogenerado
    @Column(nullable = false)
    private String type;

    @Column(nullable = false , name = "device_id")
    private String deviceId;

    @Column(nullable = false)
    private Double value;
    private Double threshold;
    @Column(nullable = false)
    private Instant timestamp;
}
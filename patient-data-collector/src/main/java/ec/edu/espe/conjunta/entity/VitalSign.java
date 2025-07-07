package ec.edu.espe.conjunta.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "vital_signs")
@Data
public class VitalSign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "device_id")
    private String deviceId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;
}
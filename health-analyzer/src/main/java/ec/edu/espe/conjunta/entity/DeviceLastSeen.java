package ec.edu.espe.conjunta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "device_last_seen")
@Data
public class DeviceLastSeen {

    // Usamos el deviceId como clave primaria, ya que es único por dispositivo.
    @Id
    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "last_seen_timestamp", nullable = false)
    private Instant lastSeenTimestamp;
}
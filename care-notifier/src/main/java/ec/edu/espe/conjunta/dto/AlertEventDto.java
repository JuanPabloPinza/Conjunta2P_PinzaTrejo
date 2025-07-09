package ec.edu.espe.conjunta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertEventDto implements Serializable {
    private String alertId;
    private String type;
    private String deviceId;
    private Double value;
    private Double threshold;
    private Instant timestamp;
}

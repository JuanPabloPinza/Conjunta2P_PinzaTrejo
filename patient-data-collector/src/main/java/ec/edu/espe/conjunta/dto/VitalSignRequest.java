package ec.edu.espe.conjunta.dto;

import lombok.Data;

@Data
public class VitalSignRequest {
    private String deviceId;
    private String type;
    private Double value;
    private String timestamp; // Recibimos como String para flexibilidad
}
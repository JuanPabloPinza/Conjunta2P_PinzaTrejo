package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import ec.edu.espe.conjunta.dto.VitalSignRequest;
import ec.edu.espe.conjunta.entity.VitalSign;
import ec.edu.espe.conjunta.repository.VitalSignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
public class VitalSignService {

    @Autowired
    private VitalSignRepository vitalSignRepository;

    @Autowired
    private ResilienceService resilienceService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public void processAndStoreVitalSign(VitalSignRequest request) {
        // 1. Validación (Requisito B.1.e)
        validateRequest(request);

        // 2. Mapeo de DTO a Entidad
        VitalSign vitalSign = new VitalSign();
        vitalSign.setDeviceId(request.getDeviceId());
        vitalSign.setType(request.getType());
        vitalSign.setValue(request.getValue());
        try {
            vitalSign.setTimestamp(Instant.parse(request.getTimestamp()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format. Use ISO-8601 format (e.g., 2024-04-05T12:00:00Z).");
        }

        // 3. Guardar en CockroachDB
        VitalSign savedVitalSign = vitalSignRepository.save(vitalSign);
        log.info("Vital sign stored with ID: {}", savedVitalSign.getId());

        // 4. Crear y publicar el evento (Requisito B.1.c)
        NewVitalSignEvent event = new NewVitalSignEvent(
                "evt-" + savedVitalSign.getId(), // Usamos el ID de la BD para mayor trazabilidad
                savedVitalSign.getDeviceId(),
                savedVitalSign.getType(),
                savedVitalSign.getValue(),
                savedVitalSign.getTimestamp()
        );

        // Aquí se implementaría la lógica de resiliencia
        publishEvent(event);
    }

    private void publishEvent(NewVitalSignEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.GLOBAL_EXCHANGE_NAME,
                    RabbitMQConfig.NEW_VITAL_SIGN_ROUTING_KEY,
                    event
            );
            log.info("Published NewVitalSignEvent for deviceId: {}", event.getDeviceId());
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ. Storing locally for retry.", e);
            // LLAMAMOS AL SERVICIO DE RESILIENCIA
            resilienceService.storeEventForRetry(event);
        }
    }

    private void validateRequest(VitalSignRequest request) {
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("Device ID cannot be empty.");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException("Type cannot be empty.");
        }
        if (request.getValue() == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        // Validación de rango (ej. Frecuencia cardíaca)
        if ("heart-rate".equalsIgnoreCase(request.getType())) {
            if (request.getValue() > 200 || request.getValue() < 30) {
                throw new IllegalArgumentException("Heart rate value out of valid range (30-200).");
            }
        }
    }



    public List<VitalSign> getHistoryByDevice(String deviceId) {
        return vitalSignRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }
}
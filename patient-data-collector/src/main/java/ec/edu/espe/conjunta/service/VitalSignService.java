package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import ec.edu.espe.conjunta.dto.VitalSignRequest;
import ec.edu.espe.conjunta.entity.VitalSign;
import ec.edu.espe.conjunta.producer.EventProducer;
import ec.edu.espe.conjunta.repository.VitalSignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor // Inyecta las dependencias finales a través del constructor (mejor práctica)
public class VitalSignService {

    private final VitalSignRepository vitalSignRepository;
    private final EventProducer eventProducer; // <-- Ahora usamos el productor dedicado

    /**
     * Procesa y almacena un nuevo signo vital. Esta operación es transaccional
     * para la base de datos de CockroachDB.
     * @param request El DTO con los datos del signo vital.
     */
    @Transactional("cockroachTransactionManager") // Especifica el transaction manager para esta operación
    public VitalSign processAndStoreVitalSign(VitalSignRequest request) {
        // ... (validación y mapeo no cambian) ...
        validateRequest(request);

        VitalSign vitalSign = new VitalSign();
        vitalSign.setDeviceId(request.getDeviceId());
        vitalSign.setType(request.getType());
        vitalSign.setValue(request.getValue());
        try {
            vitalSign.setTimestamp(Instant.parse(request.getTimestamp()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format. Use ISO-8601 format (e.g., 2024-04-05T12:00:00Z).");
        }

        VitalSign savedVitalSign = vitalSignRepository.save(vitalSign);
        log.info("Signo vital almacenado en CockroachDB con ID: {}", savedVitalSign.getId());

        NewVitalSignEvent event = new NewVitalSignEvent(
                "evt-" + UUID.randomUUID().toString(),
                savedVitalSign.getDeviceId(),
                savedVitalSign.getType(),
                savedVitalSign.getValue(),
                savedVitalSign.getTimestamp()
        );
        eventProducer.publishNewVitalSignEvent(event);

        // --- ¡CAMBIO CLAVE: Devolvemos la entidad guardada! ---
        return savedVitalSign;
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
        // Añadir más validaciones si es necesario
    }

    /**
     * Obtiene el historial de signos vitales para un dispositivo, ordenado por fecha descendente.
     * Esta operación es de solo lectura.
     * @param deviceId El ID del dispositivo.
     * @return Una lista de signos vitales.
     */
    @Transactional(value = "cockroachTransactionManager", readOnly = true)
    public List<VitalSign> getHistoryByDevice(String deviceId) {
        return vitalSignRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    /**
     * Obtiene todos los signos vitales registrados en el sistema.
     * ¡PRECAUCIÓN! Este método puede ser costoso en rendimiento si hay muchos datos.
     * Es útil para depuración o tareas administrativas.
     * @return Una lista con todos los signos vitales.
     */
    @Transactional(value = "cockroachTransactionManager", readOnly = true)
    public List<VitalSign> getAllVitalSigns() {
        return vitalSignRepository.findAll();
    }


}
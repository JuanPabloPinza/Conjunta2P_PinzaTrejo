package ec.edu.espe.conjunta.listener;

import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import ec.edu.espe.conjunta.entity.DeviceLastSeen;
import ec.edu.espe.conjunta.entity.VitalSignRecord;
import ec.edu.espe.conjunta.repository.DeviceLastSeenRepository;
import ec.edu.espe.conjunta.repository.VitalSignRecordRepository;
import ec.edu.espe.conjunta.service.HealthAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class VitalSignListener {
    private final HealthAnalysisService healthAnalysisService;
    private final DeviceLastSeenRepository deviceLastSeenRepository;
    private final VitalSignRecordRepository vitalSignRecordRepository;

    @RabbitListener(queues = RabbitMQConfig.VITAL_SIGN_QUEUE)
    @Transactional("cockroachTransactionManager")
    public void handleNewVitalSign(@Payload NewVitalSignEvent event) {
        log.info("Recibido NewVitalSignEvent: {}", event.getEventId());
        healthAnalysisService.analyzeVitalSign(event);

        updateDeviceLastSeen(event);


        saveVitalSignRecord(event);
    }

    /**
     * Actualiza la tabla 'device_last_seen' con el timestamp más reciente.
     * El método save() de JpaRepository actúa como un "upsert" (UPDATE si existe, INSERT si no).
     * @param event El evento con los datos del signo vital.
     */
    private void updateDeviceLastSeen(NewVitalSignEvent event) {
        DeviceLastSeen state = new DeviceLastSeen();
        state.setDeviceId(event.getDeviceId());
        state.setLastSeenTimestamp(event.getTimestamp());
        deviceLastSeenRepository.save(state);
        log.debug("Actualizado el estado 'last_seen' para el dispositivo: {}", event.getDeviceId());
    }


    private void saveVitalSignRecord(NewVitalSignEvent event) {
        VitalSignRecord record = new VitalSignRecord(
                event.getEventId(),
                event.getDeviceId(),
                event.getType(),
                event.getValue(),
                event.getTimestamp()
        );
        // Usamos saveAndFlush para que se persista inmediatamente y detecte duplicados de eventId si los hay
        try {
            vitalSignRecordRepository.saveAndFlush(record);
            log.debug("Signo vital histórico registrado para reporte: {}", event.getEventId());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Intento de guardar evento duplicado ({}). Ignorado para asegurar idempotencia.", event.getEventId());
        } catch (Exception e) {
            log.error("Error al guardar registro histórico de signo vital {}: {}", event.getEventId(), e.getMessage(), e);
        }
    }



}
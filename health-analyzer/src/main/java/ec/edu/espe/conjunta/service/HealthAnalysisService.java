package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.dto.AlertEventDto;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import ec.edu.espe.conjunta.entity.MedicalAlert;
import ec.edu.espe.conjunta.producer.AlertEventProducer;
import ec.edu.espe.conjunta.repository.MedicalAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthAnalysisService {
    private final MedicalAlertRepository alertRepository;
    private final AlertEventProducer alertProducer;

    @Transactional("cockroachTransactionManager")
    public void analyzeVitalSign(NewVitalSignEvent event) {
        String alertType = null;
        Double threshold = null;
        String eventType = event.getType();
        Double eventValue = event.getValue();


        // 1. Análisis de Frecuencia Cardíaca
        if ("heart-rate".equalsIgnoreCase(event.getType())) {
            if (event.getValue() > 140) {
                alertType = "CriticalHeartRateAlert";
                threshold = 140.0;
            } else if (event.getValue() < 40) {
                alertType = "CriticalHeartRateAlert";
                threshold = 40.0;
            }
        }
        // 2. Análisis de Nivel de Oxígeno
        else if ("oxygen-level".equalsIgnoreCase(event.getType())) {
            if (event.getValue() < 90) {
                alertType = "OxygenLevelCritical";
                threshold = 90.0;
            }
        }
        // 3. Análisis de Presión Arterial Sistólica
        else if ("blood-pressure-systolic".equalsIgnoreCase(eventType)) {
            if (eventValue > 180) {
                alertType = "HighBloodPressureAlert";
                threshold = 180.0;
            }
        }
        // 4. Análisis de Presión Arterial Diastólica
        else if ("blood-pressure-diastolic".equalsIgnoreCase(eventType)) {
            if (eventValue > 120) {
                alertType = "HighBloodPressureAlert";
                threshold = 120.0;
            }
        }

        if (alertType != null) {
            log.warn("¡ALERTA GENERADA! Dispositivo: {}, Tipo: {}, Valor: {}, Umbral: {}",
                    event.getDeviceId(), alertType, eventValue, threshold);
            createAndSendAlert(event, alertType, threshold);
        }
    }

    private void createAndSendAlert(NewVitalSignEvent event, String alertType, Double threshold) {
        // 1. Crear y persistir la alerta
        MedicalAlert alert = new MedicalAlert();
        alert.setAlertId("alt-" + UUID.randomUUID().toString());
        alert.setType(alertType);
        alert.setDeviceId(event.getDeviceId());
        alert.setValue(event.getValue());
        alert.setThreshold(threshold);
        alert.setTimestamp(event.getTimestamp());
        alertRepository.save(alert);
        log.info("Alerta guardada en DB con ID: {}", alert.getAlertId());

        // 2. Crear y emitir el evento de alerta
        AlertEventDto alertEvent = new AlertEventDto();
        alertEvent.setAlertId(alert.getAlertId());
        alertEvent.setType(alert.getType());
        alertEvent.setDeviceId(alert.getDeviceId());
        alertEvent.setValue(alert.getValue());
        alertEvent.setThreshold(alert.getThreshold());
        alertEvent.setTimestamp(alert.getTimestamp());

        alertProducer.publishAlertEvent(alertEvent);
    }
}
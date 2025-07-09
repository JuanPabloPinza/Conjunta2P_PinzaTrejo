package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.dto.*;
import ec.edu.espe.conjunta.model.NotificationPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationDispatcher dispatcher;
    private final LowPriorityBatchService batchService;

    public void processAlert(AlertEventDto alert) {
        NotificationPriority priority = classifyAlert(alert.getType());
        String recipient = "doctor-on-call@hospital.com"; // Simulado
        String subject = String.format("Alerta %s: %s para %s", priority, alert.getType(), alert.getDeviceId());
        String body = String.format("Dispositivo: %s, Tipo: %s, Valor: %.2f, Umbral: %.2f. Hora: %s",
                alert.getDeviceId(), alert.getType(), alert.getValue(), alert.getThreshold(), alert.getTimestamp());

        if (priority == NotificationPriority.EMERGENCY) {
            log.info("Despachando alerta de emergencia inmediatamente: {}", alert.getType());
            dispatcher.dispatchImmediate(recipient, subject, body);
        } else {
            log.info("Añadiendo alerta de baja prioridad al lote: {}", alert.getType());
            batchService.addNotificationToBatch(recipient, subject, body);
        }
    }

    public void processDailyReport(DailyReportDto report) {
        log.info("Procesando reporte diario para envío por lotes.");
        String recipient = "admin-team@hospital.com";
        String subject = "Reporte Diario de Signos Vitales " + report.getReportTimestamp().toString();
        // Formatear un cuerpo de email bonito con los datos del reporte.
        StringBuilder bodyBuilder = new StringBuilder("Resumen de Signos Vitales (24h):\n\n");
        report.getVitalSignStats().forEach((type, stats) -> {
            bodyBuilder.append(String.format("  - Tipo: %s | Promedio: %.2f | Máx: %.2f | Mín: %.2f | Conteo: %d\n",
                    type, stats.getAverage(), stats.getMax(), stats.getMin(), stats.getCount()));
        });
        String body = bodyBuilder.toString();

        batchService.addNotificationToBatch(recipient, subject, body);
    }

    private NotificationPriority classifyAlert(String alertType) {
        if ("CriticalHeartRateAlert".equalsIgnoreCase(alertType) || "OxygenLevelCritical".equalsIgnoreCase(alertType) || "HighBloodPressureAlert".equalsIgnoreCase(alertType)) {
            return NotificationPriority.EMERGENCY;
        }
        if ("DeviceOfflineAlert".equalsIgnoreCase(alertType)) {
            return NotificationPriority.WARNING;
        }
        return NotificationPriority.INFO; // Para otros tipos de alertas que no sean críticas
    }
}
package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.AlertEventDto;
import ec.edu.espe.conjunta.dto.DailyReportDto;
import ec.edu.espe.conjunta.entity.VitalSignRecord;
import ec.edu.espe.conjunta.producer.AlertEventProducer;
import ec.edu.espe.conjunta.repository.DeviceLastSeenRepository;
import ec.edu.espe.conjunta.repository.VitalSignRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ec.edu.espe.conjunta.producer.AlertEventProducer;

import java.time.Duration;
import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledTaskService {


    private final DeviceLastSeenRepository deviceLastSeenRepository;
    private final AlertEventProducer alertProducer;
    private final VitalSignRecordRepository vitalSignRecordRepository;

    //real
    //@Scheduled(cron = "${tasks.daily-report.cron:0 0 4 * * *}") // Todos los días a las 4 AM por defecto
    //prueba
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional(value = "cockroachTransactionManager", readOnly = true) // Solo lectura
    public void generateDailyReport() {
        log.info("==================== INICIO TAREA: REPORTE DIARIO DE TENDENCIAS ====================");

        Instant periodEnd = Instant.now();
        Instant periodStart = periodEnd.minus(Duration.ofHours(24));

        List<VitalSignRecord> recentRecords = vitalSignRecordRepository.findByReceivedAtBetween(periodStart, periodEnd);

        if (recentRecords.isEmpty()) {
            log.warn("No se encontraron datos de signos vitales en las últimas 24 horas. No se generará reporte.");
            log.info("==================== FIN TAREA: REPORTE DIARIO DE TENDENCIAS (SIN DATOS) ====================");
            return;
        }

        log.info("Procesando {} registros de signos vitales recibidos entre {} y {}",
                recentRecords.size(), periodStart, periodEnd);

        Map<String, DoubleSummaryStatistics> statsByType = recentRecords.stream()
                .collect(Collectors.groupingBy(
                        VitalSignRecord::getType,
                        Collectors.summarizingDouble(VitalSignRecord::getValue)
                ));


        // Construir el cuerpo del log del reporte para una mejor visualización
        StringBuilder reportLog = new StringBuilder("\n--- REPORTE DIARIO DE SIGNOS VITALES ---\n");
        reportLog.append(String.format("Periodo: %s a %s\n", periodStart, periodEnd));
        reportLog.append("------------------------------------------\n");

        statsByType.forEach((type, stats) -> {
            reportLog.append(String.format("Tipo de Signo:  %-25s | Conteo: %d\n", type, stats.getCount()));
            reportLog.append(String.format("  - Promedio:   %.2f\n", stats.getAverage()));
            reportLog.append(String.format("  - Máximo:     %.2f\n", stats.getMax()));
            reportLog.append(String.format("  - Mínimo:     %.2f\n", stats.getMin()));
            reportLog.append("------------------------------------------\n");
        });

        // Imprimir el reporte formateado en una sola entrada de log
        log.info(reportLog.toString());

        // Construir y emitir el evento DTO
        Map<String, DailyReportDto.DailyReportStats> reportStats = statsByType.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new DailyReportDto.DailyReportStats(
                                entry.getValue().getAverage(),
                                entry.getValue().getMax(),
                                entry.getValue().getMin(),
                                entry.getValue().getCount()
                        )
                ));

        DailyReportDto dailyReport = new DailyReportDto(
                "rpt-" + UUID.randomUUID().toString(),
                Instant.now(),
                periodStart,
                periodEnd,
                reportStats
        );

        try {
            String reportRoutingKey = RabbitMQConfig.DAILY_REPORT_ROUTING_KEY;
            alertProducer.publishGenericEvent(dailyReport, reportRoutingKey);
            log.info("Evento DailyReportGenerated emitido exitosamente con ID: {}", dailyReport.getReportId());
        } catch (Exception e) {
            log.error("Fallo al emitir el evento DailyReportGenerated con ID: {}. Error: {}", dailyReport.getReportId(), e.getMessage(), e);
        }

        log.info("==================== FIN TAREA: REPORTE DIARIO DE TENDENCIAS ====================");
    }



    /**
     * Tarea para limpieza de datos históricos antiguos.
     * Requisito: 4.c
     */
    //real
    //@Scheduled(cron = "${tasks.cleanup-old-data.cron:0 0 5 1 * *}") // El primer día de cada mes a las 5 AM
    //prueba
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional("cockroachTransactionManager") // Esto sí modifica la BD
    public void cleanUpOldData() {
        log.info("Iniciando tarea mensual de limpieza de datos antiguos...");

        Instant twoYearsAgo = Instant.now().minus(Duration.ofDays(365 * 2)); // 2 años de antigüedad

        // Limpia registros de DeviceLastSeen que no se han visto en 2 años (opcional, ya que solo es un registro por ID)
        // deviceLastSeenRepository.deleteByLastSeenTimestampBefore(twoYearsAgo);

        // Elimina los registros históricos de signos vitales que tienen más de 2 años
        long deletedCount = vitalSignRecordRepository.deleteByReceivedAtBefore(twoYearsAgo);

        log.info("Limpieza de datos antiguos completada. Se eliminaron {} registros de signos vitales anteriores a {}.",
                deletedCount, twoYearsAgo);

        // Aquí podrías emitir un evento 'DataCleanupEvent' si otros servicios necesitan saberlo.
    }



    //real
    //@Scheduled(fixedRateString = "${tasks.inactive-check.rate:21600000}") // Cada 6 horas por defecto
    //prueba
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional(value = "cockroachTransactionManager", readOnly = true)
    public void checkInactiveDevices() {
        log.info("Iniciando verificación de dispositivos inactivos...");
        // Calcula el timestamp de hace 24 horas
        Instant twentyFourHoursAgo = Instant.now().minus(Duration.ofHours(24));

        // Busca en la base de datos todos los dispositivos cuya última actualización fue ANTES de ese momento
        var inactiveDevices = deviceLastSeenRepository.findByLastSeenTimestampBefore(twentyFourHoursAgo);

        if (inactiveDevices.isEmpty()) {
            log.info("No se encontraron dispositivos inactivos.");
            return;
        }

        inactiveDevices.forEach(device -> {
            log.warn("¡Dispositivo inactivo detectado! DeviceId: {}, Última vez visto: {}",
                    device.getDeviceId(), device.getLastSeenTimestamp());

            // Emitir el evento DeviceOfflineAlert
            AlertEventDto offlineAlert = new AlertEventDto();
            offlineAlert.setAlertId("off-" + UUID.randomUUID().toString());
            offlineAlert.setType("DeviceOfflineAlert");
            offlineAlert.setDeviceId(device.getDeviceId());
            offlineAlert.setTimestamp(Instant.now());

            alertProducer.publishAlertEvent(offlineAlert);
        });

        log.info("Verificación de dispositivos inactivos completada. Se encontraron {} dispositivos inactivos.", inactiveDevices.size());
    }
}
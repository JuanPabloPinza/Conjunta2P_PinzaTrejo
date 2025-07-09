package ec.edu.espe.conjunta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyReportDto implements Serializable {
    private String reportId; // ID único del reporte
    private Instant reportTimestamp; // Cuando se generó el reporte
    private Instant periodStart; // Inicio del periodo cubierto por el reporte (ej. hace 24h)
    private Instant periodEnd;   // Fin del periodo cubierto por el reporte (ej. ahora)

    // Estadísticas detalladas por tipo de signo vital
    // Clave: tipo de signo (ej. "heart-rate")
    // Valor: Estadísticas del reporte (promedio, min, max, count)
    private Map<String, DailyReportStats> vitalSignStats;

    // Clase interna para contener las estadísticas resumidas
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyReportStats implements Serializable {
        private double average;
        private double max;
        private double min;
        private long count;
    }

    // Constructor para facilitar la creación desde DoubleSummaryStatistics
    /*
    public DailyReportDto(Instant periodStart, Instant periodEnd, Map<String, DoubleSummaryStatistics> rawStats) {
        this.reportId = "rpt-" + UUID.randomUUID().toString();
        this.reportTimestamp = Instant.now();
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.vitalSignStats = rawStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new DailyReportStats(entry.getValue().getAverage(),
                                entry.getValue().getMax(),
                                entry.getValue().getMin(),
                                entry.getValue().getCount())
                ));
    }*/
}

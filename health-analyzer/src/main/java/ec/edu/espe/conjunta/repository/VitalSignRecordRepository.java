package ec.edu.espe.conjunta.repository;

import ec.edu.espe.conjunta.entity.VitalSignRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface VitalSignRecordRepository extends JpaRepository<VitalSignRecord, Long> {

    /**
     * Encuentra registros de signos vitales dentro de un rango de tiempo.
     * @param start Inicio del rango.
     * @param end Fin del rango.
     * @return Lista de VitalSignRecord en el rango.
     */
    List<VitalSignRecord> findByReceivedAtBetween(Instant start, Instant end);

    /**
     * Elimina registros de signos vitales anteriores a una fecha específica (para limpieza).
     * @param cutoffTimestamp Fecha límite.
     * @return El número de registros eliminados.
     */
    long deleteByReceivedAtBefore(Instant cutoffTimestamp);
}
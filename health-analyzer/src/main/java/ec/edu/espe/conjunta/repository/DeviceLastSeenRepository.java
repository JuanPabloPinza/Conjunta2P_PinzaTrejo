package ec.edu.espe.conjunta.repository;

import ec.edu.espe.conjunta.entity.DeviceLastSeen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface DeviceLastSeenRepository extends JpaRepository<DeviceLastSeen, String> {

    /**
     * Encuentra todos los registros de dispositivos cuya última marca de tiempo
     * es anterior a la fecha proporcionada.
     * @param timestamp Límite de tiempo.
     * @return Una lista de dispositivos inactivos.
     */
    List<DeviceLastSeen> findByLastSeenTimestampBefore(Instant timestamp);
}
package ec.edu.espe.conjunta.repository;

import ec.edu.espe.conjunta.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {
    // Para el endpoint GET /vital-signs/{deviceId}
    List<VitalSign> findByDeviceIdOrderByTimestampDesc(String deviceId);
}
package ec.edu.espe.conjunta.repository.resilience;

import ec.edu.espe.conjunta.entity.resilience.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PendingEventRepository extends JpaRepository<PendingEvent, Long> {
    List<PendingEvent> findByRetryCountLessThan(int maxRetries);
}
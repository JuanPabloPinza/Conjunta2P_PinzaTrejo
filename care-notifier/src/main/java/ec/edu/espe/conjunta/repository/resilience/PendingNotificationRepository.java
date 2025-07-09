package ec.edu.espe.conjunta.repository.resilience;

import ec.edu.espe.conjunta.entity.resilience.PendingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface PendingNotificationRepository extends JpaRepository<PendingNotification, Long> {
    List<PendingNotification> findByRetryCountLessThan(int maxRetries);
}
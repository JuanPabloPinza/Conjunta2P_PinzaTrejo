package ec.edu.espe.conjunta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.entity.resilience.PendingNotification;
import ec.edu.espe.conjunta.repository.resilience.PendingNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PendingNotificationService {
    private final PendingNotificationRepository pendingNotificationRepository;
    private final NotificationDispatcher notificationDispatcher; // Para reenviar
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;

    @Transactional("resilienceTransactionManager")
    public void storeNotificationForRetry(String recipient, String subject, String body, String channel) {
        try {
            PendingNotification pendingNotification = new PendingNotification(recipient, subject, body, channel);
            pendingNotificationRepository.save(pendingNotification);
            log.warn("Notificación almacenada localmente para reintento. Total pendientes: {}", pendingNotificationRepository.count());
        } catch (Exception e) {
            log.error("¡FALLO CRÍTICO! No se pudo guardar la notificación pendiente en la BBDD de resiliencia.", e);
        }
    }

    @Scheduled(fixedRate = 30000) // Reintenta cada 30 segundos
    @Transactional("resilienceTransactionManager")
    public void retryFailedNotifications() {
        var notificationsToRetry = pendingNotificationRepository.findByRetryCountLessThan(MAX_RETRIES);
        if (notificationsToRetry.isEmpty()) {
            return;
        }

        log.info("Reintentando {} notificaciones pendientes...", notificationsToRetry.size());
        for (PendingNotification notification : notificationsToRetry) {
            try {
                notificationDispatcher.dispatchImmediate(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getBody()
                );
                pendingNotificationRepository.delete(notification); // Eliminar si el envío es exitoso
                log.info("Notificación pendiente ID {} reenviada y eliminada exitosamente.", notification.getId());
            } catch (Exception e) {
                log.error("Fallo al reintentar notificación ID {}. Incrementando contador.", notification.getId(), e);
                notification.setRetryCount(notification.getRetryCount() + 1);
                pendingNotificationRepository.save(notification);
            }
        }
    }
}
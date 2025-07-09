package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.entity.Notification;
import ec.edu.espe.conjunta.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import ec.edu.espe.conjunta.service.PendingNotificationService;


@Service
@Slf4j
//@RequiredArgsConstructor

public class NotificationDispatcher {

    private final NotificationRepository notificationRepository;


    @Lazy
    @Autowired
    private PendingNotificationService pendingNotificationService;

    public NotificationDispatcher(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void dispatchImmediate(String recipient, String subject, String body) {
        // Simular envío inmediato a todos los canales
        log.info("Despachando notificación inmediata para: {}", recipient);
        sendEmail(recipient, subject, body);
        sendSms(recipient, body);
        sendPush(body);
    }

    @Transactional("cockroachTransactionManager")
    public void sendEmail(String recipient, String subject, String body) {
        try {
            log.info("--- SIMULANDO ENVÍO DE CORREO ---");
            log.info("Para: {}", recipient);
            log.info("Asunto: {}", subject);
            log.info("Cuerpo: {}", body);
            log.info("---------------------------------");
            // Aquí iría la llamada real a una API externa de email.
            // Si la llamada real falla, lo guardaríamos para reintentar.

            saveNotificationRecord("EMAIL", recipient, "SENT");
        } catch (Exception e) {
            log.error("Fallo al enviar correo a {}. Almacenando para reintento.", recipient, e);
            pendingNotificationService.storeNotificationForRetry(recipient, subject, body, "EMAIL");
            saveNotificationRecord("EMAIL", recipient, "FAILED");
        }
    }

    @Transactional("cockroachTransactionManager")
    public void sendSms(String recipient, String body) {
        try {
            log.info("--- SIMULANDO ENVÍO DE SMS ---");
            log.info("Para: {}", recipient);
            log.info("Cuerpo: {}", body);
            log.info("-----------------------------");
            // Aquí iría la llamada real a una API externa de SMS.
            saveNotificationRecord("SMS", recipient, "SENT");
        } catch (Exception e) {
            log.error("Fallo al enviar SMS a {}. Almacenando para reintento.", recipient, e);
            pendingNotificationService.storeNotificationForRetry(recipient, "SMS Alert", body, "SMS");
            saveNotificationRecord("SMS", recipient, "FAILED");
        }
    }

    @Transactional("cockroachTransactionManager")
    public void sendPush(String body) {
        try {
            log.info("--- SIMULANDO ENVÍO DE PUSH ---");
            log.info("Cuerpo: {}", body);
            log.info("-------------------------------");
            // Aquí iría la llamada real a una API externa de Push.
            saveNotificationRecord("PUSH", "ALL", "SENT"); // Simula push a todos
        } catch (Exception e) {
            log.error("Fallo al enviar Push. Almacenando para reintento.", e);
            pendingNotificationService.storeNotificationForRetry("ALL", "Push Alert", body, "PUSH");
            saveNotificationRecord("PUSH", "ALL", "FAILED");
        }
    }


    private void saveNotificationRecord(String channel, String recipient, String status) {
        Notification record = new Notification();
        record.setNotificationId("notif-" + UUID.randomUUID().toString());
        record.setEventType(channel); // O el tipo de evento original
        record.setRecipient(recipient);
        record.setStatus(status);
        record.setTimestamp(Instant.now());
        notificationRepository.save(record);
        log.debug("Registro de notificación guardado en DB: {}", record.getNotificationId());
    }
}
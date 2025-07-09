package ec.edu.espe.conjunta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class LowPriorityBatchService {
    private final ConcurrentLinkedQueue<String> batchQueue = new ConcurrentLinkedQueue<>();
    private final NotificationDispatcher dispatcher;

    public void addNotificationToBatch(String recipient, String subject, String body) {
        String formattedNotification = String.format("Para: %s | Asunto: %s | Cuerpo: %s", recipient, subject, body);
        batchQueue.add(formattedNotification);
    }

    //real
    // @Scheduled(fixedRate = 30 * 60 * 1000) // Cada 30 minutos
    //pruebas
    @Scheduled(fixedRate = 6000)
    public void sendBatchedNotifications() {
        if (batchQueue.isEmpty()) {
            return;
        }
        log.info("Enviando lote de {} notificaciones de baja prioridad...", batchQueue.size());
        StringBuilder summaryBody = new StringBuilder("Resumen de alertas y notificaciones de baja prioridad:\n\n");
        batchQueue.forEach(notification -> summaryBody.append("- ").append(notification).append("\n"));
        batchQueue.clear();

        dispatcher.sendEmail("team-digest@hospital.com", "Resumen de Alertas", summaryBody.toString());
    }
}
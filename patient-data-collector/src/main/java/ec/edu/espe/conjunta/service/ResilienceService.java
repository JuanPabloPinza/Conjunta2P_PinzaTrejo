package ec.edu.espe.conjunta.service;

import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class ResilienceService {

    private final ConcurrentLinkedQueue<NewVitalSignEvent> failedEvents = new ConcurrentLinkedQueue<>();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Método para ser llamado cuando la publicación inicial falla
    public void storeEventForRetry(NewVitalSignEvent event) {
        failedEvents.add(event);
        log.warn("Event stored locally. Current queue size: {}", failedEvents.size());
    }

    // Tarea programada para reintentar enviar eventos cada minuto
    @Scheduled(fixedRate = 60000) // 60,000 ms = 1 minuto
    public void retryFailedEvents() {
        if (!failedEvents.isEmpty()) {
            log.info("Retrying to send {} stored events...", failedEvents.size());
            // Usamos un iterador para poder remover elementos de forma segura
            failedEvents.removeIf(event -> {
                try {
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.GLOBAL_EXCHANGE_NAME,
                            RabbitMQConfig.NEW_VITAL_SIGN_ROUTING_KEY,
                            event
                    );
                    log.info("Successfully re-sent event for deviceId: {}", event.getDeviceId());
                    return true; // Retorna true para removerlo de la cola
                } catch (Exception e) {
                    log.error("Retry failed for event {}. Will try again later.", event.getEventId());
                    return false; // Retorna false para mantenerlo en la cola
                }
            });
        }
    }
}
package ec.edu.espe.conjunta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import ec.edu.espe.conjunta.entity.resilience.PendingEvent;
import ec.edu.espe.conjunta.repository.resilience.PendingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PendingEventService {
    private final PendingEventRepository pendingEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3;

    @Transactional("resilienceTransactionManager")
    public void storeEventForRetry(String eventJson, String exchange, String routingKey) {
        try {
            PendingEvent pendingEvent = new PendingEvent(eventJson, exchange, routingKey);
            pendingEventRepository.save(pendingEvent);
            log.warn("Evento almacenado localmente para reintento. Total pendientes: {}", pendingEventRepository.count());
        } catch (Exception e) {
            log.error("¡FALLO CRÍTICO! No se pudo guardar el evento en la BBDD de resiliencia.", e);
        }
    }

    @Scheduled(fixedRate = 30000) // Reintenta cada 30 segundos
    @Transactional("resilienceTransactionManager")
    public void retryFailedEvents() {
        var eventsToRetry = pendingEventRepository.findByRetryCountLessThan(MAX_RETRIES);
        if (eventsToRetry.isEmpty()) {
            return;
        }

        log.info("Reintentando {} eventos pendientes...", eventsToRetry.size());
        for (PendingEvent event : eventsToRetry) {
            try {
                NewVitalSignEvent eventDto = objectMapper.readValue(event.getEventJson(), NewVitalSignEvent.class);
                rabbitTemplate.convertAndSend(event.getExchangeName(), event.getRoutingKey(), eventDto);
                pendingEventRepository.delete(event); // Eliminar si el envío es exitoso
                log.info("Evento ID {} reenviado y eliminado exitosamente.", eventDto.getEventId());
            } catch (Exception e) {
                log.error("Fallo al reintentar evento {}. Incrementando contador.", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                pendingEventRepository.save(event);
            }
        }
    }
}
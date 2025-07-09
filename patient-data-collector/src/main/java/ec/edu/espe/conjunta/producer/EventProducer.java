package ec.edu.espe.conjunta.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import ec.edu.espe.conjunta.service.PendingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProducer {
    private final RabbitTemplate rabbitTemplate;
    private final PendingEventService pendingEventService;
    private final ObjectMapper objectMapper;

    public void publishNewVitalSignEvent(NewVitalSignEvent event) {
        try {
            log.info("Publicando NewVitalSignEvent: {}", event);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.GLOBAL_EXCHANGE_NAME,
                    RabbitMQConfig.NEW_VITAL_SIGN_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Fallo al publicar evento a RabbitMQ. Almacenando localmente.", e);
            try {
                String eventJson = objectMapper.writeValueAsString(event);
                pendingEventService.storeEventForRetry(eventJson, RabbitMQConfig.GLOBAL_EXCHANGE_NAME, RabbitMQConfig.NEW_VITAL_SIGN_ROUTING_KEY);
            } catch (JsonProcessingException jsonEx) {
                log.error("¡FALLO CRÍTICO! No se pudo serializar el evento para guardarlo localmente.", jsonEx);
            }
        }
    }
}
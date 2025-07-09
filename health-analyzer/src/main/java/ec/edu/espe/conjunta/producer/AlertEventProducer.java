package ec.edu.espe.conjunta.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.AlertEventDto;
import ec.edu.espe.conjunta.service.PendingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlertEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final PendingEventService pendingEventService;
    private final ObjectMapper objectMapper;

    /**
     * Publica un nuevo evento de alerta médica.
     * Si RabbitMQ falla, delega al PendingEventService para almacenar el evento
     * en la base de datos de resiliencia para un reintento posterior.
     *
     * @param event El DTO del evento de alerta a publicar.
     */
    public void publishAlertEvent(AlertEventDto event) {
        // Creamos una routing key dinámica basada en el tipo de alerta.
        // Ejemplo: "event.alert.CriticalHeartRateAlert"
        String routingKey = "event.alert." + event.getType();

        try {
            log.info("Publicando AlertEvent al exchange '{}' con routing key '{}': {}",
                    RabbitMQConfig.ALERT_EVENTS_EXCHANGE, routingKey, event);

            // Enviamos el evento al exchange de alertas con su routing key específica.
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ALERT_EVENTS_EXCHANGE,
                    routingKey,
                    event
            );
        } catch (Exception e) {
            log.error("Fallo al publicar evento de alerta a RabbitMQ. Almacenando localmente.", e);
            try {
                // Si falla, serializamos el evento a JSON para guardarlo.
                String eventJson = objectMapper.writeValueAsString(event);
                // Llamamos al servicio de resiliencia con los datos correctos.
                pendingEventService.storeEventForRetry(
                        eventJson,
                        RabbitMQConfig.ALERT_EVENTS_EXCHANGE,
                        routingKey
                );
            } catch (JsonProcessingException jsonEx) {
                // Este es un error grave: si no podemos ni serializar el evento, no podemos guardarlo.
                log.error("¡FALLO CRÍTICO! No se pudo serializar el evento de alerta para guardarlo localmente.", jsonEx);
            }
        }
    }


    /**
     * Publica un evento genérico (ej. un reporte) a un exchange con una routing key específica.
     * Este método es versátil y se encarga de la resiliencia.
     *
     * @param event El objeto evento (puede ser AlertEventDto o DailyReportDto, etc.)
     * @param routingKey La routing key específica para este evento.
     */
    public void publishGenericEvent(Object event, String routingKey) {
        try {
            log.info("Publicando evento genérico a exchange '{}' con routing key '{}': {}",
                    RabbitMQConfig.REPORT_EVENTS_EXCHANGE, routingKey, event); // Usamos REPORT_EVENTS_EXCHANGE

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.REPORT_EVENTS_EXCHANGE, // Usamos el exchange de reportes
                    routingKey,
                    event
            );
        } catch (Exception e) {
            log.error("Fallo al publicar evento genérico a RabbitMQ. Almacenando localmente.", e);
            try {
                String eventJson = objectMapper.writeValueAsString(event);
                pendingEventService.storeEventForRetry(
                        eventJson,
                        RabbitMQConfig.REPORT_EVENTS_EXCHANGE, // Guardamos con el exchange correcto
                        routingKey
                );
            } catch (JsonProcessingException jsonEx) {
                log.error("¡FALLO CRÍTICO! No se pudo serializar el evento genérico para guardarlo localmente.", jsonEx);
            }
        }
    }


}

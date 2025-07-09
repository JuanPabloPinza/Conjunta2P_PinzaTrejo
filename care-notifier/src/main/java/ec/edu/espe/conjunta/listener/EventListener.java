package ec.edu.espe.conjunta.listener;

import ec.edu.espe.conjunta.config.RabbitMQConfig;
import ec.edu.espe.conjunta.dto.AlertEventDto;
import ec.edu.espe.conjunta.dto.DailyReportDto;
import ec.edu.espe.conjunta.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = RabbitMQConfig.NOTIFIER_QUEUE)
public class EventListener {
    private final NotificationService notificationService;

    /**
     * Listener específico para eventos de tipo AlertEventDto.
     * Spring enrutará aquí los mensajes cuyo header __TypeId__ coincida con AlertEventDto.
     */
    //@RabbitListener(queues = RabbitMQConfig.NOTIFIER_QUEUE)
    @RabbitHandler
    public void handleAlertEvent(@Payload AlertEventDto event) {
        log.info("Evento de Alerta recibido y procesado: {}", event.getAlertId());
        try {
            notificationService.processAlert(event);
        } catch (Exception e) {
            log.error("Error al procesar AlertEventDto con ID {}: {}", event.getAlertId(), e.getMessage(), e);
        }
    }

    /**
     * Listener específico para eventos de tipo DailyReportDto.
     * Spring enrutará aquí los mensajes cuyo header __TypeId__ coincida con DailyReportDto.
     */
    //@RabbitListener(queues = RabbitMQConfig.NOTIFIER_QUEUE)
    @RabbitHandler
    public void handleDailyReportEvent(@Payload DailyReportDto event) {
        log.info("Evento de Reporte Diario recibido y procesado: {}", event.getReportId());
        try {
            notificationService.processDailyReport(event);
        } catch (Exception e) {
            log.error("Error al procesar DailyReportDto con ID {}: {}", event.getReportId(), e.getMessage(), e);
        }
    }


    /**
     * Opcional: Un handler por defecto para tipos no reconocidos.
     * @param message El objeto deserializado que no coincide con otros handlers.
     */
    @RabbitHandler(isDefault = true)
    public void handleUnknownEvent(Object message) {
        log.warn("Evento de tipo desconocido recibido y capturado por el handler por defecto: {}", message.getClass().getName());
    }
}
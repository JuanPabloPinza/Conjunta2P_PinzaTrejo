package ec.edu.espe.conjunta.config;

import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

import ec.edu.espe.conjunta.dto.AlertEventDto;
import ec.edu.espe.conjunta.dto.DailyReportDto;

@Configuration
public class RabbitMQConfig {
    public static final String ALERT_EVENTS_EXCHANGE = "medical.alerts.exchange";
    public static final String REPORT_EVENTS_EXCHANGE = "medical.reports.exchange";

    // Cola única para este consumidor
    public static final String NOTIFIER_QUEUE = "care_notifier.events.queue";

    @Bean
    public Queue notifierQueue() {
        return new Queue(NOTIFIER_QUEUE, true);
    }

    @Bean
    public TopicExchange alertEventsExchange() {
        return new TopicExchange(ALERT_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange reportEventsExchange() {
        return new TopicExchange(REPORT_EVENTS_EXCHANGE);
    }




    // Binding para TODAS las alertas (usando el comodín #)
    @Bean
    public Binding alertBinding(Queue notifierQueue, TopicExchange alertEventsExchange) {
        return BindingBuilder.bind(notifierQueue).to(alertEventsExchange).with("event.alert.#");
    }

    // Binding para TODOS los reportes (usando el comodín #)
    @Bean
    public Binding reportBinding(Queue notifierQueue, TopicExchange reportEventsExchange) {
        return BindingBuilder.bind(notifierQueue).to(reportEventsExchange).with("event.report.#");
    }

    /**
     * Define un mapeador de clases para que RabbitMQ sepa qué clase corresponde a cada ID de tipo.
     * Esto resuelve problemas de deserialización con tipos complejos.
     * @return El mapeador de clases configurado.
     */
    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("ec.edu.espe.conjunta.dto");
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Le damos un "alias" corto a cada DTO que viaja por RabbitMQ
        idClassMapping.put("newVitalSign", NewVitalSignEvent.class); // Para HealthAnalyzer
        idClassMapping.put("alert", AlertEventDto.class);
        idClassMapping.put("dailyReport", DailyReportDto.class);
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }



    @Bean
    public MessageConverter jsonMessageConverter(DefaultClassMapper classMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }

    // Este Factory configura todos los @RabbitListener de esta aplicación.
    // Le decimos explícitamente qué MessageConverter debe usar para la deserialización.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }






}

package ec.edu.espe.conjunta.config;
import ec.edu.espe.conjunta.dto.AlertEventDto;
import ec.edu.espe.conjunta.dto.DailyReportDto;
import ec.edu.espe.conjunta.dto.NewVitalSignEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Exchange del que vamos a LEER (debe coincidir con el del productor)
    public static final String VITAL_SIGN_EVENTS_EXCHANGE = "medical.events.exchange";
    // Exchange al que vamos a ESCRIBIR nuestras alertas
    public static final String ALERT_EVENTS_EXCHANGE = "medical.alerts.exchange";

    //-- EXCHANGE PARA REPORTES ---
    public static final String REPORT_EVENTS_EXCHANGE = "medical.reports.exchange";
    public static final String DAILY_REPORT_ROUTING_KEY = "event.report.daily"; // Routing key específica


    // --- CONFIGURACIÓN DE ENTRADA (CONSUMIDOR) ---
    public static final String VITAL_SIGN_QUEUE = "health_analyzer.vitalsign.queue";
    public static final String VITAL_SIGN_ROUTING_KEY = "event.vitalsign.new";

    @Bean
    public Queue vitalSignQueue() {
        return new Queue(VITAL_SIGN_QUEUE, true); // durable
    }

    @Bean
    public TopicExchange globalEventsExchange() {
        return new TopicExchange(VITAL_SIGN_EVENTS_EXCHANGE);
    }

    @Bean
    public Binding vitalSignBinding(Queue vitalSignQueue, TopicExchange globalEventsExchange) {
        return BindingBuilder.bind(vitalSignQueue).to(globalEventsExchange).with(VITAL_SIGN_ROUTING_KEY);
    }

    // --- CONFIGURACIÓN DE SALIDA (PRODUCTOR) ---
    @Bean
    public TopicExchange alertEventsExchange() {
        return new TopicExchange(ALERT_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange reportEventsExchange() {
        return new TopicExchange(REPORT_EVENTS_EXCHANGE);
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

    /**
     * Configura la fábrica de listeners para usar nuestro convertidor JSON personalizado.
     * Esto asegura que los @RabbitListener puedan deserializar correctamente los DTOs.
     * @param connectionFactory La conexión a RabbitMQ.
     * @param jsonMessageConverter Nuestro convertidor configurado.
     * @return La fábrica de listeners.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
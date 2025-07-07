package ec.edu.espe.conjunta.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombre del exchange donde se publicarán todos los eventos del sistema
    public static final String GLOBAL_EXCHANGE_NAME = "medical.events.exchange";

    // Routing key para nuevos signos vitales
    public static final String NEW_VITAL_SIGN_ROUTING_KEY = "event.vitalsign.new";

    @Bean
    public TopicExchange globalExchange() {
        return new TopicExchange(GLOBAL_EXCHANGE_NAME);
    }
}

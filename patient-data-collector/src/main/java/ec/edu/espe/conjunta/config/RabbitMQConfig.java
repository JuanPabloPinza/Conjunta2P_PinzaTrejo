package ec.edu.espe.conjunta.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory; // ¡IMPORTAR!
import org.springframework.amqp.rabbit.core.RabbitTemplate; // ¡IMPORTAR!
import org.springframework.amqp.core.AmqpTemplate; // ¡IMPORTAR!
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}

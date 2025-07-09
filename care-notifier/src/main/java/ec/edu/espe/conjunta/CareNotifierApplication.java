package ec.edu.espe.conjunta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling // Habilita el programador de tareas
public class CareNotifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(CareNotifierApplication.class, args);
	}

}

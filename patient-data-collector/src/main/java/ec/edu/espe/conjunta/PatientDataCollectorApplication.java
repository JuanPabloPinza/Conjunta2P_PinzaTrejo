package ec.edu.espe.conjunta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class PatientDataCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatientDataCollectorApplication.class, args);
	}

}

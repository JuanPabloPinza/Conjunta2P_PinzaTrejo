package ec.edu.espe.conjunta.repository;

import ec.edu.espe.conjunta.entity.MedicalAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalAlertRepository extends JpaRepository<MedicalAlert, String> {}
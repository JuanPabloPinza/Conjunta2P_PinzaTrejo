package ec.edu.espe.conjunta.controller;


import ec.edu.espe.conjunta.dto.VitalSignRequest;
import ec.edu.espe.conjunta.entity.VitalSign;
import ec.edu.espe.conjunta.service.VitalSignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/vital-signs") // Establece la ruta base para todos los métodos de este controlador
@RequiredArgsConstructor
@Slf4j
public class VitalSignController {

    private final VitalSignService vitalSignService;

    /**
     * Endpoint para recibir y procesar un nuevo signo vital.
     * Corresponde a: POST /vital-signs
     * Cumple con el requisito B.1.a
     */
    @PostMapping
    public ResponseEntity<?> receiveVitalSign(@Valid @RequestBody VitalSignRequest request) {
        try {
            // Llama al servicio, que ahora devuelve la entidad guardada
            VitalSign savedVitalSign = vitalSignService.processAndStoreVitalSign(request);

            // Devuelve una respuesta 201 CREATED con el objeto en el cuerpo
            return new ResponseEntity<>(savedVitalSign, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Captura errores de validación y devuelve un 400 Bad Request
            log.warn("Petición rechazada por validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Captura cualquier otro error inesperado y devuelve un 500 Internal Server Error
            log.error("Error interno al procesar el signo vital.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error interno.");
        }
    }

    /**
     * Endpoint para obtener el historial de signos vitales de un dispositivo.
     * Corresponde a: GET /vital-signs/{deviceId}
     * Cumple con el requisito B.1.a
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<List<VitalSign>> getHistoryByDevice(@PathVariable String deviceId) {
        List<VitalSign> history = vitalSignService.getHistoryByDevice(deviceId);

        // Si no se encuentra historial, se puede devolver una lista vacía con un 200 OK,
        // que es una práctica común de REST.
        return ResponseEntity.ok(history);
    }

    @GetMapping
    public ResponseEntity<List<VitalSign>> getAllHistory() {
        log.info("Solicitando todo el historial de signos vitales.");
        List<VitalSign> allSigns = vitalSignService.getAllVitalSigns();
        return ResponseEntity.ok(allSigns);
    }



}
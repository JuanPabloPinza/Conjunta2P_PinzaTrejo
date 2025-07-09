package ec.edu.espe.conjunta.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
@Slf4j
public class MockApiController {
    @PostMapping("/email")
    public ResponseEntity<String> receiveEmail(@RequestBody String emailBody) {
        log.info("--- MOCK EMAIL API: Correo recibido ---\n{}", emailBody);
        return ResponseEntity.ok("Email simulado y recibido.");
    }

    @PostMapping("/sms")
    public ResponseEntity<String> receiveSms(@RequestBody String smsBody) {
        log.info("--- MOCK SMS API: SMS recibido ---\n{}", smsBody);
        return ResponseEntity.ok("SMS simulado y recibido.");
    }
}

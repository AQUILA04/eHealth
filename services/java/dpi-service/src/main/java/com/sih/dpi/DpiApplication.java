package com.sih.dpi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du DPI Service (Dossier Patient Informatisé — Module II).
 *
 * <p>Port par défaut : 8083
 * <p>Console H2 : http://localhost:8083/h2-console
 * <p>Actuator health : http://localhost:8083/actuator/health
 */
@SpringBootApplication
public class DpiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DpiApplication.class, args);
    }
}

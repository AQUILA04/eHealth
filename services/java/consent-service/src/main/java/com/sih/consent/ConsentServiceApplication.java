package com.sih.consent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class ConsentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsentServiceApplication.class, args);
    }
}

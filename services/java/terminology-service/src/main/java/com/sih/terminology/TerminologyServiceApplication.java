package com.sih.terminology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class TerminologyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerminologyServiceApplication.class, args);
    }
}

package com.sih.empi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class EmpiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmpiServiceApplication.class, args);
    }
}

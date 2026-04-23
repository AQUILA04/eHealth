package com.sih.cpoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class CpoeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CpoeServiceApplication.class, args);
    }
}

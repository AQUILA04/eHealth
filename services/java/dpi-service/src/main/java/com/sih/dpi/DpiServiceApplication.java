package com.sih.dpi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class DpiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DpiServiceApplication.class, args);
    }
}

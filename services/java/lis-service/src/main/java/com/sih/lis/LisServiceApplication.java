package com.sih.lis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class LisServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LisServiceApplication.class, args);
    }
}

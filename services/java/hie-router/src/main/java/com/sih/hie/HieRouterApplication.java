package com.sih.hie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sih"})
public class HieRouterApplication {
    public static void main(String[] args) {
        SpringApplication.run(HieRouterApplication.class, args);
    }
}

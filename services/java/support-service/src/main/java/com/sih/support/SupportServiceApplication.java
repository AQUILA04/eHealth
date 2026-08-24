package com.sih.support;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class SupportServiceApplication { public static void main(String[] a){SpringApplication.run(SupportServiceApplication.class,a);} }

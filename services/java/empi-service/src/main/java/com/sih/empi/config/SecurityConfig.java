package com.sih.empi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour le mode mock (développement).
 *
 * <p><b>ATTENTION :</b> Cette configuration désactive l'authentification.
 * Elle est réservée au profil de développement. En production, remplacer
 * par une configuration OAuth2/JWT avec Keycloak.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Autoriser la console H2 (utilise des frames)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics en mode mock
                .requestMatchers(
                    "/api/v1/empi/**",
                    "/actuator/**",
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}

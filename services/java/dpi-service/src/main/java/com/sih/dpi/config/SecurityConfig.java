package com.sih.dpi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les profils {@code mock} et {@code unsecure}.
 *
 * <p>Cette configuration est <b>sans authentification</b> : tous les endpoints sont
 * accessibles sans token. Elle est destinée au développement local et aux tests.
 *
 * <p>Pour activer la sécurité Keycloak, démarrer avec le profil {@code secure} :
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.profiles=secure
 * </pre>
 *
 * @see com.sih.dpi.security.SecureSecurityConfig
 */
@Configuration
@EnableWebSecurity
@Profile({"mock", "unsecure", "default"})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}

package com.sih.tenant.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("secure")
public class SecureSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics (monitoring + signup self-serve)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/signup/plans").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/signup").permitAll()
                .requestMatchers("/api/v1/internal/quota/**").permitAll()

                // Endpoints d'administration des tenants (CRUD) réservés aux Super Admins / Admin System
                .requestMatchers(HttpMethod.GET, "/api/v1/tenants/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN_SYSTEM")
                .requestMatchers(HttpMethod.POST, "/api/v1/tenants")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN_SYSTEM")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tenants/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN_SYSTEM")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tenants/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN_SYSTEM")

                .requestMatchers("/api/v1/subscriptions/plans/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN_SYSTEM")
                .requestMatchers("/api/v1/signup/requests/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN_SYSTEM")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtRoleConverter());
        return converter;
    }
}

package com.sih.lis.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("secure")
public class SecureSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/lis/**").hasAnyRole("MEDECIN", "INFIRMIER", "BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/lis/orders").hasAnyRole("MEDECIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/lis/orders/*/collect").hasAnyRole("INFIRMIER", "BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/lis/orders/*/receive", "/api/v1/lis/orders/*/validate").hasAnyRole("BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/lis/orders/*/results", "/api/v1/lis/orders/*/critical-notification").hasAnyRole("BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/lis/blood-bank/transfusions").hasAnyRole("MEDECIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/lis/blood-bank/units", "/api/v1/lis/blood-bank/transfusions/*/reaction").hasAnyRole("BIOLOGISTE", "INFIRMIER", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/lis/blood-bank/transfusions/*/crossmatch", "/api/v1/lis/blood-bank/transfusions/*/issue").hasAnyRole("BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/lis/blood-bank/transfusions/*/complete").hasAnyRole("INFIRMIER", "BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers("/api/v1/lis/**").hasAnyRole("BIOLOGISTE", "SUPER_ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RealmRoleConverter());
        return converter;
    }

    private static final class RealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) return Collections.emptyList();
            return ((List<String>) roles).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
        }
    }
}

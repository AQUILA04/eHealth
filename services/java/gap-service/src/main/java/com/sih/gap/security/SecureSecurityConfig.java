package com.sih.gap.security;

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

/**
 * Configuration de sécurité pour le profil {@code secure}.
 *
 * <p>Active l'authentification OAuth2/JWT via Keycloak et applique un RBAC strict
 * sur chaque endpoint du GAP service selon les rôles définis dans le realm {@code ehealth}.
 *
 * <p>Matrice des autorisations :
 * <ul>
 *   <li>Patients — lecture : MEDECIN, INFIRMIER, ADMIN_GAP, PHARMACIEN</li>
 *   <li>Patients — écriture : ADMIN_GAP</li>
 *   <li>Encounters (ADT) — lecture : MEDECIN, INFIRMIER, ADMIN_GAP</li>
 *   <li>Encounters (ADT) — écriture/admission/sortie : ADMIN_GAP</li>
 *   <li>Appointments — lecture : MEDECIN, INFIRMIER, ADMIN_GAP, PATIENT</li>
 *   <li>Appointments — écriture : ADMIN_GAP</li>
 * </ul>
 *
 * @see Profile
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile({"secure", "prod"})
public class SecureSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics (monitoring)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // ── Patients ──────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/gap/patients/**")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "ADMIN_GAP", "PHARMACIEN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/gap/patients")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/gap/patients/**")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/gap/patients/**")
                    .hasAnyRole("SUPER_ADMIN")

                // ── Encounters (ADT) ──────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/gap/encounters/**")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/gap/encounters")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/gap/encounters/*/transfer")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/gap/encounters/*/discharge")
                    .hasAnyRole("ADMIN_GAP", "MEDECIN", "SUPER_ADMIN")

                // ── Appointments ──────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/gap/appointments/**")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "ADMIN_GAP", "PATIENT", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/gap/appointments")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/gap/appointments/**")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/gap/appointments/**")
                    .hasAnyRole("ADMIN_GAP", "SUPER_ADMIN")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Convertisseur JWT qui extrait les rôles Keycloak du claim {@code realm_access.roles}
     * et les transforme en {@code GrantedAuthority} Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtRoleConverter());
        return converter;
    }
}

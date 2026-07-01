package com.sih.dpi.security;

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
 * Configuration de sécurité pour le profil {@code secure} du DPI service.
 *
 * <p>Active l'authentification OAuth2/JWT via Keycloak et applique un RBAC clinique strict.
 *
 * <p>Mappings réels du ClinicalEncounterController :
 * <ul>
 *   <li>POST   /api/v1/dpi/encounters</li>
 *   <li>GET    /api/v1/dpi/encounters/{id}</li>
 *   <li>GET    /api/v1/dpi/encounters/gap/{gapEncounterId}</li>
 *   <li>GET    /api/v1/dpi/encounters/patient/{patientRef}</li>
 *   <li>PUT    /api/v1/dpi/encounters/{id}/diagnosis</li>
 *   <li>PUT    /api/v1/dpi/encounters/{id}/close</li>
 *   <li>POST   /api/v1/dpi/encounters/{encounterId}/vital-signs</li>
 *   <li>GET    /api/v1/dpi/encounters/{encounterId}/vital-signs</li>
 *   <li>POST   /api/v1/dpi/encounters/{encounterId}/medications</li>
 *   <li>GET    /api/v1/dpi/encounters/{encounterId}/medications</li>
 *   <li>PATCH  /api/v1/dpi/encounters/medications/{orderId}/status</li>
 *   <li>POST   /api/v1/dpi/encounters/{encounterId}/lab-orders</li>
 *   <li>GET    /api/v1/dpi/encounters/{encounterId}/lab-orders</li>
 *   <li>PATCH  /api/v1/dpi/encounters/lab-orders/{orderId}/result</li>
 * </ul>
 *
 * <p>Matrice RBAC :
 * <ul>
 *   <li>ClinicalEncounters — lecture : MEDECIN, INFIRMIER, PHARMACIEN</li>
 *   <li>ClinicalEncounters — création/modification : MEDECIN</li>
 *   <li>VitalSigns — lecture : MEDECIN, INFIRMIER</li>
 *   <li>VitalSigns — saisie : INFIRMIER, MEDECIN</li>
 *   <li>MedicationOrders — lecture : MEDECIN, INFIRMIER, PHARMACIEN</li>
 *   <li>MedicationOrders — prescription : MEDECIN</li>
 *   <li>MedicationOrders — changement de statut : MEDECIN, PHARMACIEN</li>
 *   <li>LabOrders — lecture : MEDECIN, INFIRMIER, BIOLOGISTE</li>
 *   <li>LabOrders — prescription : MEDECIN</li>
 *   <li>LabOrders — saisie résultats : BIOLOGISTE</li>
 * </ul>
 */
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
                // Endpoints publics (monitoring)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // ── ClinicalEncounters ────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/dpi/encounters/*",
                    "/api/v1/dpi/encounters/gap/*",
                    "/api/v1/dpi/encounters/patient/*")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "PHARMACIEN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/dpi/encounters")
                    .hasAnyRole("MEDECIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/dpi/encounters/*/diagnosis",
                    "/api/v1/dpi/encounters/*/close")
                    .hasAnyRole("MEDECIN", "SUPER_ADMIN")

                // ── VitalSigns ────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/dpi/encounters/*/vital-signs")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/dpi/encounters/*/vital-signs")
                    .hasAnyRole("INFIRMIER", "MEDECIN", "SUPER_ADMIN")

                // ── MedicationOrders (CPOE) ───────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/dpi/encounters/*/medications")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "PHARMACIEN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/dpi/encounters/*/medications")
                    .hasAnyRole("MEDECIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/dpi/encounters/medications/*/status")
                    .hasAnyRole("MEDECIN", "PHARMACIEN", "SUPER_ADMIN")

                // ── LabOrders ─────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/dpi/encounters/*/lab-orders")
                    .hasAnyRole("MEDECIN", "INFIRMIER", "BIOLOGISTE", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/dpi/encounters/*/lab-orders")
                    .hasAnyRole("MEDECIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/dpi/encounters/lab-orders/*/result")
                    .hasAnyRole("BIOLOGISTE", "SUPER_ADMIN")

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

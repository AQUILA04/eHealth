package com.sih.rcm.security;

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

@Configuration @EnableWebSecurity @EnableMethodSecurity @Profile("secure")
public class SecureSecurityConfig {
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/rcm/**").hasAnyRole("COMPTABLE", "CAISSIER", "FACTURATION", "RESPONSABLE_FINANCIER", "SUPER_ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/v1/rcm/invoices").hasAnyRole("FACTURATION", "RESPONSABLE_FINANCIER", "SUPER_ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/v1/rcm/invoices/*/issue", "/api/v1/rcm/invoices/*/payments").hasAnyRole("CAISSIER", "RESPONSABLE_FINANCIER", "SUPER_ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/v1/rcm/claims", "/api/v1/rcm/claims/*/submit", "/api/v1/rcm/claims/*/adjudicate").hasAnyRole("FACTURATION", "RESPONSABLE_FINANCIER", "SUPER_ADMIN")
        .anyRequest().authenticated())
      .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter()))).build();
  }
  @Bean JwtAuthenticationConverter jwtAuthenticationConverter() { JwtAuthenticationConverter c = new JwtAuthenticationConverter(); c.setJwtGrantedAuthoritiesConverter(new RealmRoleConverter()); return c; }
  private static final class RealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override @SuppressWarnings("unchecked") public Collection<GrantedAuthority> convert(Jwt jwt) {
      Map<String,Object> access = jwt.getClaimAsMap("realm_access"); if (access == null || !(access.get("roles") instanceof List<?> roles)) return Collections.emptyList();
      return ((List<String>) roles).stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toSet());
    }
  }
}

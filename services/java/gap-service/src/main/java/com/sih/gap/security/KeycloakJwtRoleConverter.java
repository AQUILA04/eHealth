package com.sih.gap.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convertit les rôles Keycloak présents dans le JWT en {@link GrantedAuthority} Spring Security.
 *
 * <p>Keycloak place les rôles realm dans le claim {@code realm_access.roles}.
 * Ce convertisseur les préfixe avec {@code ROLE_} pour les rendre compatibles
 * avec les annotations {@code @PreAuthorize("hasRole('MEDECIN')")} de Spring Security.
 */
public class KeycloakJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess == null || !realmAccess.containsKey(ROLES_CLAIM)) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }
}

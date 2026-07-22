import Keycloak from 'keycloak-js'

/**
 * Instance Keycloak singleton pour l'application eHealth.
 *
 * Configuration cible le realm `ehealth` sur le serveur Keycloak local (port 8080).
 * En mode développement, le client `ehealth-frontend` est configuré sans secret
 * (public client) avec PKCE activé.
 */
const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'ehealth',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'ehealth-frontend',
})

export default keycloak

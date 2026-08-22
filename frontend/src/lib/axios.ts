import axios from 'axios'
import keycloak from '@/auth/keycloak'

/**
 * Client Axios configuré pour les appels aux APIs du SIH.
 * Injecte automatiquement le token Bearer Keycloak dans chaque requête.
 */
const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use(
  async (config) => {
    if (keycloak.token) {
      // Rafraîchir le token si expirant dans moins de 30s
      try {
        await keycloak.updateToken(30)
      } catch {
        keycloak.logout()
        return Promise.reject(new Error('Session expirée'))
      }
      config.headers.Authorization = `Bearer ${keycloak.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (!keycloak.authenticated) {
        keycloak.login()
      } else {
        console.error(
          'HTTP 401 Unauthorized received, but client is already authenticated. Avoiding infinite login redirect loop.'
        )
      }
    }
    return Promise.reject(error)
  }
)

export default apiClient

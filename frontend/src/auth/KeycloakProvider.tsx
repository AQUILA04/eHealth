import React, { createContext, useContext, useEffect, useState } from 'react'
import keycloak from './keycloak'

interface KeycloakContextValue {
  isAuthenticated: boolean
  isLoading: boolean
  token: string | undefined
  userInfo: {
    username: string
    email: string
    fullName: string
    roles: string[]
  } | null
  login: () => void
  logout: () => void
  hasRole: (role: string) => boolean
}

const KeycloakContext = createContext<KeycloakContextValue | null>(null)

// Activer Keycloak uniquement si VITE_AUTH_ENABLED !== 'false'
// Cela correspond au profil `unsecure` des services backend
const AUTH_ENABLED = import.meta.env.VITE_AUTH_ENABLED !== 'false'

// Contexte fictif pour le mode no-auth (profil unsecure)
const NO_AUTH_CONTEXT: KeycloakContextValue = {
  isAuthenticated: true,
  isLoading: false,
  token: undefined,
  userInfo: {
    username: 'dev-user',
    email: 'dev@ehealth.local',
    fullName: 'Utilisateur Dev',
    roles: ['MEDECIN', 'ADMIN_GAP', 'INFIRMIER'],
  },
  login: () => {},
  logout: () => {},
  hasRole: () => true,
}

export function KeycloakProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(!AUTH_ENABLED)
  const [isLoading, setIsLoading] = useState(AUTH_ENABLED)

  useEffect(() => {
    if (!AUTH_ENABLED) return

    keycloak
      .init({
        onLoad: 'login-required',
        checkLoginIframe: false,
        pkceMethod: 'S256',
      })
      .then((authenticated) => {
        setIsAuthenticated(authenticated)
        setIsLoading(false)

        // Rafraîchissement automatique du token avant expiration
        const interval = setInterval(() => {
          keycloak.updateToken(60).catch(() => {
            clearInterval(interval)
            keycloak.logout()
          })
        }, 30_000)
      })
      .catch(() => {
        setIsLoading(false)
      })
  }, [])

  if (!AUTH_ENABLED) {
    return (
      <KeycloakContext.Provider value={NO_AUTH_CONTEXT}>
        {children}
      </KeycloakContext.Provider>
    )
  }

  const userInfo = keycloak.tokenParsed
    ? {
        username: (keycloak.tokenParsed as Record<string, string>)['preferred_username'] || '',
        email: (keycloak.tokenParsed as Record<string, string>)['email'] || '',
        fullName: (keycloak.tokenParsed as Record<string, string>)['name'] || '',
        roles: (keycloak.tokenParsed as Record<string, string[]>)['realm_access']
          ? (keycloak.tokenParsed as Record<string, { roles: string[] }>)['realm_access'].roles
          : [],
      }
    : null

  const hasRole = (role: string) => userInfo?.roles.includes(role) ?? false

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-4">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-primary-600 border-t-transparent" />
          <p className="text-sm text-slate-500">Authentification en cours...</p>
        </div>
      </div>
    )
  }

  return (
    <KeycloakContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        token: keycloak.token,
        userInfo,
        login: () => keycloak.login(),
        logout: () => keycloak.logout({ redirectUri: window.location.origin }),
        hasRole,
      }}
    >
      {children}
    </KeycloakContext.Provider>
  )
}

export function useKeycloak() {
  const ctx = useContext(KeycloakContext)
  if (!ctx) throw new Error('useKeycloak must be used within KeycloakProvider')
  return ctx
}

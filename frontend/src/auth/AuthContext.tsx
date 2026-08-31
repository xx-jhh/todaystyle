import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { clearToken, getToken, setToken, UNAUTHORIZED_EVENT } from '../api/client'

interface AuthState {
  isAuthenticated: boolean
  signIn: (accessToken: string) => void
  signOut: () => void
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => getToken())

  useEffect(() => {
    function handleUnauthorized() {
      setTokenState(null)
    }
    window.addEventListener(UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [])

  const value = useMemo<AuthState>(
    () => ({
      isAuthenticated: token !== null,
      signIn: (accessToken: string) => {
        setToken(accessToken)
        setTokenState(accessToken)
      },
      signOut: () => {
        clearToken()
        setTokenState(null)
      },
    }),
    [token],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}

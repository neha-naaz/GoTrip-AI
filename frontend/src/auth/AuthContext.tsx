import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react"
import * as authApi from "@/api/auth"
import { getMe } from "@/api/users"
import type { AuthResponse, AuthUser, LoginPayload, RegisterPayload } from "@/api/types"

const TOKEN_KEY = "tripflow_token"
const USER_KEY = "tripflow_user"

type AuthContextValue = {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  isBootstrapping: boolean
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => void
  refreshMe: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function toAuthUser(response: AuthResponse): AuthUser {
  return {
    userId: response.userId,
    email: response.email,
    role: response.role,
  }
}

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser())
  const [isBootstrapping, setIsBootstrapping] = useState(Boolean(localStorage.getItem(TOKEN_KEY)))

  const persistSession = useCallback((response: AuthResponse) => {
    const nextUser = toAuthUser(response)
    localStorage.setItem(TOKEN_KEY, response.accessToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
    setToken(response.accessToken)
    setUser(nextUser)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const refreshMe = useCallback(async () => {
    const me = await getMe()
    const nextUser: AuthUser = {
      userId: me.id,
      email: me.email,
      role: me.role,
      name: me.name,
    }
    setUser(nextUser)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }, [])

  useEffect(() => {
    if (!token) {
      setIsBootstrapping(false)
      return
    }

    let cancelled = false
    ;(async () => {
      try {
        await refreshMe()
      } catch {
        if (!cancelled) logout()
      } finally {
        if (!cancelled) setIsBootstrapping(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [token, refreshMe, logout])

  const login = useCallback(
    async (payload: LoginPayload) => {
      const response = await authApi.login(payload)
      persistSession(response)
      await refreshMe()
    },
    [persistSession, refreshMe],
  )

  const register = useCallback(
    async (payload: RegisterPayload) => {
      const response = await authApi.register(payload)
      persistSession(response)
      await refreshMe()
    },
    [persistSession, refreshMe],
  )

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token && user),
      isBootstrapping,
      login,
      register,
      logout,
      refreshMe,
    }),
    [user, token, isBootstrapping, login, register, logout, refreshMe],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider")
  }
  return ctx
}

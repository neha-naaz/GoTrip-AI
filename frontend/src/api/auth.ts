import { apiRequest } from "@/api/client"
import type { AuthResponse, LoginPayload, RegisterPayload } from "@/api/types"

export function login(payload: LoginPayload) {
  return apiRequest<AuthResponse>(
    "/api/auth/login",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    false,
  )
}

export function register(payload: RegisterPayload) {
  return apiRequest<AuthResponse>(
    "/api/auth/register",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    false,
  )
}

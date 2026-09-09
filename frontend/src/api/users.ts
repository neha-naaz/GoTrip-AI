import { apiRequest } from "@/api/client"
import type { UserResponse } from "@/api/types"

export function getMe() {
  return apiRequest<UserResponse>("/api/users/me")
}

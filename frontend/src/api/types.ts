export type UserRole = "CUSTOMER" | "AGENCY" | "ADMIN"

export type AuthUser = {
  userId: number
  email: string
  role: UserRole
  name?: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  userId: number
  email: string
  role: UserRole
}

export type UserResponse = {
  id: number
  name: string
  email: string
  role: UserRole
  status: string
}

export type RegisterPayload = {
  name: string
  email: string
  password: string
  role: "CUSTOMER" | "AGENCY"
  agencyName?: string
}

export type LoginPayload = {
  email: string
  password: string
}

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

export type Trip = {
  id: number
  agencyId: number
  title: string
  description: string | null
  source: string
  destination: string
  startDate: string
  endDate: string
  price: number
  bookingAmount: number
  capacity: number
  status: string
  createdAt: string
}

export type TripItinerary = {
  id: number
  dayNumber: number
  title: string
  description: string | null
}

export type TripItem = {
  id: number
  description: string
}

/** Matches backend TripDetailResponse */
export type TripDetail = Trip & {
  itineraries: TripItinerary[]
  inclusions: TripItem[]
  exclusions: TripItem[]
}

export type TripSearchParams = {
  source?: string
  destination?: string
}

/** Matches backend CreateTripRequest */
export type CreateTripPayload = {
  title: string
  description?: string
  source: string
  destination: string
  startDate: string
  endDate: string
  price: number
  bookingAmount: number
  capacity: number
}

/** Matches backend BookingResponse */
export type BookingStatus = "PENDING_PAYMENT" | "CONFIRMED" | "CANCELLED" | "EXPIRED"

export type Booking = {
  id: number
  tripId: number
  status: BookingStatus
  amountDue: number
}

export type CreateBookingRequest = {
  tripId: number
}

/** Matches backend PaymentResponse */
export type PaymentStatus = "CREATED" | "SUCCESS" | "FAILED"

export type Payment = {
  id: number
  bookingId: number
  amount: number
  status: PaymentStatus
  provider: string
  providerRef: string
}

export type ConfirmWebhookRequest = {
  providerRef: string
  status: "SUCCESS" | "FAILED"
}
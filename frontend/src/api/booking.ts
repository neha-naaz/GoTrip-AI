import { apiRequest } from "@/api/client"
import type { Booking, CreateBookingRequest } from "@/api/types"

export function createBooking(payload: CreateBookingRequest) {
  return apiRequest<Booking>("/api/bookings", {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export function listMyBookings() {
  return apiRequest<Booking[]>("/api/bookings/me")
}

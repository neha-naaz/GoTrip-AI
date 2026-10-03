import { apiRequest } from "@/api/client"
import type { Payment } from "@/api/types"

export function initiatePay(bookingId: number) {
  return apiRequest<Payment>(`/api/bookings/${bookingId}/pay`, {
    method: "POST",
  })
}

export function listPayments(bookingId: number) {
  return apiRequest<Payment[]>(`/api/bookings/${bookingId}/payments`)
}

/**
 * Sandbox demo: authenticated confirm (no webhook secret in the browser).
 * Real providers should call POST /api/payments/webhook from their servers.
 */
export function sandboxConfirm(bookingId: number) {
  return apiRequest<Payment>(`/api/bookings/${bookingId}/sandbox-confirm`, {
    method: "POST",
  })
}

/** Full sandbox pay flow for the UI */
export function payAndConfirm(bookingId: number): Promise<Payment> {
  return sandboxConfirm(bookingId)
}

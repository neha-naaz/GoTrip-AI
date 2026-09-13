import { apiRequest } from "@/api/client"
import type { ConfirmWebhookRequest, Payment } from "@/api/types"

export function initiatePay(bookingId: number) {
  return apiRequest<Payment>(`/api/bookings/${bookingId}/pay`, {
    method: "POST",
  })
}

export function listPayments(bookingId: number) {
  return apiRequest<Payment[]>(`/api/bookings/${bookingId}/payments`)
}

/**
 * Dev/sandbox helper: simulates the payment provider calling our webhook.
 * Real Razorpay would hit this endpoint from their servers.
 */
export function confirmWebhook(payload: ConfirmWebhookRequest) {
  const secret = import.meta.env.VITE_PAYMENT_WEBHOOK_SECRET ?? "local-dev-secret"

  return apiRequest<void>(
    "/api/payments/webhook",
    {
      method: "POST",
      headers: {
        "X-Tripflow-Webhook-Secret": secret,
      },
      body: JSON.stringify(payload),
    },
    false,
  )
}

/** Full sandbox pay: initiate → webhook SUCCESS */
export async function payAndConfirm(bookingId: number): Promise<Payment> {
  const payment = await initiatePay(bookingId)
  await confirmWebhook({
    providerRef: payment.providerRef,
    status: "SUCCESS",
  })
  return payment
}

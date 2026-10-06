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
 * Sandbox demo: authenticated confirm (MockPaymentProvider only).
 */
export function sandboxConfirm(bookingId: number) {
  return apiRequest<Payment>(`/api/bookings/${bookingId}/sandbox-confirm`, {
    method: "POST",
  })
}

export function confirmCheckout(
  bookingId: number,
  body: { orderId: string; paymentId: string; signature: string },
) {
  return apiRequest<Payment>(`/api/bookings/${bookingId}/confirm-checkout`, {
    method: "POST",
    body: JSON.stringify(body),
  })
}

type RazorpaySuccess = {
  razorpay_payment_id: string
  razorpay_order_id: string
  razorpay_signature: string
}

type RazorpayCheckout = {
  open: () => void
  on: (event: string, handler: (response: unknown) => void) => void
}

declare global {
  interface Window {
    Razorpay?: new (options: Record<string, unknown>) => RazorpayCheckout
  }
}

export class PaymentCancelledError extends Error {
  constructor(message = "Payment cancelled") {
    super(message)
    this.name = "PaymentCancelledError"
  }
}

function loadRazorpayScript(): Promise<boolean> {
  if (typeof window === "undefined") return Promise.resolve(false)
  if (window.Razorpay) return Promise.resolve(true)

  return new Promise((resolve) => {
    const existing = document.querySelector<HTMLScriptElement>(
      'script[src="https://checkout.razorpay.com/v1/checkout.js"]',
    )
    if (existing) {
      existing.addEventListener("load", () => resolve(!!window.Razorpay), { once: true })
      existing.addEventListener("error", () => resolve(false), { once: true })
      return
    }
    const script = document.createElement("script")
    script.src = "https://checkout.razorpay.com/v1/checkout.js"
    script.async = true
    script.onload = () => resolve(!!window.Razorpay)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

async function openRazorpayCheckout(bookingId: number, payment: Payment): Promise<Payment> {
  const keyId = payment.checkoutKeyId || import.meta.env.VITE_RAZORPAY_KEY_ID
  if (!keyId || payment.amountPaise == null || !payment.currency) {
    throw new Error("Checkout session incomplete")
  }
  if (payment.amountPaise < 100) {
    throw new Error("Payment amount must be at least ₹1")
  }

  const ready = await loadRazorpayScript()
  if (!ready || !window.Razorpay) {
    throw new Error("Could not load Razorpay Checkout")
  }

  return new Promise((resolve, reject) => {
    let settled = false
    const rzp = new window.Razorpay!({
      key: keyId,
      amount: payment.amountPaise,
      currency: payment.currency,
      order_id: payment.providerRef,
      name: "Tripflow",
      description: `Booking #${bookingId}`,
      handler: (response: RazorpaySuccess) => {
        settled = true
        void confirmCheckout(bookingId, {
          orderId: response.razorpay_order_id,
          paymentId: response.razorpay_payment_id,
          signature: response.razorpay_signature,
        })
          .then(resolve)
          .catch(reject)
      },
      modal: {
        ondismiss: () => {
          if (!settled) {
            reject(new PaymentCancelledError())
          }
        },
      },
    })
    rzp.on("payment.failed", (response: unknown) => {
      settled = true
      const failed = response as { error?: { description?: string } }
      reject(new Error(failed.error?.description || "Payment failed"))
    })
    rzp.open()
  })
}

/**
 * Full pay flow: Mock → sandbox confirm; Razorpay → hosted Checkout + signature confirm.
 */
export async function payAndConfirm(bookingId: number): Promise<Payment> {
  const payment = await initiatePay(bookingId)

  if (payment.status === "SUCCESS") {
    return payment
  }

  if (payment.provider === "RAZORPAY") {
    return openRazorpayCheckout(bookingId, payment)
  }

  if (payment.provider === "MOCK") {
    return sandboxConfirm(bookingId)
  }

  throw new Error(`Unsupported payment provider: ${payment.provider}`)
}

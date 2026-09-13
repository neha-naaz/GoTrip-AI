import { useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { CreditCard, MapPin } from "lucide-react"
import { ApiError } from "@/api/client"
import { listMyBookings } from "@/api/booking"
import { payAndConfirm } from "@/api/payments"
import { getTrip } from "@/api/trips"
import type { Booking, Trip } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { formatTripMoney } from "@/lib/trip-display"

function statusStyles(status: Booking["status"]) {
  switch (status) {
    case "CONFIRMED":
      return "bg-emerald-50 text-emerald-800"
    case "PENDING_PAYMENT":
      return "bg-amber-50 text-amber-800"
    case "EXPIRED":
    case "CANCELLED":
      return "bg-zinc-100 text-zinc-600"
    default:
      return "bg-muted text-muted-foreground"
  }
}

export function BookingsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [bookings, setBookings] = useState<Booking[]>([])
  const [tripsById, setTripsById] = useState<Record<number, Trip>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [payingId, setPayingId] = useState<number | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const data = await listMyBookings()
      setBookings(data)

      const uniqueTripIds = [...new Set(data.map((b) => b.tripId))]
      const trips = await Promise.all(
        uniqueTripIds.map(async (id) => {
          try {
            return await getTrip(id)
          } catch {
            return null
          }
        }),
      )

      const map: Record<number, Trip> = {}
      for (const trip of trips) {
        if (trip) map[trip.id] = trip
      }
      setTripsById(map)
    } catch (err) {
      setBookings([])
      setError(err instanceof ApiError ? err.message : "Could not load bookings")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (user?.role !== "CUSTOMER") {
      setLoading(false)
      return
    }
    void load()
  }, [user?.role])

  async function onPay(bookingId: number) {
    setActionError(null)
    setPayingId(bookingId)
    try {
      await payAndConfirm(bookingId)
      await load()
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Payment failed")
    } finally {
      setPayingId(null)
    }
  }

  if (user?.role !== "CUSTOMER") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">Bookings are for traveler accounts</p>
        <p className="mt-2 text-sm text-muted-foreground">
          Sign in as a customer to reserve seats and pay.
        </p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          Explore trips
        </Button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <div className="mb-8">
        <h1 className="text-3xl font-semibold tracking-tight">My bookings</h1>
        <p className="mt-2 text-muted-foreground">
          Pay pending bookings to confirm your seat. Sandbox payment simulates the provider webhook.
        </p>
      </div>

      {actionError ? (
        <p className="mb-4 rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {actionError}
        </p>
      ) : null}

      {loading ? (
        <p className="py-16 text-center text-sm text-muted-foreground">Loading bookings…</p>
      ) : null}

      {!loading && error ? (
        <p className="rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</p>
      ) : null}

      {!loading && !error && bookings.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center">
          <p className="font-medium">No bookings yet</p>
          <p className="mt-1 text-sm text-muted-foreground">Find a trip and reserve your seat.</p>
          <Button className="mt-6 rounded-2xl" onClick={() => navigate("/trips")}>
            Explore trips
          </Button>
        </div>
      ) : null}

      {!loading && !error && bookings.length > 0 ? (
        <ul className="space-y-4">
          {bookings.map((booking) => {
            const trip = tripsById[booking.tripId]
            return (
              <li
                key={booking.id}
                className="rounded-2xl border border-border bg-card p-5 shadow-sm"
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <Link
                      to={`/trips/${booking.tripId}`}
                      className="text-lg font-semibold tracking-tight hover:text-primary"
                    >
                      {trip?.title ?? `Trip #${booking.tripId}`}
                    </Link>
                    {trip ? (
                      <p className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
                        <MapPin className="size-3.5" />
                        {trip.source} → {trip.destination}
                      </p>
                    ) : null}
                    <p className="mt-3 text-sm">
                      Amount due{" "}
                      <span className="font-semibold">{formatTripMoney(booking.amountDue)}</span>
                    </p>
                  </div>
                  <span
                    className={`rounded-full px-3 py-1 text-xs font-medium ${statusStyles(booking.status)}`}
                  >
                    {booking.status.replaceAll("_", " ")}
                  </span>
                </div>

                {booking.status === "PENDING_PAYMENT" ? (
                  <Button
                    className="mt-5 h-11 rounded-2xl"
                    disabled={payingId === booking.id}
                    onClick={() => void onPay(booking.id)}
                  >
                    <CreditCard className="size-4" />
                    {payingId === booking.id ? "Processing…" : "Pay booking amount"}
                  </Button>
                ) : null}

                {booking.status === "CONFIRMED" ? (
                  <p className="mt-4 text-sm text-muted-foreground">
                    Seat confirmed. Group chat UI comes next.
                  </p>
                ) : null}
              </li>
            )
          })}
        </ul>
      ) : null}
    </div>
  )
}

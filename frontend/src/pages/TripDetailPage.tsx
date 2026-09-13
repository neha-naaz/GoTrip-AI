import { useEffect, useState } from "react"
import { Link, useNavigate, useParams } from "react-router-dom"
import { ArrowLeft, CalendarDays, Users } from "lucide-react"
import { createBooking } from "@/api/booking"
import { ApiError } from "@/api/client"
import { getTrip } from "@/api/trips"
import type { TripDetail } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { formatTripDateRange, formatTripMoney, tripImageForDestination } from "@/lib/trip-display"

export function TripDetailPage() {
  const { tripId } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const [trip, setTrip] = useState<TripDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [booking, setBooking] = useState(false)
  const [bookError, setBookError] = useState<string | null>(null)

  useEffect(() => {
    const id = Number(tripId)
    if (!Number.isFinite(id)) {
      setError("Invalid trip")
      setLoading(false)
      return
    }

    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await getTrip(id)
        if (!cancelled) setTrip(data)
      } catch (err) {
        if (!cancelled) {
          setTrip(null)
          setError(err instanceof ApiError ? err.message : "Trip not found")
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [tripId])

  async function onBook() {
    if (!trip) return
    setBookError(null)
    setBooking(true)
    try {
      await createBooking({ tripId: trip.id })
      navigate("/bookings")
    } catch (err) {
      setBookError(err instanceof ApiError ? err.message : "Could not create booking")
    } finally {
      setBooking(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center text-sm text-muted-foreground">
        Loading trip…
      </div>
    )
  }

  if (error || !trip) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">{error ?? "Trip not found"}</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          <ArrowLeft className="size-4" />
          Back to explore
        </Button>
      </div>
    )
  }

  const imageUrl = tripImageForDestination(trip.destination)

  return (
    <div>
      <section className="relative isolate min-h-[48vh] overflow-hidden">
        <img src={imageUrl} alt={trip.destination} className="absolute inset-0 size-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/35 to-black/15" />
        <div className="relative mx-auto flex min-h-[48vh] max-w-6xl flex-col justify-end px-4 pb-10 pt-20 sm:px-6">
          <Link to="/trips" className="mb-4 inline-flex w-fit items-center gap-2 text-sm text-white/80 hover:text-white">
            <ArrowLeft className="size-4" />
            Explore
          </Link>
          <h1 className="max-w-3xl text-3xl font-semibold tracking-tight text-white sm:text-5xl">
            {trip.title}
          </h1>
          <p className="mt-3 text-base text-white/85">
            {trip.source} → {trip.destination}
          </p>
        </div>
      </section>

      <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 lg:grid-cols-[1.4fr_0.8fr] sm:px-6">
        <div className="space-y-10">
          <section>
            <h2 className="text-xl font-semibold tracking-tight">About this trip</h2>
            <p className="mt-3 whitespace-pre-wrap text-muted-foreground leading-relaxed">
              {trip.description?.trim() || "No description provided yet."}
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold tracking-tight">Itinerary</h2>
            {trip.itineraries.length === 0 ? (
              <p className="mt-3 text-sm text-muted-foreground">Itinerary coming soon.</p>
            ) : (
              <ol className="mt-4 space-y-3">
                {trip.itineraries.map((day) => (
                  <li key={day.id} className="rounded-2xl border border-border bg-card p-4 shadow-sm">
                    <p className="text-xs font-medium tracking-wide text-primary uppercase">
                      Day {day.dayNumber}
                    </p>
                    <p className="mt-1 font-medium">{day.title}</p>
                    {day.description ? (
                      <p className="mt-1 text-sm text-muted-foreground">{day.description}</p>
                    ) : null}
                  </li>
                ))}
              </ol>
            )}
          </section>

          <section className="grid gap-6 sm:grid-cols-2">
            <div>
              <h2 className="text-xl font-semibold tracking-tight">Inclusions</h2>
              {trip.inclusions.length === 0 ? (
                <p className="mt-3 text-sm text-muted-foreground">None listed.</p>
              ) : (
                <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
                  {trip.inclusions.map((item) => (
                    <li key={item.id}>{item.description}</li>
                  ))}
                </ul>
              )}
            </div>
            <div>
              <h2 className="text-xl font-semibold tracking-tight">Exclusions</h2>
              {trip.exclusions.length === 0 ? (
                <p className="mt-3 text-sm text-muted-foreground">None listed.</p>
              ) : (
                <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
                  {trip.exclusions.map((item) => (
                    <li key={item.id}>{item.description}</li>
                  ))}
                </ul>
              )}
            </div>
          </section>
        </div>

        <aside className="h-fit rounded-2xl border border-border bg-card p-6 shadow-sm lg:sticky lg:top-24">
          <p className="text-3xl font-semibold tracking-tight">{formatTripMoney(trip.price)}</p>
          <p className="mt-1 text-sm text-muted-foreground">
            Booking amount {formatTripMoney(trip.bookingAmount)}
          </p>

          <div className="mt-6 space-y-3 text-sm">
            <p className="flex items-center gap-2 text-muted-foreground">
              <CalendarDays className="size-4 text-foreground" />
              {formatTripDateRange(trip.startDate, trip.endDate)}
            </p>
            <p className="flex items-center gap-2 text-muted-foreground">
              <Users className="size-4 text-foreground" />
              Capacity {trip.capacity}
            </p>
          </div>

          {bookError ? (
            <p className="mt-4 rounded-2xl bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {bookError}
            </p>
          ) : null}

          {!isAuthenticated ? (
            <Button className="mt-8 h-11 w-full rounded-2xl" render={<Link to="/login" />}>
              Sign in to book
            </Button>
          ) : user?.role === "CUSTOMER" ? (
            <Button
              className="mt-8 h-11 w-full rounded-2xl"
              disabled={booking}
              onClick={() => void onBook()}
            >
              {booking ? "Booking…" : "Book this trip"}
            </Button>
          ) : (
            <Button className="mt-8 h-11 w-full rounded-2xl" variant="outline" disabled>
              Agency accounts browse only
            </Button>
          )}
          <p className="mt-3 text-center text-xs text-muted-foreground">
            Creates a pending booking. Pay from My bookings to confirm.
          </p>
        </aside>
      </div>
    </div>
  )
}

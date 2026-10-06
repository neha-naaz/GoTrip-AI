import { useEffect, useState } from "react"
import { Link, useNavigate, useParams } from "react-router-dom"
import { ArrowLeft, CalendarDays, MapPin, Users } from "lucide-react"
import { createBooking } from "@/api/booking"
import { ApiError } from "@/api/client"
import { getTrip } from "@/api/trips"
import type { TripDetail } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { TripCoverCarousel } from "@/components/trips/TripCoverCarousel"
import { TripPhotoStrip } from "@/components/trips/TripPhotoStrip"
import { Button } from "@/components/ui/button"
import { formatTripDateRange, formatTripMoney, itineraryDayLabel, isOptionalItineraryDay, tripLengthDays } from "@/lib/trip-display"

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

  return (
    <div>
      <TripCoverCarousel
        destination={trip.destination}
        coverImageUrl={trip.coverImageUrl}
      >
        <Link to="/trips" className="mb-4 inline-flex w-fit items-center gap-2 text-sm text-white/80 hover:text-white">
          <ArrowLeft className="size-4" />
          Explore
        </Link>
        <h1 className="max-w-3xl text-3xl font-semibold tracking-tight text-white sm:text-5xl">
          {trip.title}
        </h1>
        <p className="mt-4 inline-flex max-w-full flex-wrap items-center gap-2 rounded-full bg-black/45 px-3.5 py-2 text-sm font-semibold tracking-wide text-white shadow-sm ring-1 ring-white/25 backdrop-blur-sm sm:text-base">
          <MapPin className="size-4 shrink-0 text-white" aria-hidden />
          <span>{trip.source}</span>
          <span className="text-white/70" aria-hidden>
            →
          </span>
          <span>{trip.destination}</span>
        </p>
      </TripCoverCarousel>

      <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 lg:grid-cols-[1.4fr_0.8fr] sm:px-6">
        <div className="space-y-10">
          <TripPhotoStrip
            destination={trip.destination}
            images={trip.images}
            coverImageUrl={trip.coverImageUrl}
          />

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
                {trip.itineraries.map((day) => {
                  const extra = isOptionalItineraryDay(
                    day.dayNumber,
                    tripLengthDays(trip.startDate, trip.endDate),
                  )
                  return (
                  <li
                    key={day.id}
                    className={
                      extra
                        ? "rounded-2xl border border-dashed border-amber-200 bg-amber-50/80 p-4 shadow-sm"
                        : "rounded-2xl border border-border bg-card p-4 shadow-sm"
                    }
                  >
                    <p className={`text-xs font-medium tracking-wide uppercase ${extra ? "text-amber-800" : "text-primary"}`}>
                      {itineraryDayLabel(day.dayNumber, tripLengthDays(trip.startDate, trip.endDate))}
                    </p>
                    <p className="mt-1 font-medium">{day.title}</p>
                    {day.description ? (
                      <p className="mt-1 text-sm text-muted-foreground">{day.description}</p>
                    ) : null}
                  </li>
                  )
                })}
              </ol>
            )}
          </section>

          <section>
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
          </section>

          <section>
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
          </section>
        </div>

        <aside className="h-fit rounded-2xl border border-border bg-card p-6 shadow-sm lg:sticky lg:top-24">
          <p className="text-3xl font-semibold tracking-tight">{formatTripMoney(trip.price)}</p>
          <p className="mt-1 text-sm text-muted-foreground">
            Booking amount {formatTripMoney(trip.bookingAmount)}
          </p>

          <div className="mt-6 space-y-3 text-sm">
            <p className="flex items-start gap-2 font-medium text-foreground">
              <MapPin className="mt-0.5 size-4 shrink-0" />
              <span>
                {trip.source}
                <span className="mx-1.5 text-muted-foreground" aria-hidden>
                  →
                </span>
                {trip.destination}
              </span>
            </p>
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

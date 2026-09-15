import { useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { ArrowLeft, CalendarDays, Pencil, Users } from "lucide-react"
import { ApiError } from "@/api/client"
import { listAgencyTrips } from "@/api/agencyTrips"
import { listExclusions, listInclusions, listItineraries } from "@/api/tripContent"
import type { Trip, TripItinerary, TripItem } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { formatTripDateRange, formatTripMoney, tripImageForDestination } from "@/lib/trip-display"

export function AgencyTripPreviewPage() {
  const { tripId } = useParams()
  const { user } = useAuth()
  const id = Number(tripId)

  const [trip, setTrip] = useState<Trip | null>(null)
  const [itineraries, setItineraries] = useState<TripItinerary[]>([])
  const [inclusions, setInclusions] = useState<TripItem[]>([])
  const [exclusions, setExclusions] = useState<TripItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (user?.role !== "AGENCY" || !Number.isFinite(id)) {
      setLoading(false)
      if (!Number.isFinite(id)) setError("Invalid trip")
      return
    }

    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const trips = await listAgencyTrips()
        const found = trips.find((t) => t.id === id) ?? null
        if (!found) {
          if (!cancelled) {
            setTrip(null)
            setError("Trip not found")
          }
          return
        }

        const [days, incl, excl] = await Promise.all([
          listItineraries(id),
          listInclusions(id),
          listExclusions(id),
        ])
        if (cancelled) return
        setTrip(found)
        setItineraries(days)
        setInclusions(incl)
        setExclusions(excl)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : "Could not load preview")
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [user?.role, id])

  if (user?.role !== "AGENCY") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">Preview is for agency accounts</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          Explore trips
        </Button>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center text-sm text-muted-foreground">
        Loading preview…
      </div>
    )
  }

  if (error || !trip) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">{error ?? "Trip not found"}</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/agency/trips" />}>
          <ArrowLeft className="size-4" />
          My trips
        </Button>
      </div>
    )
  }

  const imageUrl = tripImageForDestination(trip.destination)

  return (
    <div>
      <div className="border-b border-border bg-amber-50 px-4 py-3 text-center text-sm text-amber-900 sm:px-6">
        Draft preview — only you can see this. Customers see it after publish.
        {trip.status === "DRAFT" ? (
          <>
            {" "}
            <Link to={`/agency/trips/${trip.id}/edit`} className="font-medium underline underline-offset-2">
              Edit content
            </Link>
          </>
        ) : null}
      </div>

      <section className="relative isolate min-h-[48vh] overflow-hidden">
        <img src={imageUrl} alt={trip.destination} className="absolute inset-0 size-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/35 to-black/15" />
        <div className="relative mx-auto flex min-h-[48vh] max-w-6xl flex-col justify-end px-4 pb-10 pt-20 sm:px-6">
          <Link
            to="/agency/trips"
            className="mb-4 inline-flex w-fit items-center gap-2 text-sm text-white/80 hover:text-white"
          >
            <ArrowLeft className="size-4" />
            My trips
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
            {itineraries.length === 0 ? (
              <p className="mt-3 text-sm text-muted-foreground">Itinerary coming soon.</p>
            ) : (
              <ol className="mt-4 space-y-3">
                {itineraries.map((day) => (
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

          <section>
            <h2 className="text-xl font-semibold tracking-tight">Inclusions</h2>
            {inclusions.length === 0 ? (
              <p className="mt-3 text-sm text-muted-foreground">None listed.</p>
            ) : (
              <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
                {inclusions.map((item) => (
                  <li key={item.id}>{item.description}</li>
                ))}
              </ul>
            )}
          </section>

          <section>
            <h2 className="text-xl font-semibold tracking-tight">Exclusions</h2>
            {exclusions.length === 0 ? (
              <p className="mt-3 text-sm text-muted-foreground">None listed.</p>
            ) : (
              <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
                {exclusions.map((item) => (
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
            <p className="flex items-center gap-2 text-muted-foreground">
              <CalendarDays className="size-4 text-foreground" />
              {formatTripDateRange(trip.startDate, trip.endDate)}
            </p>
            <p className="flex items-center gap-2 text-muted-foreground">
              <Users className="size-4 text-foreground" />
              Capacity {trip.capacity}
            </p>
          </div>

          {trip.status === "DRAFT" ? (
            <Button
              className="mt-8 h-11 w-full rounded-2xl"
              render={<Link to={`/agency/trips/${trip.id}/edit`} />}
            >
              <Pencil className="size-4" />
              Edit content
            </Button>
          ) : (
            <Button className="mt-8 h-11 w-full rounded-2xl" render={<Link to={`/trips/${trip.id}`} />}>
              View public page
            </Button>
          )}
          <p className="mt-3 text-center text-xs text-muted-foreground">
            This is how travelers will see your trip after publish.
          </p>
        </aside>
      </div>
    </div>
  )
}

import { useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { ArrowLeft, Users } from "lucide-react"
import { ApiError } from "@/api/client"
import { listAgencyTravelers, listAgencyTrips } from "@/api/agencyTrips"
import type { AgencyTraveler, Trip } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"

export function AgencyTripTravelersPage() {
  const { tripId } = useParams()
  const { user } = useAuth()
  const id = Number(tripId)

  const [trip, setTrip] = useState<Trip | null>(null)
  const [travelers, setTravelers] = useState<AgencyTraveler[]>([])
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
        const [trips, roster] = await Promise.all([listAgencyTrips(), listAgencyTravelers(id)])
        if (cancelled) return
        const found = trips.find((t) => t.id === id) ?? null
        if (!found) {
          setTrip(null)
          setError("Trip not found")
          return
        }
        setTrip(found)
        setTravelers(roster)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : "Could not load travelers")
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
        <p className="text-lg font-medium">Travelers list is for agency accounts</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          Explore trips
        </Button>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-sm text-muted-foreground">
        Loading travelers…
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

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <Link
        to="/agency/trips"
        className="mb-4 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="size-3.5" />
        My trips
      </Link>

      <div className="mb-8">
        <h1 className="text-3xl font-semibold tracking-tight">Travelers</h1>
        <p className="mt-2 text-muted-foreground">
          {trip.title} · {trip.source} → {trip.destination}
        </p>
        <p className="mt-3 inline-flex items-center gap-1.5 text-sm text-muted-foreground">
          <Users className="size-3.5" />
          {travelers.length} confirmed
        </p>
      </div>

      {travelers.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center">
          <p className="font-medium">No confirmed travelers yet</p>
          <p className="mt-1 text-sm text-muted-foreground">
            Customers appear here after they pay and confirm their booking.
          </p>
        </div>
      ) : (
        <ul className="space-y-3">
          {travelers.map((traveler) => (
            <li
              key={traveler.bookingId}
              className="flex flex-wrap items-start justify-between gap-3 rounded-2xl border border-border bg-card p-4"
            >
              <div className="min-w-0">
                <p className="font-medium">{traveler.name}</p>
                <p className="mt-1 truncate text-sm text-muted-foreground">{traveler.email}</p>
              </div>
              <p className="text-xs text-muted-foreground">
                Booked{" "}
                {new Date(traveler.bookedAt).toLocaleDateString(undefined, {
                  month: "short",
                  day: "numeric",
                  year: "numeric",
                })}
              </p>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

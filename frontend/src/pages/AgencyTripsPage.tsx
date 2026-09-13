import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { Plus, Rocket, Trash2 } from "lucide-react"
import { ApiError } from "@/api/client"
import { deleteAgencyTrip, listAgencyTrips, publishAgencyTrip } from "@/api/agencyTrips"
import type { Trip } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { formatTripDateRange, formatTripMoney } from "@/lib/trip-display"

function statusStyles(status: string) {
  if (status === "PUBLISHED") return "bg-emerald-50 text-emerald-800"
  if (status === "DRAFT") return "bg-amber-50 text-amber-800"
  return "bg-zinc-100 text-zinc-600"
}

export function AgencyTripsPage() {
  const { user } = useAuth()
  const [trips, setTrips] = useState<Trip[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setTrips(await listAgencyTrips())
    } catch (err) {
      setTrips([])
      setError(err instanceof ApiError ? err.message : "Could not load trips")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (user?.role !== "AGENCY") {
      setLoading(false)
      return
    }
    void load()
  }, [user?.role])

  async function onPublish(tripId: number) {
    setActionError(null)
    setBusyId(tripId)
    try {
      await publishAgencyTrip(tripId)
      await load()
    } catch (err) {
      setActionError(
        err instanceof ApiError
          ? err.message
          : "Publish failed. Agency may need verification.",
      )
    } finally {
      setBusyId(null)
    }
  }

  async function onDelete(tripId: number) {
    setActionError(null)
    setBusyId(tripId)
    try {
      await deleteAgencyTrip(tripId)
      await load()
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Delete failed")
    } finally {
      setBusyId(null)
    }
  }

  if (user?.role !== "AGENCY") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">Agency trips are for agency accounts</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          Explore trips
        </Button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">My trips</h1>
          <p className="mt-2 text-muted-foreground">
            Create drafts, then publish when your agency is verified.
          </p>
        </div>
        <Button className="rounded-2xl" render={<Link to="/agency/trips/new" />}>
          <Plus className="size-4" />
          New trip
        </Button>
      </div>

      {actionError ? (
        <p className="mb-4 rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {actionError}
        </p>
      ) : null}

      {loading ? (
        <p className="py-16 text-center text-sm text-muted-foreground">Loading trips…</p>
      ) : null}

      {!loading && error ? (
        <p className="rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</p>
      ) : null}

      {!loading && !error && trips.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center">
          <p className="font-medium">No trips yet</p>
          <p className="mt-1 text-sm text-muted-foreground">Create your first draft itinerary.</p>
          <Button className="mt-6 rounded-2xl" render={<Link to="/agency/trips/new" />}>
            Create trip
          </Button>
        </div>
      ) : null}

      {!loading && !error && trips.length > 0 ? (
        <ul className="space-y-4">
          {trips.map((trip) => (
            <li key={trip.id} className="rounded-2xl border border-border bg-card p-5 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="text-lg font-semibold tracking-tight">{trip.title}</p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {trip.source} → {trip.destination}
                  </p>
                  <p className="mt-2 text-sm text-muted-foreground">
                    {formatTripDateRange(trip.startDate, trip.endDate)} ·{" "}
                    {formatTripMoney(trip.price)} · capacity {trip.capacity}
                  </p>
                </div>
                <span className={`rounded-full px-3 py-1 text-xs font-medium ${statusStyles(trip.status)}`}>
                  {trip.status}
                </span>
              </div>

              <div className="mt-5 flex flex-wrap gap-2">
                {trip.status === "PUBLISHED" ? (
                  <Button
                    variant="outline"
                    size="sm"
                    className="rounded-2xl"
                    render={<Link to={`/trips/${trip.id}`} />}
                  >
                    View public page
                  </Button>
                ) : null}

                {trip.status === "DRAFT" ? (
                  <Button
                    size="sm"
                    className="rounded-2xl"
                    disabled={busyId === trip.id}
                    onClick={() => void onPublish(trip.id)}
                  >
                    <Rocket className="size-3.5" />
                    {busyId === trip.id ? "Publishing…" : "Publish"}
                  </Button>
                ) : null}

                {(trip.status === "DRAFT" || trip.status === "PUBLISHED") && (
                  <Button
                    variant="outline"
                    size="sm"
                    className="rounded-2xl"
                    disabled={busyId === trip.id}
                    onClick={() => void onDelete(trip.id)}
                  >
                    <Trash2 className="size-3.5" />
                    Delete
                  </Button>
                )}
              </div>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  )
}

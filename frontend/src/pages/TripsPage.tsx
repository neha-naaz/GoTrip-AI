import { useEffect, useState, type FormEvent } from "react"
import { Search } from "lucide-react"
import { ApiError } from "@/api/client"
import { listTrips } from "@/api/trips"
import type { Trip } from "@/api/types"
import { TripCard } from "@/components/trips/TripCard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export function TripsPage() {
  const [source, setSource] = useState("")
  const [destination, setDestination] = useState("")
  const [trips, setTrips] = useState<Trip[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function loadTrips(nextSource = source, nextDestination = destination) {
    setLoading(true)
    setError(null)
    try {
      const data = await listTrips({
        source: nextSource,
        destination: nextDestination,
      })
      setTrips(data)
    } catch (err) {
      setTrips([])
      setError(err instanceof ApiError ? err.message : "Could not load trips")
    } finally {
      setLoading(false)
    }
  }

  // Run once when the page opens
  useEffect(() => {
    void loadTrips("", "")
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function onSearch(event: FormEvent) {
    event.preventDefault()
    void loadTrips(source, destination)
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
      <div className="mb-8 max-w-2xl">
        <h1 className="text-3xl font-semibold tracking-tight sm:text-4xl">Explore trips</h1>
        <p className="mt-2 text-muted-foreground">
          Find published group adventures by source and destination.
        </p>
      </div>

      <form
        onSubmit={onSearch}
        className="mb-10 grid gap-3 rounded-2xl border border-border bg-card p-4 shadow-sm sm:grid-cols-[1fr_1fr_auto] sm:items-end"
      >
        <div className="space-y-2">
          <Label htmlFor="source">From</Label>
          <Input
            id="source"
            value={source}
            onChange={(e) => setSource(e.target.value)}
            placeholder="Delhi"
            className="h-11 rounded-2xl"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="destination">To</Label>
          <Input
            id="destination"
            value={destination}
            onChange={(e) => setDestination(e.target.value)}
            placeholder="Manali"
            className="h-11 rounded-2xl"
          />
        </div>
        <Button type="submit" className="h-11 rounded-2xl sm:min-w-28" disabled={loading}>
          <Search className="size-4" />
          Search
        </Button>
      </form>

      {loading ? (
        <p className="py-16 text-center text-sm text-muted-foreground">Loading trips…</p>
      ) : null}

      {!loading && error ? (
        <p className="rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</p>
      ) : null}

      {!loading && !error && trips.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center">
          <p className="font-medium">No trips found</p>
          <p className="mt-1 text-sm text-muted-foreground">
            Try clearing filters, or publish a trip from an agency account.
          </p>
        </div>
      ) : null}

      {!loading && !error && trips.length > 0 ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {trips.map((trip) => (
            <TripCard key={trip.id} trip={trip} />
          ))}
        </div>
      ) : null}
    </div>
  )
}

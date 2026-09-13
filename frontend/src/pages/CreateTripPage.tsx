import { useState, type FormEvent } from "react"
import { Link, useNavigate } from "react-router-dom"
import { ApiError } from "@/api/client"
import { createAgencyTrip } from "@/api/agencyTrips"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

function defaultStartDate() {
  const d = new Date()
  d.setDate(d.getDate() + 14)
  return d.toISOString().slice(0, 10)
}

function defaultEndDate() {
  const d = new Date()
  d.setDate(d.getDate() + 18)
  return d.toISOString().slice(0, 10)
}

export function CreateTripPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [title, setTitle] = useState("")
  const [description, setDescription] = useState("")
  const [source, setSource] = useState("")
  const [destination, setDestination] = useState("")
  const [startDate, setStartDate] = useState(defaultStartDate)
  const [endDate, setEndDate] = useState(defaultEndDate)
  const [price, setPrice] = useState("15000")
  const [bookingAmount, setBookingAmount] = useState("3000")
  const [capacity, setCapacity] = useState("12")
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (user?.role !== "AGENCY") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">Only agencies can create trips</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          Explore trips
        </Button>
      </div>
    )
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const trip = await createAgencyTrip({
        title: title.trim(),
        description: description.trim() || undefined,
        source: source.trim(),
        destination: destination.trim(),
        startDate,
        endDate,
        price: Number(price),
        bookingAmount: Number(bookingAmount),
        capacity: Number(capacity),
      })
      navigate("/agency/trips", { state: { createdTripId: trip.id } })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create trip")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6">
      <div className="mb-8">
        <h1 className="text-3xl font-semibold tracking-tight">Create a trip</h1>
        <p className="mt-2 text-muted-foreground">
          Saves as a draft. Publish later once your agency is verified.
        </p>
      </div>

      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle>Trip details</CardTitle>
          <CardDescription>Fill the basics travelers will see on Explore.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={onSubmit}>
            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input
                id="title"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="h-11 rounded-2xl"
                placeholder="Manali Escape"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={4}
                className="w-full rounded-2xl border border-input bg-transparent px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                placeholder="What makes this trip special?"
              />
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="source">From</Label>
                <Input
                  id="source"
                  required
                  value={source}
                  onChange={(e) => setSource(e.target.value)}
                  className="h-11 rounded-2xl"
                  placeholder="Delhi"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="destination">To</Label>
                <Input
                  id="destination"
                  required
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                  className="h-11 rounded-2xl"
                  placeholder="Manali"
                />
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="startDate">Start date</Label>
                <Input
                  id="startDate"
                  type="date"
                  required
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="h-11 rounded-2xl"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="endDate">End date</Label>
                <Input
                  id="endDate"
                  type="date"
                  required
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="h-11 rounded-2xl"
                />
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-3">
              <div className="space-y-2">
                <Label htmlFor="price">Price (₹)</Label>
                <Input
                  id="price"
                  type="number"
                  min={1}
                  required
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  className="h-11 rounded-2xl"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="bookingAmount">Booking amount (₹)</Label>
                <Input
                  id="bookingAmount"
                  type="number"
                  min={1}
                  required
                  value={bookingAmount}
                  onChange={(e) => setBookingAmount(e.target.value)}
                  className="h-11 rounded-2xl"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="capacity">Capacity</Label>
                <Input
                  id="capacity"
                  type="number"
                  min={1}
                  required
                  value={capacity}
                  onChange={(e) => setCapacity(e.target.value)}
                  className="h-11 rounded-2xl"
                />
              </div>
            </div>

            {error ? (
              <p className="rounded-2xl bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>
            ) : null}

            <div className="flex flex-wrap gap-3 pt-2">
              <Button type="submit" className="h-11 rounded-2xl" disabled={loading}>
                {loading ? "Saving…" : "Save draft"}
              </Button>
              <Button
                type="button"
                variant="outline"
                className="h-11 rounded-2xl"
                render={<Link to="/agency/trips" />}
              >
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

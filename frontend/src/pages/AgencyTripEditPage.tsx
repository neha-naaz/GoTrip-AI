import { useEffect, useState, type FormEvent } from "react"
import { Link, Navigate, useParams } from "react-router-dom"
import { ArrowLeft, Eye, Pencil, Plus, Rocket, Trash2 } from "lucide-react"
import { ApiError } from "@/api/client"
import { listAgencyTrips, publishAgencyTrip, updateAgencyTrip } from "@/api/agencyTrips"
import { TripImageGalleryEditor } from "@/components/trips/TripImageGalleryEditor"
import {
  createExclusion,
  createInclusion,
  createItinerary,
  deleteExclusion,
  deleteInclusion,
  deleteItinerary,
  listExclusions,
  listInclusions,
  listItineraries,
  updateExclusion,
  updateInclusion,
  updateItinerary,
} from "@/api/tripContent"
import type { Trip, TripItinerary, TripItem } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { ConfirmDialog } from "@/components/ui/confirm-dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { formatTripDateRange, itineraryDayLabel, isOptionalItineraryDay, tripLengthDays } from "@/lib/trip-display"

function statusStyles(status: string) {
  if (status === "PUBLISHED") return "bg-emerald-50 text-emerald-800"
  if (status === "DRAFT") return "bg-amber-50 text-amber-800"
  return "bg-zinc-100 text-zinc-600"
}

function localTodayIso() {
  const d = new Date()
  const month = String(d.getMonth() + 1).padStart(2, "0")
  const day = String(d.getDate()).padStart(2, "0")
  return `${d.getFullYear()}-${month}-${day}`
}

function nextMissingTripDay(
  days: { dayNumber: number }[],
  startDate: string,
  endDate: string,
): number {
  const length = tripLengthDays(startDate, endDate)
  const existing = new Set(days.map((d) => d.dayNumber))
  for (let day = 1; day <= length; day++) {
    if (!existing.has(day)) return day
  }
  const extras = days.map((d) => d.dayNumber).filter((n) => n > length)
  const nextExtra = extras.length === 0 ? length + 1 : Math.max(...extras) + 1
  return existing.has(0) ? nextExtra : 0
}

export function AgencyTripEditPage() {
  const { tripId } = useParams()
  const { user } = useAuth()
  const id = Number(tripId)

  const [trip, setTrip] = useState<Trip | null>(null)
  const [itineraries, setItineraries] = useState<TripItinerary[]>([])
  const [inclusions, setInclusions] = useState<TripItem[]>([])
  const [exclusions, setExclusions] = useState<TripItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [publishOpen, setPublishOpen] = useState(false)

  const [dayNumber, setDayNumber] = useState("1")
  const [dayTitle, setDayTitle] = useState("")
  const [dayDescription, setDayDescription] = useState("")
  const [inclusionText, setInclusionText] = useState("")
  const [exclusionText, setExclusionText] = useState("")

  const [editingItineraryId, setEditingItineraryId] = useState<number | null>(null)
  const [editDayNumber, setEditDayNumber] = useState("")
  const [editDayTitle, setEditDayTitle] = useState("")
  const [editDayDescription, setEditDayDescription] = useState("")

  const [editingInclusionId, setEditingInclusionId] = useState<number | null>(null)
  const [editInclusionText, setEditInclusionText] = useState("")
  const [editingExclusionId, setEditingExclusionId] = useState<number | null>(null)
  const [editExclusionText, setEditExclusionText] = useState("")

  const [metaTitle, setMetaTitle] = useState("")
  const [metaDescription, setMetaDescription] = useState("")
  const [metaSource, setMetaSource] = useState("")
  const [metaDestination, setMetaDestination] = useState("")
  const [metaStartDate, setMetaStartDate] = useState("")
  const [metaEndDate, setMetaEndDate] = useState("")
  const [metaPrice, setMetaPrice] = useState("")
  const [metaBookingAmount, setMetaBookingAmount] = useState("")
  const [metaCapacity, setMetaCapacity] = useState("")

  const canEdit = trip?.status === "DRAFT"

  async function load() {
    if (!Number.isFinite(id)) {
      setError("Invalid trip")
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)
    try {
      const trips = await listAgencyTrips()
      const found = trips.find((t) => t.id === id) ?? null
      if (!found) {
        setTrip(null)
        setError("Trip not found")
        return
      }

      const [days, incl, excl] = await Promise.all([
        listItineraries(id),
        listInclusions(id),
        listExclusions(id),
      ])
      setTrip(found)
      setMetaTitle(found.title)
      setMetaDescription(found.description ?? "")
      setMetaSource(found.source)
      setMetaDestination(found.destination)
      setMetaStartDate(found.startDate)
      setMetaEndDate(found.endDate)
      setMetaPrice(String(found.price))
      setMetaBookingAmount(String(found.bookingAmount))
      setMetaCapacity(String(found.capacity))
      setItineraries(days)
      setInclusions(incl)
      setExclusions(excl)
      const nextDay =
        days.length === 0 ? 1 : Math.max(...days.map((d) => d.dayNumber)) + 1
      setDayNumber(String(nextMissingTripDay(days, found.startDate, found.endDate)))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load trip content")
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
  }, [user?.role, id])

  async function runAction(action: () => Promise<void>) {
    setActionError(null)
    setBusy(true)
    try {
      await action()
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Action failed")
    } finally {
      setBusy(false)
    }
  }

  async function onSaveTripDetails(event: FormEvent) {
    event.preventDefault()
    if (!canEdit) return
    await runAction(async () => {
      const updated = await updateAgencyTrip(id, {
        title: metaTitle.trim(),
        description: metaDescription.trim() || undefined,
        source: metaSource.trim(),
        destination: metaDestination.trim(),
        startDate: metaStartDate,
        endDate: metaEndDate,
        price: Number(metaPrice),
        bookingAmount: Number(metaBookingAmount),
        capacity: Number(metaCapacity),
      })
      setTrip(updated)
    })
  }

  async function onPublish() {
    if (!canEdit) return
    await runAction(async () => {
      const published = await publishAgencyTrip(id)
      setPublishOpen(false)
      setTrip(published)
    })
  }

  async function onAddItinerary(event: FormEvent) {
    event.preventDefault()
    if (!canEdit) return
    await runAction(async () => {
      const created = await createItinerary(id, {
        dayNumber: Number(dayNumber),
        title: dayTitle.trim(),
        description: dayDescription.trim() || undefined,
      })
      setItineraries((prev) =>
        [...prev, created].sort((a, b) => a.dayNumber - b.dayNumber),
      )
      setDayNumber(String(nextMissingTripDay([...itineraries, created], trip.startDate, trip.endDate)))
      setDayTitle("")
      setDayDescription("")
    })
  }

  async function onSaveItinerary(itineraryId: number) {
    if (!canEdit) return
    await runAction(async () => {
      const updated = await updateItinerary(id, itineraryId, {
        dayNumber: Number(editDayNumber),
        title: editDayTitle.trim(),
        description: editDayDescription.trim() || undefined,
      })
      setItineraries((prev) =>
        prev
          .map((item) => (item.id === itineraryId ? updated : item))
          .sort((a, b) => a.dayNumber - b.dayNumber),
      )
      setEditingItineraryId(null)
    })
  }

  async function onDeleteItinerary(itineraryId: number) {
    if (!canEdit) return
    await runAction(async () => {
      await deleteItinerary(id, itineraryId)
      setItineraries((prev) => prev.filter((item) => item.id !== itineraryId))
    })
  }

  async function onAddInclusion(event: FormEvent) {
    event.preventDefault()
    if (!canEdit) return
    await runAction(async () => {
      const created = await createInclusion(id, { description: inclusionText.trim() })
      setInclusions((prev) => [...prev, created])
      setInclusionText("")
    })
  }

  async function onSaveInclusion(inclusionId: number) {
    if (!canEdit) return
    await runAction(async () => {
      const updated = await updateInclusion(id, inclusionId, {
        description: editInclusionText.trim(),
      })
      setInclusions((prev) => prev.map((item) => (item.id === inclusionId ? updated : item)))
      setEditingInclusionId(null)
    })
  }

  async function onDeleteInclusion(inclusionId: number) {
    if (!canEdit) return
    await runAction(async () => {
      await deleteInclusion(id, inclusionId)
      setInclusions((prev) => prev.filter((item) => item.id !== inclusionId))
    })
  }

  async function onAddExclusion(event: FormEvent) {
    event.preventDefault()
    if (!canEdit) return
    await runAction(async () => {
      const created = await createExclusion(id, { description: exclusionText.trim() })
      setExclusions((prev) => [...prev, created])
      setExclusionText("")
    })
  }

  async function onSaveExclusion(exclusionId: number) {
    if (!canEdit) return
    await runAction(async () => {
      const updated = await updateExclusion(id, exclusionId, {
        description: editExclusionText.trim(),
      })
      setExclusions((prev) => prev.map((item) => (item.id === exclusionId ? updated : item)))
      setEditingExclusionId(null)
    })
  }

  async function onDeleteExclusion(exclusionId: number) {
    if (!canEdit) return
    await runAction(async () => {
      await deleteExclusion(id, exclusionId)
      setExclusions((prev) => prev.filter((item) => item.id !== exclusionId))
    })
  }

  if (user?.role !== "AGENCY") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">Only agencies can edit trip content</p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/trips" />}>
          Explore trips
        </Button>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-sm text-muted-foreground">
        Loading trip content…
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

  if (trip.status !== "DRAFT") {
    return <Navigate to={`/agency/trips/${trip.id}/preview`} replace />
  }

  const lengthDays = tripLengthDays(trip.startDate, trip.endDate)
  const existingDays = new Set(itineraries.map((day) => day.dayNumber))
  const missingDays = Array.from({ length: lengthDays }, (_, i) => i + 1).filter(
    (day) => !existingDays.has(day),
  )
  const hasDayZero = existingDays.has(0)
  const itineraryReady = missingDays.length === 0

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <Link
        to="/agency/trips"
        className="mb-4 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="size-3.5" />
        My trips
      </Link>

      <div className="mb-8 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">{trip.title}</h1>
          <p className="mt-2 text-muted-foreground">
            {trip.source} → {trip.destination}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-3 py-1 text-xs font-medium ${statusStyles(trip.status)}`}>
            {trip.status}
          </span>
          <Button
            variant="outline"
            size="sm"
            className="rounded-2xl"
            render={<Link to={`/agency/trips/${trip.id}/preview`} />}
          >
            <Eye className="size-3.5" />
            Preview
          </Button>
          <Button
            size="sm"
            className="rounded-2xl"
            disabled={busy || !itineraryReady}
            onClick={() => setPublishOpen(true)}
          >
            <Rocket className="size-3.5" />
            Publish
          </Button>
        </div>
      </div>

      <p className="mb-6 text-sm text-muted-foreground">
        Edit trip basics, itinerary, and inclusions before you publish. Published trips stay locked.
      </p>

      {actionError ? (
        <p className="mb-6 rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {actionError}
        </p>
      ) : null}

      <div className="mb-10">
        <TripImageGalleryEditor tripId={trip.id} disabled={!canEdit || busy} />
      </div>

      <section className="mb-10 space-y-4">
        <h2 className="text-xl font-semibold tracking-tight">Trip details</h2>
        <form
          onSubmit={onSaveTripDetails}
          className="space-y-3 rounded-2xl border border-border bg-card p-4"
        >
          <div className="space-y-1.5">
            <Label htmlFor="metaTitle">Title</Label>
            <Input
              id="metaTitle"
              value={metaTitle}
              onChange={(e) => setMetaTitle(e.target.value)}
              className="h-10 rounded-2xl"
              required
              disabled={busy}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="metaDescription">Description</Label>
            <textarea
              id="metaDescription"
              value={metaDescription}
              onChange={(e) => setMetaDescription(e.target.value)}
              rows={3}
              disabled={busy}
              className="w-full rounded-2xl border border-input bg-transparent px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
            />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="metaSource">From</Label>
              <Input
                id="metaSource"
                value={metaSource}
                onChange={(e) => setMetaSource(e.target.value)}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="metaDestination">To</Label>
              <Input
                id="metaDestination"
                value={metaDestination}
                onChange={(e) => setMetaDestination(e.target.value)}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="metaStart">Start</Label>
              <Input
                id="metaStart"
                type="date"
                min={localTodayIso()}
                value={metaStartDate}
                onChange={(e) => {
                  const next = e.target.value
                  setMetaStartDate(next)
                  if (metaEndDate && next && metaEndDate < next) {
                    setMetaEndDate(next)
                  }
                }}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="metaEnd">End</Label>
              <Input
                id="metaEnd"
                type="date"
                min={
                  metaStartDate && metaStartDate > localTodayIso()
                    ? metaStartDate
                    : localTodayIso()
                }
                value={metaEndDate}
                onChange={(e) => setMetaEndDate(e.target.value)}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-3">
            <div className="space-y-1.5">
              <Label htmlFor="metaPrice">Price</Label>
              <Input
                id="metaPrice"
                type="number"
                min={1}
                step="0.01"
                value={metaPrice}
                onChange={(e) => setMetaPrice(e.target.value)}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="metaBooking">Booking amount</Label>
              <Input
                id="metaBooking"
                type="number"
                min={1}
                step="0.01"
                value={metaBookingAmount}
                onChange={(e) => setMetaBookingAmount(e.target.value)}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="metaCapacity">Capacity</Label>
              <Input
                id="metaCapacity"
                type="number"
                min={1}
                value={metaCapacity}
                onChange={(e) => setMetaCapacity(e.target.value)}
                className="h-10 rounded-2xl"
                required
                disabled={busy}
              />
            </div>
          </div>
          <Button type="submit" className="rounded-2xl" disabled={busy}>
            Save trip details
          </Button>
        </form>
      </section>

      <section className="mb-10 space-y-4">
        <h2 className="text-xl font-semibold tracking-tight">Itinerary</h2>
        <p className="text-sm text-muted-foreground">
          This trip is {lengthDays} days ({formatTripDateRange(trip.startDate, trip.endDate)}).
          Add days 1–{lengthDays} to publish. Day 0 (travel/prep) and days after {lengthDays} are optional extras.
        </p>
        {missingDays.length > 0 ? (
          <p className="text-sm text-amber-800">
            Still needed: {missingDays.map((day) => `Day ${day}`).join(", ")}
          </p>
        ) : (
          <p className="text-sm text-emerald-800">
            All {lengthDays} trip days are filled. You can still add Day 0 or extra days.
          </p>
        )}

        {canEdit ? (
          <form
            onSubmit={onAddItinerary}
            className="space-y-3 rounded-2xl border border-border bg-card p-4"
          >
            <div className="grid gap-3 sm:grid-cols-[100px_1fr]">
              <div className="space-y-1.5">
                <Label htmlFor="dayNumber">Day</Label>
                <Input
                  id="dayNumber"
                  type="number"
                  min={0}
                  value={dayNumber}
                  onChange={(e) => setDayNumber(e.target.value)}
                  className="h-10 rounded-2xl"
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dayTitle">Title</Label>
                <Input
                  id="dayTitle"
                  value={dayTitle}
                  onChange={(e) => setDayTitle(e.target.value)}
                  className="h-10 rounded-2xl"
                  placeholder={Number(dayNumber) === 0 ? "Reach destination / packing" : "Arrival & check-in"}
                  required
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="dayDescription">Description</Label>
              <Input
                id="dayDescription"
                value={dayDescription}
                onChange={(e) => setDayDescription(e.target.value)}
                className="h-10 rounded-2xl"
                placeholder={
                  Number(dayNumber) === 0
                    ? "How to reach, meeting point, what to pack…"
                    : "Optional details"
                }
              />
            </div>
            <div className="flex flex-wrap gap-2">
              <Button type="submit" className="rounded-2xl" disabled={busy || !dayTitle.trim()}>
                <Plus className="size-4" />
                Add day
              </Button>
              {!hasDayZero ? (
                <Button
                  type="button"
                  variant="outline"
                  className="rounded-2xl"
                  disabled={busy}
                  onClick={() => {
                    setDayNumber("0")
                    if (!dayTitle.trim()) setDayTitle("Travel to destination")
                  }}
                >
                  Add Day 0
                </Button>
              ) : null}
            </div>
          </form>
        ) : null}

        {itineraries.length === 0 ? (
          <p className="text-sm text-muted-foreground">No itinerary days yet.</p>
        ) : (
          <ul className="space-y-3">
            {itineraries.map((item) => {
                const extra = isOptionalItineraryDay(item.dayNumber, lengthDays)
                return (
              <li
                key={item.id}
                className={
                  extra
                    ? "rounded-2xl border border-dashed border-amber-200 bg-amber-50/80 p-4"
                    : "rounded-2xl border border-border bg-card p-4"
                }
              >
                {editingItineraryId === item.id ? (
                  <div className="space-y-3">
                    <div className="grid gap-3 sm:grid-cols-[100px_1fr]">
                      <Input
                        type="number"
                        min={0}
                        value={editDayNumber}
                        onChange={(e) => setEditDayNumber(e.target.value)}
                        className="h-10 rounded-2xl"
                      />
                      <Input
                        value={editDayTitle}
                        onChange={(e) => setEditDayTitle(e.target.value)}
                        className="h-10 rounded-2xl"
                      />
                    </div>
                    <Input
                      value={editDayDescription}
                      onChange={(e) => setEditDayDescription(e.target.value)}
                      className="h-10 rounded-2xl"
                    />
                    <div className="flex flex-wrap gap-2">
                      <Button
                        size="sm"
                        className="rounded-2xl"
                        disabled={busy || !editDayTitle.trim()}
                        onClick={() => void onSaveItinerary(item.id)}
                      >
                        Save
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="rounded-2xl"
                        onClick={() => setEditingItineraryId(null)}
                      >
                        Cancel
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-start gap-3">
                    <div className="min-w-0 flex-1">
                      <p className="font-medium">
                        {itineraryDayLabel(item.dayNumber, lengthDays)}: {item.title}
                      </p>
                      {extra ? (
                        <p className="mt-1 text-xs font-medium text-amber-800/80">Optional extra</p>
                      ) : null}
                      {item.description ? (
                        <p className="mt-1 text-sm text-muted-foreground">{item.description}</p>
                      ) : null}
                    </div>
                    {canEdit ? (
                      <div className="flex shrink-0 gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          className="rounded-2xl"
                          onClick={() => {
                            setEditingItineraryId(item.id)
                            setEditDayNumber(String(item.dayNumber))
                            setEditDayTitle(item.title)
                            setEditDayDescription(item.description ?? "")
                          }}
                        >
                          <Pencil className="size-3.5" />
                          Edit
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          className="rounded-2xl"
                          disabled={busy}
                          onClick={() => void onDeleteItinerary(item.id)}
                        >
                          <Trash2 className="size-3.5" />
                          Delete
                        </Button>
                      </div>
                    ) : null}
                  </div>
                )}
              </li>
                )
            })}
          </ul>
        )}
      </section>

      <ItemSection
        title="Inclusions"
        emptyLabel="No inclusions yet."
        canEdit={canEdit}
        busy={busy}
        items={inclusions}
        draft={inclusionText}
        onDraftChange={setInclusionText}
        onAdd={onAddInclusion}
        addPlaceholder="Hotel stay, breakfast…"
        editingId={editingInclusionId}
        editText={editInclusionText}
        onStartEdit={(item) => {
          setEditingInclusionId(item.id)
          setEditInclusionText(item.description)
        }}
        onCancelEdit={() => setEditingInclusionId(null)}
        onEditTextChange={setEditInclusionText}
        onSave={(itemId) => void onSaveInclusion(itemId)}
        onDelete={(itemId) => void onDeleteInclusion(itemId)}
      />

      <ItemSection
        title="Exclusions"
        emptyLabel="No exclusions yet."
        canEdit={canEdit}
        busy={busy}
        items={exclusions}
        draft={exclusionText}
        onDraftChange={setExclusionText}
        onAdd={onAddExclusion}
        addPlaceholder="Flights, personal expenses…"
        editingId={editingExclusionId}
        editText={editExclusionText}
        onStartEdit={(item) => {
          setEditingExclusionId(item.id)
          setEditExclusionText(item.description)
        }}
        onCancelEdit={() => setEditingExclusionId(null)}
        onEditTextChange={setEditExclusionText}
        onSave={(itemId) => void onSaveExclusion(itemId)}
        onDelete={(itemId) => void onDeleteExclusion(itemId)}
      />

      <div className="mt-8 flex flex-wrap gap-3">
        <Button className="rounded-2xl" render={<Link to="/agency/trips" />}>
          Done
        </Button>
        <Button
          variant="outline"
          className="rounded-2xl"
          render={<Link to={`/agency/trips/${trip.id}/preview`} />}
        >
          <Eye className="size-4" />
          Preview
        </Button>
        <Button
          className="rounded-2xl"
          disabled={busy || !itineraryReady}
          onClick={() => setPublishOpen(true)}
        >
          <Rocket className="size-4" />
          Publish
        </Button>
      </div>

      <ConfirmDialog
        open={publishOpen}
        onOpenChange={setPublishOpen}
        title="Publish this trip?"
        description="Travelers will be able to see and book it. You will not be able to edit photos or itinerary after publishing."
        confirmLabel="Publish"
        confirmDisabled={busy}
        onConfirm={() => void onPublish()}
      />
    </div>
  )
}

type ItemSectionProps = {
  title: string
  emptyLabel: string
  canEdit: boolean
  busy: boolean
  items: TripItem[]
  draft: string
  onDraftChange: (value: string) => void
  onAdd: (event: FormEvent) => void | Promise<void>
  addPlaceholder: string
  editingId: number | null
  editText: string
  onStartEdit: (item: TripItem) => void
  onCancelEdit: () => void
  onEditTextChange: (value: string) => void
  onSave: (itemId: number) => void
  onDelete: (itemId: number) => void
}

function ItemSection({
  title,
  emptyLabel,
  canEdit,
  busy,
  items,
  draft,
  onDraftChange,
  onAdd,
  addPlaceholder,
  editingId,
  editText,
  onStartEdit,
  onCancelEdit,
  onEditTextChange,
  onSave,
  onDelete,
}: ItemSectionProps) {
  return (
    <section className="mb-10 space-y-4">
      <h2 className="text-xl font-semibold tracking-tight">{title}</h2>

      {canEdit ? (
        <form
          onSubmit={(event) => void onAdd(event)}
          className="flex flex-col gap-3 rounded-2xl border border-border bg-card p-4 sm:flex-row"
        >
          <Input
            value={draft}
            onChange={(e) => onDraftChange(e.target.value)}
            className="h-10 rounded-2xl"
            placeholder={addPlaceholder}
            required
          />
          <Button type="submit" className="rounded-2xl sm:shrink-0" disabled={busy || !draft.trim()}>
            <Plus className="size-4" />
            Add
          </Button>
        </form>
      ) : null}

      {items.length === 0 ? (
        <p className="text-sm text-muted-foreground">{emptyLabel}</p>
      ) : (
        <ul className="space-y-3">
          {items.map((item) => (
            <li key={item.id} className="rounded-2xl border border-border bg-card p-4">
              {editingId === item.id ? (
                <div className="space-y-3">
                  <Input
                    value={editText}
                    onChange={(e) => onEditTextChange(e.target.value)}
                    className="h-10 rounded-2xl"
                  />
                  <div className="flex flex-wrap gap-2">
                    <Button
                      size="sm"
                      className="rounded-2xl"
                      disabled={busy || !editText.trim()}
                      onClick={() => onSave(item.id)}
                    >
                      Save
                    </Button>
                    <Button size="sm" variant="outline" className="rounded-2xl" onClick={onCancelEdit}>
                      Cancel
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="flex items-start gap-3">
                  <p className="min-w-0 flex-1 text-sm">{item.description}</p>
                  {canEdit ? (
                    <div className="flex shrink-0 gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        className="rounded-2xl"
                        onClick={() => onStartEdit(item)}
                      >
                        <Pencil className="size-3.5" />
                        Edit
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="rounded-2xl"
                        disabled={busy}
                        onClick={() => onDelete(item.id)}
                      >
                        <Trash2 className="size-3.5" />
                        Delete
                      </Button>
                    </div>
                  ) : null}
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

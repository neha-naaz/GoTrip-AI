import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { ArrowRight, MapPin } from "lucide-react"
import { ApiError } from "@/api/client"
import { listAgencyTrips } from "@/api/agencyTrips"
import { listMyBookings } from "@/api/booking"
import { getTrip } from "@/api/trips"
import type { Booking, Trip, UserRole } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { formatTripDateRange, formatTripMoney } from "@/lib/trip-display"

const RECENT_LIMIT = 5

type RecentItem = {
  key: string
  tripId: number
  title: string
  route: string
  meta: string
  status: string
  href: string
}

function roleLabel(role: UserRole | undefined) {
  if (role === "CUSTOMER") return "Traveler"
  if (role === "AGENCY") return "Agency"
  if (role === "ADMIN") return "Admin"
  return "—"
}

function verificationLabel(status: string | null | undefined) {
  if (status === "VERIFIED") return "Verified"
  if (status === "PENDING") return "Pending verification"
  if (status === "REJECTED") return "Rejected"
  return null
}

function verificationStyles(status: string | null | undefined) {
  if (status === "VERIFIED") return "bg-emerald-50 text-emerald-800"
  if (status === "PENDING") return "bg-amber-50 text-amber-800"
  if (status === "REJECTED") return "bg-red-50 text-red-800"
  return "bg-muted text-muted-foreground"
}

function statusStyles(status: string) {
  switch (status) {
    case "CONFIRMED":
    case "PUBLISHED":
      return "bg-emerald-50 text-emerald-800"
    case "PENDING_PAYMENT":
    case "DRAFT":
      return "bg-amber-50 text-amber-800"
    case "EXPIRED":
    case "CANCELLED":
      return "bg-zinc-100 text-zinc-600"
    default:
      return "bg-muted text-muted-foreground"
  }
}

function statusLabel(status: string) {
  return status.replaceAll("_", " ")
}

function initials(name: string | undefined, email: string | undefined) {
  const source = (name?.trim() || email || "?").trim()
  const parts = source.split(/\s+/).filter(Boolean)
  if (parts.length >= 2) {
    return `${parts[0]![0] ?? ""}${parts[1]![0] ?? ""}`.toUpperCase()
  }
  return source.slice(0, 2).toUpperCase()
}

export function MePage() {
  const { user } = useAuth()
  const [items, setItems] = useState<RecentItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const seeAllHref = user?.role === "AGENCY" ? "/agency/trips" : "/bookings"
  const seeAllLabel = user?.role === "AGENCY" ? "See all trips" : "See all bookings"
  const sectionTitle = user?.role === "AGENCY" ? "Your trips" : "Your bookings"

  useEffect(() => {
    if (!user) {
      setLoading(false)
      return
    }

    let cancelled = false

    async function loadCustomer() {
      const bookings = (await listMyBookings()).slice(0, RECENT_LIMIT)
      const trips = await Promise.all(
        bookings.map(async (booking) => {
          try {
            return await getTrip(booking.tripId)
          } catch {
            return null
          }
        }),
      )

      return bookings.map((booking: Booking, index) => {
        const trip = trips[index]
        return {
          key: `booking-${booking.id}`,
          tripId: booking.tripId,
          title: trip?.title ?? `Trip #${booking.tripId}`,
          route: trip ? `${trip.source} → ${trip.destination}` : "Trip details unavailable",
          meta: trip
            ? `${formatTripDateRange(trip.startDate, trip.endDate)} · ${formatTripMoney(booking.amountDue)}`
            : formatTripMoney(booking.amountDue),
          status: booking.status,
          href: `/trips/${booking.tripId}`,
        } satisfies RecentItem
      })
    }

    async function loadAgency() {
      const trips = (await listAgencyTrips())
        .filter((t) => t.status === "DRAFT" || t.status === "PUBLISHED")
        .slice(0, RECENT_LIMIT)

      return trips.map(
        (trip: Trip) =>
          ({
            key: `trip-${trip.id}`,
            tripId: trip.id,
            title: trip.title,
            route: `${trip.source} → ${trip.destination}`,
            meta: `${formatTripDateRange(trip.startDate, trip.endDate)} · ${formatTripMoney(trip.price)}`,
            status: trip.status,
            href: trip.status === "PUBLISHED" ? `/trips/${trip.id}` : `/agency/trips/${trip.id}/edit`,
          }) satisfies RecentItem,
      )
    }

    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const next =
          user.role === "AGENCY"
            ? await loadAgency()
            : user.role === "CUSTOMER"
              ? await loadCustomer()
              : []
        if (!cancelled) setItems(next)
      } catch (err) {
        if (!cancelled) {
          setItems([])
          setError(err instanceof ApiError ? err.message : "Could not load recent trips")
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [user])

  const isAgency = user?.role === "AGENCY"
  const displayName = isAgency
    ? user?.agencyName?.trim() || user?.name?.trim() || user?.email || "Agency"
    : user?.name?.trim() || user?.email || "Account"
  const verificationText = isAgency ? verificationLabel(user?.verificationStatus) : null
  const secondaryLine = isAgency
    ? user?.name?.trim() || null
    : null

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      {/* Profile */}
      <section>
        <div className="flex items-start justify-between gap-6">
          <div className="flex min-w-0 flex-1 items-start gap-5">
            <div
              className="flex size-16 shrink-0 items-center justify-center rounded-2xl bg-primary text-lg font-semibold text-primary-foreground sm:size-20 sm:text-xl"
              aria-hidden
            >
              {initials(isAgency ? user?.agencyName ?? user?.name : user?.name, user?.email)}
            </div>
            <div className="min-w-0 flex-1 pt-0.5">
              <p className="text-sm font-medium tracking-wide text-primary">{roleLabel(user?.role)}</p>
              <h1 className="mt-1 truncate text-3xl font-semibold tracking-tight">{displayName}</h1>
              {secondaryLine ? (
                <p className="mt-1 truncate text-sm text-foreground/80">{secondaryLine}</p>
              ) : null}
              <p className="mt-2 truncate text-muted-foreground">{user?.email}</p>
            </div>
          </div>

          {/* Agency-only meta (verification now; followers later) */}
          {isAgency ? (
            <div className="flex shrink-0 flex-col items-end gap-3 pt-1 text-right">
              {verificationText ? (
                <span
                  className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${verificationStyles(user?.verificationStatus)}`}
                >
                  {verificationText}
                </span>
              ) : null}
            </div>
          ) : null}
        </div>
      </section>

      {/* Recent trips */}
      <section className="mt-14">
        <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold tracking-tight">{sectionTitle}</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              {user?.role === "AGENCY"
                ? "Your latest drafts and published trips"
                : "Your latest seat reservations"}
            </p>
          </div>
          {user?.role === "CUSTOMER" || user?.role === "AGENCY" ? (
            <Button variant="outline" size="sm" className="rounded-2xl" render={<Link to={seeAllHref} />}>
              {seeAllLabel}
              <ArrowRight className="size-3.5" />
            </Button>
          ) : null}
        </div>

        {loading ? (
          <p className="py-12 text-center text-sm text-muted-foreground">Loading…</p>
        ) : null}

        {!loading && error ? (
          <p className="rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</p>
        ) : null}

        {!loading && !error && items.length === 0 ? (
          <div className="rounded-2xl bg-muted/50 px-6 py-14 text-center">
            <p className="font-medium">
              {user?.role === "AGENCY" ? "No trips yet" : "No bookings yet"}
            </p>
            <p className="mt-1 text-sm text-muted-foreground">
              {user?.role === "AGENCY"
                ? "Create a draft itinerary to get started."
                : "Explore trips and reserve a seat."}
            </p>
            <Button
              className="mt-6 rounded-2xl"
              render={
                <Link to={user?.role === "AGENCY" ? "/agency/trips/new" : "/trips"} />
              }
            >
              {user?.role === "AGENCY" ? "Create trip" : "Explore trips"}
            </Button>
          </div>
        ) : null}

        {!loading && !error && items.length > 0 ? (
          <ul className="space-y-2">
            {items.map((item) => (
              <li key={item.key}>
                <Link
                  to={item.href}
                  className="flex items-start justify-between gap-4 rounded-2xl bg-muted/45 px-4 py-4 transition-colors hover:bg-muted/80"
                >
                  <div className="min-w-0">
                    <p className="truncate font-semibold tracking-tight">{item.title}</p>
                    <p className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
                      <MapPin className="size-3.5 shrink-0" />
                      <span className="truncate">{item.route}</span>
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">{item.meta}</p>
                  </div>
                  <span
                    className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium capitalize ${statusStyles(item.status)}`}
                  >
                    {statusLabel(item.status)}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        ) : null}
      </section>
    </div>
  )
}

import { Link } from "react-router-dom"
import { MapPin } from "lucide-react"
import type { Trip } from "@/api/types"
import { formatTripDateRange, formatTripMoney, tripImageForDestination } from "@/lib/trip-display"

type TripCardProps = {
  trip: Trip
}

/**
 * Image-first discovery card.
 * Parent pages pass a Trip object; this component only displays it.
 */
export function TripCard({ trip }: TripCardProps) {
  const imageUrl = tripImageForDestination(trip.destination)

  return (
    <Link
      to={`/trips/${trip.id}`}
      className="group block overflow-hidden rounded-2xl border border-border bg-card shadow-sm transition-shadow duration-200 hover:shadow-md"
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-muted">
        <img
          src={imageUrl}
          alt={`${trip.destination} trip`}
          className="size-full object-cover transition-transform duration-300 group-hover:scale-[1.03]"
          loading="lazy"
        />
      </div>

      <div className="space-y-2 p-4">
        <h3 className="line-clamp-2 text-base font-semibold tracking-tight text-foreground">
          {trip.title}
        </h3>

        <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
          <MapPin className="size-3.5 shrink-0" />
          <span className="truncate">
            {trip.source} → {trip.destination}
          </span>
        </p>

        <div className="flex items-end justify-between gap-3 pt-1">
          <p className="text-sm text-muted-foreground">
            {formatTripDateRange(trip.startDate, trip.endDate)}
          </p>
          <div className="text-right">
            <p className="text-base font-semibold text-foreground">{formatTripMoney(trip.price)}</p>
            <p className="text-xs text-muted-foreground">
              book from {formatTripMoney(trip.bookingAmount)}
            </p>
          </div>
        </div>
      </div>
    </Link>
  )
}

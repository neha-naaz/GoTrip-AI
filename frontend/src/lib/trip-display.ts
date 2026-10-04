import { resolveMediaUrl } from "@/api/client"
import type { Trip } from "@/api/types"

/**
 * Fallback Unsplash photo from destination when the trip has no cover image yet.
 */
const DESTINATION_IMAGES: Record<string, string> = {
  manali: "https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1200&q=80",
  goa: "https://images.unsplash.com/photo-1614082242765-7c98ca0f3df3?auto=format&fit=crop&w=1200&q=80",
  jaipur: "https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1200&q=80",
  leh: "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1200&q=80",
  default: "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1200&q=80",
}

export function tripImageForDestination(destination: string): string {
  const key = destination.trim().toLowerCase()
  return DESTINATION_IMAGES[key] ?? DESTINATION_IMAGES.default
}

export function tripCoverUrl(trip: Pick<Trip, "destination" | "coverImageUrl">): string {
  return resolveMediaUrl(trip.coverImageUrl) ?? tripImageForDestination(trip.destination)
}

export function formatTripMoney(amount: number): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount)
}

export function tripLengthDays(startDate: string, endDate: string): number {
  const start = new Date(`${startDate}T00:00:00`)
  const end = new Date(`${endDate}T00:00:00`)
  return Math.round((end.getTime() - start.getTime()) / 86_400_000) + 1
}

export function isOptionalItineraryDay(dayNumber: number, tripLength: number): boolean {
  return dayNumber === 0 || dayNumber > tripLength
}

export function itineraryDayLabel(dayNumber: number, tripLength?: number): string {
  if (dayNumber === 0) return "Day 0 · Travel / prep"
  if (tripLength != null && dayNumber > tripLength) return `Day ${dayNumber} · Extra`
  return `Day ${dayNumber}`
}

export function formatTripDateRange(startDate: string, endDate: string): string {
  const start = new Date(startDate)
  const end = new Date(endDate)
  const opts: Intl.DateTimeFormatOptions = { day: "numeric", month: "short" }
  return `${start.toLocaleDateString("en-IN", opts)} – ${end.toLocaleDateString("en-IN", opts)}`
}

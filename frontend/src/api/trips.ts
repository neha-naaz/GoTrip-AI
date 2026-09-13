import { apiRequest } from "@/api/client"
import type { Trip, TripDetail, TripSearchParams } from "@/api/types"

/**
 * Public trip APIs — no JWT required.
 * Backend: GET /api/trips , GET /api/trips/{id}
 */
export function listTrips(params: TripSearchParams = {}) {
  const query = new URLSearchParams()

  if (params.source?.trim()) {
    query.set("source", params.source.trim())
  }
  if (params.destination?.trim()) {
    query.set("destination", params.destination.trim())
  }

  const suffix = query.toString() ? `?${query.toString()}` : ""
  return apiRequest<Trip[]>(`/api/trips${suffix}`, { method: "GET" }, false)
}

export function getTrip(tripId: number) {
  return apiRequest<TripDetail>(`/api/trips/${tripId}`, { method: "GET" }, false)
}

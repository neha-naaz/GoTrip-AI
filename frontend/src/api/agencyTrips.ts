import { apiRequest } from "@/api/client"
import type { CreateTripPayload, Trip } from "@/api/types"

export function listAgencyTrips() {
  return apiRequest<Trip[]>("/api/agency/trips")
}

export function createAgencyTrip(payload: CreateTripPayload) {
  return apiRequest<Trip>("/api/agency/trips", {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export function publishAgencyTrip(tripId: number) {
  return apiRequest<Trip>(`/api/agency/trips/${tripId}/publish`, {
    method: "POST",
  })
}

export function deleteAgencyTrip(tripId: number) {
  return apiRequest<void>(`/api/agency/trips/${tripId}`, {
    method: "DELETE",
  })
}

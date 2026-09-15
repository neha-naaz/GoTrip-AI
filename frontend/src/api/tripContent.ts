import { apiRequest } from "@/api/client"
import type { TripItinerary, TripItem } from "@/api/types"

export type ItineraryPayload = {
  dayNumber: number
  title: string
  description?: string
}

export type TripItemPayload = {
  description: string
}

function base(tripId: number) {
  return `/api/agency/trips/${tripId}`
}

export function listItineraries(tripId: number) {
  return apiRequest<TripItinerary[]>(`${base(tripId)}/itineraries`)
}

export function createItinerary(tripId: number, payload: ItineraryPayload) {
  return apiRequest<TripItinerary>(`${base(tripId)}/itineraries`, {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export function updateItinerary(tripId: number, itineraryId: number, payload: ItineraryPayload) {
  return apiRequest<TripItinerary>(`${base(tripId)}/itineraries/${itineraryId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  })
}

export function deleteItinerary(tripId: number, itineraryId: number) {
  return apiRequest<void>(`${base(tripId)}/itineraries/${itineraryId}`, {
    method: "DELETE",
  })
}

export function listInclusions(tripId: number) {
  return apiRequest<TripItem[]>(`${base(tripId)}/inclusions`)
}

export function createInclusion(tripId: number, payload: TripItemPayload) {
  return apiRequest<TripItem>(`${base(tripId)}/inclusions`, {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export function updateInclusion(tripId: number, inclusionId: number, payload: TripItemPayload) {
  return apiRequest<TripItem>(`${base(tripId)}/inclusions/${inclusionId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  })
}

export function deleteInclusion(tripId: number, inclusionId: number) {
  return apiRequest<void>(`${base(tripId)}/inclusions/${inclusionId}`, {
    method: "DELETE",
  })
}

export function listExclusions(tripId: number) {
  return apiRequest<TripItem[]>(`${base(tripId)}/exclusions`)
}

export function createExclusion(tripId: number, payload: TripItemPayload) {
  return apiRequest<TripItem>(`${base(tripId)}/exclusions`, {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export function updateExclusion(tripId: number, exclusionId: number, payload: TripItemPayload) {
  return apiRequest<TripItem>(`${base(tripId)}/exclusions/${exclusionId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  })
}

export function deleteExclusion(tripId: number, exclusionId: number) {
  return apiRequest<void>(`${base(tripId)}/exclusions/${exclusionId}`, {
    method: "DELETE",
  })
}

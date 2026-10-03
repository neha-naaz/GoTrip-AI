import { apiRequest, apiUpload } from "@/api/client"
import type { TripImage } from "@/api/types"

export function listTripImages(tripId: number) {
  return apiRequest<TripImage[]>(`/api/agency/trips/${tripId}/images`)
}

export function uploadTripImage(tripId: number, file: File) {
  const formData = new FormData()
  formData.append("file", file)
  return apiUpload<TripImage>(`/api/agency/trips/${tripId}/images/upload`, formData)
}

export function addTripImageUrl(tripId: number, url: string) {
  return apiRequest<TripImage>(`/api/agency/trips/${tripId}/images/url`, {
    method: "POST",
    body: JSON.stringify({ url }),
  })
}

export function reorderTripImages(tripId: number, imageIds: number[]) {
  return apiRequest<TripImage[]>(`/api/agency/trips/${tripId}/images/reorder`, {
    method: "PUT",
    body: JSON.stringify({ imageIds }),
  })
}

export function deleteTripImage(tripId: number, imageId: number) {
  return apiRequest<void>(`/api/agency/trips/${tripId}/images/${imageId}`, {
    method: "DELETE",
  })
}

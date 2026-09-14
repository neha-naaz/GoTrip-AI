import { apiRequest } from "@/api/client"
import type { ChatMessage, GroupMember, TripGroup } from "@/api/types"

export function getTripGroup(tripId: number) {
  return apiRequest<TripGroup>(`/api/trips/${tripId}/group`)
}

export function listGroupMembers(tripId: number) {
  return apiRequest<GroupMember[]>(`/api/trips/${tripId}/group/members`)
}

export function listChatMessages(groupId: number, limit = 50) {
  return apiRequest<ChatMessage[]>(`/api/groups/${groupId}/messages?limit=${limit}`)
}

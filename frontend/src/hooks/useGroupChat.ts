import { useCallback, useEffect, useRef, useState } from "react"
import { Client, type IMessage } from "@stomp/stompjs"
import { listChatMessages } from "@/api/groups"
import type { ChatMessage } from "@/api/types"

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"

function toWsBase(httpBase: string) {
  return httpBase.replace(/^http/, "ws")
}

/** History + any live messages that arrived before history resolved. Never drop either side. */
function mergeMessages(existing: ChatMessage[], incoming: ChatMessage[]) {
  const byId = new Map<number, ChatMessage>()
  for (const message of existing) byId.set(message.id, message)
  for (const message of incoming) {
    if (!byId.has(message.id)) byId.set(message.id, message)
  }
  return [...byId.values()].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime() || a.id - b.id,
  )
}

function parseChatMessage(raw: string): ChatMessage | null {
  try {
    const parsed = JSON.parse(raw) as Partial<ChatMessage>
    if (
      typeof parsed.id !== "number" ||
      typeof parsed.groupId !== "number" ||
      typeof parsed.senderUserId !== "number" ||
      typeof parsed.content !== "string" ||
      typeof parsed.createdAt !== "string"
    ) {
      return null
    }
    return {
      id: parsed.id,
      groupId: parsed.groupId,
      senderUserId: parsed.senderUserId,
      senderName: typeof parsed.senderName === "string" && parsed.senderName.trim()
        ? parsed.senderName.trim()
        : `Traveler #${parsed.senderUserId}`,
      content: parsed.content,
      createdAt: parsed.createdAt,
    }
  } catch {
    return null
  }
}

type UseGroupChatOptions = {
  groupId: number | null
  token: string | null
  enabled?: boolean
}

export function useGroupChat({ groupId, token, enabled = true }: UseGroupChatOptions) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [connected, setConnected] = useState(false)
  const [loadingHistory, setLoadingHistory] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!enabled || groupId == null || token == null) return

    let cancelled = false
    setMessages([])
    setLoadingHistory(true)
    setError(null)

    ;(async () => {
      try {
        const history = await listChatMessages(groupId)
        if (cancelled) return
        setMessages((prev) => mergeMessages(history, prev))
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Could not load chat history")
        }
      } finally {
        if (!cancelled) setLoadingHistory(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [enabled, groupId, token])

  useEffect(() => {
    if (!enabled || groupId == null || token == null) return

    setConnected(false)
    setError(null)

    const client = new Client({
      brokerURL: `${toWsBase(API_BASE_URL)}/ws`,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        setError(null)
        client.subscribe(`/topic/groups/${groupId}`, (message: IMessage) => {
          const body = parseChatMessage(message.body)
          if (body == null) {
            setError("Received invalid chat message")
            return
          }
          setMessages((prev) => mergeMessages(prev, [body]))
        })
        client.subscribe("/user/queue/errors", (message: IMessage) => {
          setError(message.body || "Chat error")
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        setError(frame.headers["message"] || "STOMP error")
        setConnected(false)
      },
      onWebSocketError: () => {
        setError("WebSocket connection failed")
        setConnected(false)
      },
    })

    clientRef.current = client
    client.activate()

    return () => {
      clientRef.current = null
      void client.deactivate()
      setConnected(false)
    }
  }, [enabled, groupId, token])

  const sendMessage = useCallback(
    (content: string) => {
      const trimmed = content.trim()
      const client = clientRef.current
      if (!trimmed || groupId == null || !client?.connected) {
        return false
      }
      client.publish({
        destination: `/app/groups/${groupId}/send`,
        body: JSON.stringify({ content: trimmed }),
      })
      return true
    },
    [groupId],
  )

  return {
    messages,
    connected,
    loadingHistory,
    error,
    sendMessage,
  }
}

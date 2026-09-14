import { useEffect, useRef, useState, type FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { ArrowLeft, Send, Users } from "lucide-react"
import { ApiError } from "@/api/client"
import { getTripGroup, listGroupMembers } from "@/api/groups"
import { getTrip } from "@/api/trips"
import type { Trip } from "@/api/types"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useGroupChat } from "@/hooks/useGroupChat"

export function TripChatPage() {
  const { tripId } = useParams()
  const { user, token } = useAuth()
  const [trip, setTrip] = useState<Trip | null>(null)
  const [groupId, setGroupId] = useState<number | null>(null)
  const [memberCount, setMemberCount] = useState(0)
  const [bootError, setBootError] = useState<string | null>(null)
  const [booting, setBooting] = useState(true)
  const [draft, setDraft] = useState("")
  const bottomRef = useRef<HTMLDivElement | null>(null)

  const { messages, connected, loadingHistory, error, sendMessage } = useGroupChat({
    groupId,
    token,
    enabled: Boolean(groupId && token),
  })

  useEffect(() => {
    const id = Number(tripId)
    if (!Number.isFinite(id)) {
      setBootError("Invalid trip")
      setBooting(false)
      return
    }

    let cancelled = false
    ;(async () => {
      setBooting(true)
      setBootError(null)
      try {
        const [tripData, group, members] = await Promise.all([
          getTrip(id),
          getTripGroup(id),
          listGroupMembers(id),
        ])
        if (cancelled) return
        setTrip(tripData)
        setGroupId(group.id)
        setMemberCount(members.length)
      } catch (err) {
        if (!cancelled) {
          setBootError(err instanceof ApiError ? err.message : "Unable to open chat")
        }
      } finally {
        if (!cancelled) setBooting(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [tripId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages.length])

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (sendMessage(draft)) {
      setDraft("")
    }
  }

  if (booting) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center text-sm text-muted-foreground">
        Opening group chat…
      </div>
    )
  }

  if (bootError || !trip || !groupId) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6">
        <p className="text-lg font-medium">{bootError ?? "Chat unavailable"}</p>
        <p className="mt-2 text-sm text-muted-foreground">
          Confirm your booking first, then return here.
        </p>
        <Button className="mt-6 rounded-2xl" render={<Link to="/bookings" />}>
          <ArrowLeft className="size-4" />
          My bookings
        </Button>
      </div>
    )
  }

  return (
    <div className="mx-auto flex h-[calc(100vh-4rem)] max-w-3xl flex-col px-4 sm:px-6">
      <header className="flex items-start justify-between gap-3 border-b border-border py-4">
        <div>
          <Link
            to="/bookings"
            className="mb-2 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="size-3.5" />
            Bookings
          </Link>
          <h1 className="text-xl font-semibold tracking-tight sm:text-2xl">{trip.title}</h1>
          <p className="mt-1 flex items-center gap-3 text-sm text-muted-foreground">
            <span className="inline-flex items-center gap-1.5">
              <Users className="size-3.5" />
              {memberCount} members
            </span>
            <span className={connected ? "text-emerald-700" : "text-amber-700"}>
              {connected ? "Live" : "Connecting…"}
            </span>
          </p>
        </div>
      </header>

      <div className="min-h-0 flex-1 space-y-3 overflow-y-auto py-4">
        {loadingHistory ? (
          <p className="text-center text-sm text-muted-foreground">Loading messages…</p>
        ) : null}

        {!loadingHistory && messages.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-border px-4 py-10 text-center">
            <p className="font-medium">No messages yet</p>
            <p className="mt-1 text-sm text-muted-foreground">Say hi to your travel group.</p>
          </div>
        ) : null}

        {messages.map((message) => {
          const mine = message.senderUserId === user?.userId
          return (
            <div key={message.id} className={`flex ${mine ? "justify-end" : "justify-start"}`}>
              <div
                className={`max-w-[80%] rounded-2xl px-3.5 py-2 text-sm shadow-sm ${
                  mine
                    ? "bg-primary text-primary-foreground"
                    : "border border-border bg-card text-foreground"
                }`}
              >
                {!mine ? (
                  <p className="mb-1 text-[11px] font-medium opacity-70">User #{message.senderUserId}</p>
                ) : null}
                <p className="whitespace-pre-wrap break-words">{message.content}</p>
                <p className={`mt-1 text-[10px] ${mine ? "text-primary-foreground/70" : "text-muted-foreground"}`}>
                  {new Date(message.createdAt).toLocaleTimeString([], {
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </p>
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {error ? (
        <p className="mb-2 rounded-2xl bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>
      ) : null}

      <form onSubmit={onSubmit} className="flex gap-2 border-t border-border py-4">
        <Input
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          placeholder="Write a message…"
          className="h-11 rounded-2xl"
          maxLength={1000}
          disabled={!connected}
        />
        <Button type="submit" className="h-11 rounded-2xl px-4" disabled={!connected || !draft.trim()}>
          <Send className="size-4" />
          Send
        </Button>
      </form>
    </div>
  )
}

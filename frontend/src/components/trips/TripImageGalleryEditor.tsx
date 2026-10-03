import { useEffect, useState, type FormEvent } from "react"
import { ImagePlus, Link2, Star, Trash2 } from "lucide-react"
import { ApiError, resolveMediaUrl } from "@/api/client"
import {
  addTripImageUrl,
  deleteTripImage,
  listTripImages,
  reorderTripImages,
  uploadTripImage,
} from "@/api/tripImages"
import type { TripImage } from "@/api/types"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

const MAX_IMAGES = 6

type TripImageGalleryEditorProps = {
  tripId: number
  disabled?: boolean
}

export function TripImageGalleryEditor({ tripId, disabled = false }: TripImageGalleryEditorProps) {
  const [images, setImages] = useState<TripImage[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [url, setUrl] = useState("")

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setImages(await listTripImages(tripId))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load images")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [tripId])

  async function run(action: () => Promise<void>) {
    setBusy(true)
    setError(null)
    try {
      await action()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Image action failed")
    } finally {
      setBusy(false)
    }
  }

  async function onUpload(fileList: FileList | null) {
    const file = fileList?.[0]
    if (!file || disabled) return
    await run(async () => {
      const created = await uploadTripImage(tripId, file)
      setImages((prev) => [...prev, created])
    })
  }

  async function onAddUrl(event: FormEvent) {
    event.preventDefault()
    if (disabled || !url.trim()) return
    await run(async () => {
      const created = await addTripImageUrl(tripId, url.trim())
      setImages((prev) => [...prev, created])
      setUrl("")
    })
  }

  async function onDelete(imageId: number) {
    if (disabled) return
    await run(async () => {
      await deleteTripImage(tripId, imageId)
      const next = await listTripImages(tripId)
      setImages(next)
    })
  }

  async function onMakeCover(imageId: number) {
    if (disabled) return
    const ordered = [
      imageId,
      ...images.filter((img) => img.id !== imageId).map((img) => img.id),
    ]
    await run(async () => {
      setImages(await reorderTripImages(tripId, ordered))
    })
  }

  const atLimit = images.length >= MAX_IMAGES
  const canEdit = !disabled && !busy

  return (
    <section className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold tracking-tight">Photos</h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Up to {MAX_IMAGES} images (5MB each). First is the cover. Upload a file or paste a URL.
        </p>
      </div>

      {error ? (
        <p className="rounded-2xl bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading photos…</p>
      ) : (
        <ul className="grid gap-3 sm:grid-cols-2">
          {images.map((image) => {
            const src = resolveMediaUrl(image.url) ?? image.url
            return (
              <li
                key={image.id}
                className="overflow-hidden rounded-2xl border border-border bg-card"
              >
                <div className="relative aspect-[4/3] bg-muted">
                  <img src={src} alt="" className="size-full object-cover" />
                  {image.cover ? (
                    <span className="absolute left-2 top-2 rounded-full bg-background/90 px-2 py-0.5 text-xs font-medium">
                      Cover
                    </span>
                  ) : null}
                </div>
                <div className="flex flex-wrap gap-2 p-3">
                  {!image.cover ? (
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      className="rounded-2xl"
                      disabled={!canEdit}
                      onClick={() => void onMakeCover(image.id)}
                    >
                      <Star className="size-3.5" />
                      Make cover
                    </Button>
                  ) : null}
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    className="rounded-2xl"
                    disabled={!canEdit}
                    onClick={() => void onDelete(image.id)}
                  >
                    <Trash2 className="size-3.5" />
                    Remove
                  </Button>
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {!disabled && !atLimit ? (
        <div className="space-y-3 rounded-2xl border border-border bg-card p-4">
          <div className="space-y-1.5">
            <Label htmlFor={`upload-${tripId}`}>Upload from computer</Label>
            <Input
              id={`upload-${tripId}`}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              disabled={!canEdit}
              className="h-11 rounded-2xl file:mr-3 file:rounded-xl file:border-0 file:bg-muted file:px-3 file:py-1.5 file:text-sm"
              onChange={(e) => {
                void onUpload(e.target.files)
                e.target.value = ""
              }}
            />
          </div>
          <form onSubmit={onAddUrl} className="flex flex-col gap-2 sm:flex-row sm:items-end">
            <div className="min-w-0 flex-1 space-y-1.5">
              <Label htmlFor={`url-${tripId}`}>Or paste image URL</Label>
              <Input
                id={`url-${tripId}`}
                type="url"
                placeholder="https://…"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                disabled={!canEdit}
                className="h-11 rounded-2xl"
              />
            </div>
            <Button type="submit" className="h-11 rounded-2xl" disabled={!canEdit || !url.trim()}>
              <Link2 className="size-4" />
              Add URL
            </Button>
          </form>
          <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <ImagePlus className="size-3.5" />
            Later: web search / AI-generated sources can plug into the same gallery.
          </p>
        </div>
      ) : null}

      {!disabled && atLimit ? (
        <p className="text-sm text-muted-foreground">Gallery full ({MAX_IMAGES} images).</p>
      ) : null}
    </section>
  )
}

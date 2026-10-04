import { useEffect, useState } from "react"
import { ChevronLeft, ChevronRight, X } from "lucide-react"
import { resolveMediaUrl } from "@/api/client"
import type { TripImage } from "@/api/types"

const PREVIEW_COUNT = 3

type TripPhotoStripProps = {
  destination: string
  images?: TripImage[]
  coverImageUrl?: string | null
}

function photoUrls(images: TripImage[] | undefined, coverImageUrl?: string | null): string[] {
  const all = [...(images ?? [])]
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((img) => resolveMediaUrl(img.url) ?? img.url)
    .filter(Boolean)
  const cover = resolveMediaUrl(coverImageUrl)
  if (!cover) return all
  return all.filter((url) => url !== cover)
}

export function TripPhotoStrip({ destination, images, coverImageUrl }: TripPhotoStripProps) {
  const urls = photoUrls(images, coverImageUrl)
  const [lightboxIndex, setLightboxIndex] = useState<number | null>(null)

  const overflow = urls.length - PREVIEW_COUNT
  const preview = overflow > 0 ? urls.slice(0, PREVIEW_COUNT) : urls
  const overflowSrc = overflow > 0 ? urls[PREVIEW_COUNT] : null

  useEffect(() => {
    if (lightboxIndex == null) return

    function onKey(event: KeyboardEvent) {
      if (event.key === "Escape") setLightboxIndex(null)
      if (event.key === "ArrowLeft") {
        setLightboxIndex((prev) =>
          prev == null || urls.length < 2 ? prev : (prev - 1 + urls.length) % urls.length,
        )
      }
      if (event.key === "ArrowRight") {
        setLightboxIndex((prev) =>
          prev == null || urls.length < 2 ? prev : (prev + 1) % urls.length,
        )
      }
    }

    const prevOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    window.addEventListener("keydown", onKey)
    return () => {
      document.body.style.overflow = prevOverflow
      window.removeEventListener("keydown", onKey)
    }
  }, [lightboxIndex, urls.length])

  if (urls.length === 0) return null

  const current = lightboxIndex != null ? urls[lightboxIndex] : null

  return (
    <>
      <section>
        <h2 className="text-xl font-semibold tracking-tight">Photos</h2>
        <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {preview.map((src, index) => (
            <button
              key={`${src}-${index}`}
              type="button"
              onClick={() => setLightboxIndex(index)}
              className="aspect-[4/3] overflow-hidden rounded-3xl bg-muted"
              aria-label={`View photo ${index + 1} of ${destination}`}
            >
              <img src={src} alt="" className="size-full object-cover" />
            </button>
          ))}
          {overflowSrc ? (
            <button
              type="button"
              onClick={() => setLightboxIndex(PREVIEW_COUNT)}
              className="relative aspect-[4/3] overflow-hidden rounded-3xl bg-muted"
              aria-label={`View all ${urls.length} photos`}
            >
              <img src={overflowSrc} alt="" className="size-full object-cover" />
              <span className="absolute inset-0 flex flex-col items-center justify-center bg-black/50 text-white">
                <span className="text-2xl font-semibold tracking-tight">{overflow}+</span>
                <span className="text-sm font-medium">View All</span>
              </span>
            </button>
          ) : null}
        </div>
      </section>

      {current ? (
        <div
          role="dialog"
          aria-modal="true"
          aria-label={`${destination} photos`}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/92"
          onClick={() => setLightboxIndex(null)}
        >
          <button
            type="button"
            aria-label="Close full view"
            onClick={() => setLightboxIndex(null)}
            className="absolute right-4 top-4 z-20 flex size-11 items-center justify-center rounded-full border border-white/20 bg-white/10 text-white transition hover:bg-white/20"
          >
            <X className="size-5" />
          </button>

          {urls.length > 1 ? (
            <>
              <button
                type="button"
                aria-label="Previous photo"
                onClick={(e) => {
                  e.stopPropagation()
                  setLightboxIndex((prev) =>
                    prev == null ? prev : (prev - 1 + urls.length) % urls.length,
                  )
                }}
                className="absolute left-3 z-20 flex size-12 items-center justify-center rounded-full border border-white/20 bg-white/10 text-white transition hover:bg-white/20 sm:left-6"
              >
                <ChevronLeft className="size-6" />
              </button>
              <button
                type="button"
                aria-label="Next photo"
                onClick={(e) => {
                  e.stopPropagation()
                  setLightboxIndex((prev) =>
                    prev == null ? prev : (prev + 1) % urls.length,
                  )
                }}
                className="absolute right-3 z-20 flex size-12 items-center justify-center rounded-full border border-white/20 bg-white/10 text-white transition hover:bg-white/20 sm:right-6"
              >
                <ChevronRight className="size-6" />
              </button>
            </>
          ) : null}

          <img
            src={current}
            alt={`${destination} photo ${(lightboxIndex ?? 0) + 1}`}
            className="max-h-[88vh] max-w-[min(96vw,1200px)] object-contain shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          />

          <p className="absolute bottom-5 left-1/2 z-20 -translate-x-1/2 rounded-full bg-white/10 px-3 py-1 text-xs text-white/90 backdrop-blur-sm">
            {(lightboxIndex ?? 0) + 1} / {urls.length}
          </p>
        </div>
      ) : null}
    </>
  )
}

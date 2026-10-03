import { useEffect, useState, type ReactNode } from "react"
import { ChevronLeft, ChevronRight, Expand, X } from "lucide-react"
import { resolveMediaUrl } from "@/api/client"
import type { TripImage } from "@/api/types"
import { tripImageForDestination } from "@/lib/trip-display"
import { cn } from "@/lib/utils"

type TripCoverCarouselProps = {
  destination: string
  coverImageUrl?: string | null
  images?: TripImage[]
  children: ReactNode
  className?: string
}

function buildSlides(
  destination: string,
  coverImageUrl: string | null | undefined,
  images: TripImage[] | undefined,
): string[] {
  const ordered = [...(images ?? [])].sort((a, b) => a.sortOrder - b.sortOrder)
  if (ordered.length > 0) {
    return ordered.map((img) => resolveMediaUrl(img.url) ?? img.url)
  }
  const cover = resolveMediaUrl(coverImageUrl)
  if (cover) return [cover]
  return [tripImageForDestination(destination)]
}

export function TripCoverCarousel({
  destination,
  coverImageUrl,
  images,
  children,
  className,
}: TripCoverCarouselProps) {
  const slides = buildSlides(destination, coverImageUrl, images)
  const [index, setIndex] = useState(0)
  const [lightboxOpen, setLightboxOpen] = useState(false)
  const hasMany = slides.length > 1
  const current = slides[Math.min(index, slides.length - 1)] ?? slides[0]

  function go(delta: number) {
    if (!hasMany) return
    setIndex((prev) => (prev + delta + slides.length) % slides.length)
  }

  function openLightbox() {
    setLightboxOpen(true)
  }

  function closeLightbox() {
    setLightboxOpen(false)
  }

  useEffect(() => {
    if (!lightboxOpen) return

    function onKey(event: KeyboardEvent) {
      if (event.key === "Escape") closeLightbox()
      if (event.key === "ArrowLeft") go(-1)
      if (event.key === "ArrowRight") go(1)
    }

    const prevOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    window.addEventListener("keydown", onKey)
    return () => {
      document.body.style.overflow = prevOverflow
      window.removeEventListener("keydown", onKey)
    }
  }, [lightboxOpen, hasMany, slides.length])

  return (
    <>
      <section className={cn("relative isolate min-h-[52vh] overflow-hidden", className)}>
        <button
          type="button"
          onClick={openLightbox}
          className="absolute inset-0 cursor-zoom-in"
          aria-label={`View full photo of ${destination}`}
        >
          <img
            key={current}
            src={current}
            alt={`${destination} photo ${index + 1}`}
            className="size-full object-cover"
          />
        </button>
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/80 via-black/35 to-black/20" />

        {hasMany ? (
          <>
            <button
              type="button"
              aria-label="Previous photo"
              onClick={() => go(-1)}
              className="absolute left-3 top-1/2 z-10 flex size-11 -translate-y-1/2 items-center justify-center rounded-full border border-white/25 bg-black/35 text-white backdrop-blur-sm transition hover:bg-black/50 sm:left-5"
            >
              <ChevronLeft className="size-5" />
            </button>

            <div className="absolute right-3 top-1/2 z-10 flex -translate-y-1/2 flex-col items-center gap-2 sm:right-5">
              <button
                type="button"
                aria-label="Next photo"
                onClick={() => go(1)}
                className="flex size-11 items-center justify-center rounded-full border border-white/25 bg-black/35 text-white backdrop-blur-sm transition hover:bg-black/50"
              >
                <ChevronRight className="size-5" />
              </button>
              {index === 0 ? (
                <button
                  type="button"
                  onClick={() => go(1)}
                  className="max-w-[9.5rem] rounded-2xl border border-white/30 bg-black/45 px-3 py-2 text-center text-xs leading-snug text-white/95 backdrop-blur-sm transition hover:bg-black/60 sm:max-w-[11rem] sm:text-sm"
                >
                  Click to see more of {destination}
                </button>
              ) : null}
            </div>

            <div className="absolute bottom-5 left-1/2 z-10 flex -translate-x-1/2 items-center gap-2">
              <span className="rounded-full bg-black/40 px-2.5 py-1 text-[11px] text-white/85 backdrop-blur-sm">
                {index + 1} / {slides.length}
              </span>
              <div className="flex gap-1.5">
                {slides.map((_, i) => (
                  <button
                    key={i}
                    type="button"
                    aria-label={`Go to photo ${i + 1}`}
                    onClick={() => setIndex(i)}
                    className={cn(
                      "h-1.5 rounded-full transition-all",
                      i === index ? "w-5 bg-white" : "w-1.5 bg-white/45 hover:bg-white/70",
                    )}
                  />
                ))}
              </div>
            </div>
          </>
        ) : null}

        <button
          type="button"
          onClick={openLightbox}
          className="absolute right-3 top-3 z-10 inline-flex items-center gap-1.5 rounded-full border border-white/25 bg-black/35 px-3 py-1.5 text-xs text-white backdrop-blur-sm transition hover:bg-black/50 sm:right-5 sm:top-4"
        >
          <Expand className="size-3.5" />
          Full view
        </button>

        <div
          className={cn(
            "pointer-events-none relative mx-auto flex min-h-[52vh] max-w-6xl flex-col justify-end px-4 pt-20 sm:px-6",
            hasMany ? "pb-16" : "pb-10",
          )}
        >
          <div className="pointer-events-auto">{children}</div>
        </div>
      </section>

      {lightboxOpen ? (
        <div
          role="dialog"
          aria-modal="true"
          aria-label={`${destination} photos`}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/92"
          onClick={closeLightbox}
        >
          <button
            type="button"
            aria-label="Close full view"
            onClick={closeLightbox}
            className="absolute right-4 top-4 z-20 flex size-11 items-center justify-center rounded-full border border-white/20 bg-white/10 text-white transition hover:bg-white/20"
          >
            <X className="size-5" />
          </button>

          {hasMany ? (
            <>
              <button
                type="button"
                aria-label="Previous photo"
                onClick={(e) => {
                  e.stopPropagation()
                  go(-1)
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
                  go(1)
                }}
                className="absolute right-3 z-20 flex size-12 items-center justify-center rounded-full border border-white/20 bg-white/10 text-white transition hover:bg-white/20 sm:right-6"
              >
                <ChevronRight className="size-6" />
              </button>
            </>
          ) : null}

          <img
            src={current}
            alt={`${destination} photo ${index + 1}`}
            className="max-h-[88vh] max-w-[min(96vw,1200px)] object-contain shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          />

          <div className="absolute bottom-5 left-1/2 z-20 flex -translate-x-1/2 flex-col items-center gap-2">
            <p className="rounded-full bg-white/10 px-3 py-1 text-xs text-white/90 backdrop-blur-sm">
              {index + 1} / {slides.length}
              {hasMany ? ` · ${destination}` : ""}
            </p>
            {hasMany ? (
              <div className="flex gap-1.5">
                {slides.map((_, i) => (
                  <button
                    key={i}
                    type="button"
                    aria-label={`Go to photo ${i + 1}`}
                    onClick={(e) => {
                      e.stopPropagation()
                      setIndex(i)
                    }}
                    className={cn(
                      "h-1.5 rounded-full transition-all",
                      i === index ? "w-5 bg-white" : "w-1.5 bg-white/40 hover:bg-white/70",
                    )}
                  />
                ))}
              </div>
            ) : null}
          </div>
        </div>
      ) : null}
    </>
  )
}

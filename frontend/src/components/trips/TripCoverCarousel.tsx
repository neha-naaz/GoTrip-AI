import { useEffect, useState, type ReactNode } from "react"
import { resolveMediaUrl } from "@/api/client"
import { tripImageForDestination } from "@/lib/trip-display"
import { cn } from "@/lib/utils"

type TripCoverCarouselProps = {
  destination: string
  coverImageUrl?: string | null
  children: ReactNode
  className?: string
}

export function TripCoverCarousel({
  destination,
  coverImageUrl,
  children,
  className,
}: TripCoverCarouselProps) {
  const src = resolveMediaUrl(coverImageUrl) ?? tripImageForDestination(destination)

  return (
    <section className={cn("relative isolate min-h-[52vh] overflow-hidden", className)}>
      <img
        src={src}
        alt={`${destination} cover`}
        className="absolute inset-0 size-full object-cover"
      />
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/80 via-black/35 to-black/20" />
      <div className="pointer-events-none relative mx-auto flex min-h-[52vh] max-w-6xl flex-col justify-end px-4 pb-10 pt-20 sm:px-6">
        <div className="pointer-events-auto">{children}</div>
      </div>
    </section>
  )
}

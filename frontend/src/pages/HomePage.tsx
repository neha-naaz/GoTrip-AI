import { Link } from "react-router-dom"
import { ArrowRight, MapPinned, Users } from "lucide-react"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"

const HERO_IMAGE =
  "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=2400&q=80"

export function HomePage() {
  const { isAuthenticated } = useAuth()

  return (
    <div>
      <section className="relative isolate min-h-[78vh] overflow-hidden">
        <img
          src={HERO_IMAGE}
          alt="Travelers on an open road toward mountains"
          className="absolute inset-0 size-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/35 to-black/20" />

        <div className="relative mx-auto flex min-h-[78vh] max-w-6xl flex-col justify-end px-4 pb-16 pt-24 sm:px-6 sm:pb-20">
          <p className="mb-3 text-sm font-medium tracking-wide text-white/80">Group travel marketplace</p>
          <h1 className="max-w-3xl text-4xl font-semibold tracking-tight text-white sm:text-5xl md:text-6xl">
            Tripflow
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed text-white/85 sm:text-lg">
            Discover curated group trips from verified agencies. Book your seat, join your crew, and travel with
            people who want the same adventure.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button size="lg" className="h-11 rounded-2xl px-5" render={<Link to="/trips" />}>
              Explore trips
              <ArrowRight className="size-4" />
            </Button>
            {isAuthenticated ? (
              <Button
                size="lg"
                variant="outline"
                className="h-11 rounded-2xl border-white/30 bg-white/10 px-5 text-white hover:bg-white/20 hover:text-white"
                render={<Link to="/me" />}
              >
                Go to account
              </Button>
            ) : (
              <Button
                size="lg"
                variant="outline"
                className="h-11 rounded-2xl border-white/30 bg-white/10 px-5 text-white hover:bg-white/20 hover:text-white"
                render={<Link to="/register" />}
              >
                Create account
              </Button>
            )}
          </div>
        </div>
      </section>

      <section className="mx-auto grid max-w-6xl gap-6 px-4 py-16 sm:grid-cols-2 sm:px-6">
        <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
          <div className="mb-4 flex size-10 items-center justify-center rounded-2xl bg-accent text-accent-foreground">
            <MapPinned className="size-5" />
          </div>
          <h2 className="text-lg font-semibold tracking-tight">For travelers</h2>
          <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
            Browse published trips, reserve a seat, pay the booking amount, and unlock your trip group chat.
          </p>
        </div>
        <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
          <div className="mb-4 flex size-10 items-center justify-center rounded-2xl bg-accent text-accent-foreground">
            <Users className="size-5" />
          </div>
          <h2 className="text-lg font-semibold tracking-tight">For agencies</h2>
          <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
            Create draft itineraries, publish once verified, and fill capacity with travelers who are ready to go.
          </p>
        </div>
      </section>
    </div>
  )
}

import { Link } from "react-router-dom"
import { ArrowRight } from "lucide-react"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"

const HERO_IMAGE =
  "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=2400&q=80"

const TRAVELER_IMAGE =
  "https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=1600&q=80"

const AGENCY_IMAGE =
  "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1600&q=80"

const STEPS = [
  {
    n: "01",
    title: "Discover active trips",
    body: "Browse curated group adventures from verified agencies — destination, dates, and itinerary in one place.",
  },
  {
    n: "02",
    title: "Reserve and pay the booking amount",
    body: "Hold your seat with a deposit. When payment confirms, you’re in.",
  },
  {
    n: "03",
    title: "Unlock the trip group chat",
    body: "Meet your crew before you go. Coordinate, share plans, and travel as a group.",
  },
] as const

export function HomePage() {
  const { isAuthenticated, user } = useAuth()
  const isAgency = user?.role === "AGENCY"

  return (
    <div>
      {/* Hero */}
      <section className="relative isolate min-h-[88vh] overflow-hidden">
        <img
          src={HERO_IMAGE}
          alt="Travelers on an open road toward mountains"
          className="absolute inset-0 size-full object-cover -scale-x-100"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/40 to-black/25" />

        <div className="relative mx-auto flex min-h-[88vh] max-w-6xl flex-col justify-end px-4 pb-16 pt-28 sm:px-6 sm:pb-24">
          <p className="mb-2 animate-in fade-in slide-in-from-bottom-2 text-5xl font-semibold tracking-tight text-white duration-700 sm:text-6xl md:text-7xl">
            Tripflow
          </p>
          <h1 className="mt-3 max-w-2xl animate-in fade-in slide-in-from-bottom-3 text-2xl font-medium leading-snug tracking-tight text-white/95 duration-700 delay-100 fill-mode-both sm:text-3xl md:text-4xl">
            Group trips from verified agencies.<br></br>
            Crews that actually connect.
          </h1>
          <p className="mt-4 max-w-lg animate-in fade-in slide-in-from-bottom-3 text-base leading-relaxed text-white/80 duration-700 delay-200 fill-mode-both sm:text-lg">
            A marketplace where travelers book seats and agencies fill trips — then everyone lands in the same
            group chat.
          </p>
          <div className="mt-8 flex animate-in fade-in slide-in-from-bottom-3 flex-wrap gap-3 duration-700 delay-300 fill-mode-both">
            <Button size="lg" className="h-11 rounded-2xl px-5" render={<Link to="/trips" />}>
              Explore trips
              <ArrowRight className="size-4" />
            </Button>
            {isAuthenticated ? (
              <Button
                size="lg"
                variant="outline"
                className="h-11 rounded-2xl border-white/30 bg-white/10 px-5 text-white hover:bg-white/20 hover:text-white"
                render={<Link to={isAgency ? "/agency/trips" : "/me"} />}
              >
                {isAgency ? "My trips" : "Go to account"}
              </Button>
            ) : (
              <Button
                size="lg"
                variant="outline"
                className="h-11 rounded-2xl border-white/30 bg-white/10 px-5 text-white hover:bg-white/20 hover:text-white"
                render={<Link to="/register" />}
              >
                Get started
              </Button>
            )}
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="border-b border-border/80 bg-background">
        <div className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
          <p className="text-sm font-medium tracking-wide text-primary">How Tripflow works</p>
          <h2 className="mt-2 max-w-xl text-3xl font-semibold tracking-tight sm:text-4xl">
            One loop for the whole marketplace
          </h2>
          <p className="mt-3 max-w-2xl text-base leading-relaxed text-muted-foreground">
            Travelers and agencies meet on the same path — publish or join, pay to confirm, then collaborate in
            chat.
          </p>

          <ol className="mt-14 grid gap-12 sm:grid-cols-3 sm:gap-8">
            {STEPS.map((step) => (
              <li key={step.n} className="animate-in fade-in slide-in-from-bottom-2 duration-700 fill-mode-both">
                <span className="font-mono text-sm tracking-widest text-primary/80">{step.n}</span>
                <h3 className="mt-3 text-lg font-semibold tracking-tight">{step.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{step.body}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      {/* For travelers */}
      <section className="bg-background">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-4 py-20 sm:px-6 lg:grid-cols-2 lg:gap-16">
          <div className="relative aspect-[4/5] overflow-hidden sm:aspect-[5/4] lg:aspect-[4/5]">
            <img
              src={TRAVELER_IMAGE}
              alt="Friends exploring a coastal town together"
              className="absolute inset-0 size-full object-cover"
            />
          </div>
          <div>
            <p className="text-sm font-medium tracking-wide text-primary">For travelers</p>
            <h2 className="mt-2 text-3xl font-semibold tracking-tight sm:text-4xl">
              Find your trip. Meet your crew.
            </h2>
            <p className="mt-4 text-base leading-relaxed text-muted-foreground">
              Browse published trips, reserve a seat, pay the booking amount, and unlock the group chat with
              everyone going on the same adventure.
            </p>
            <div className="mt-8 space-y-4 border-t border-border pt-8">
              <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Coming to Tripflow</p>
              <p className="text-base leading-relaxed text-foreground/90">
                Travel. Capture. Share. Relive. Rate your agency, add your favorite moments to the trip gallery,
                and get some love from your fellow travelers.
              </p>
            </div>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button size="lg" className="h-11 rounded-2xl px-5" render={<Link to="/trips" />}>
                Explore trips
                <ArrowRight className="size-4" />
              </Button>
              {!isAuthenticated ? (
                <Button
                  size="lg"
                  variant="outline"
                  className="h-11 rounded-2xl px-5"
                  render={<Link to="/register" />}
                >
                  Create traveler account
                </Button>
              ) : null}
            </div>
          </div>
        </div>
      </section>

      {/* For agencies */}
      <section className="border-t border-border/80 bg-muted/40">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-4 py-20 sm:px-6 lg:grid-cols-2 lg:gap-16">
          <div className="order-2 lg:order-1">
            <p className="text-sm font-medium tracking-wide text-primary">For agencies</p>
            <h2 className="mt-2 text-3xl font-semibold tracking-tight sm:text-4xl">
              Publish once. Fill seats with less friction.
            </h2>
            <p className="mt-4 text-base leading-relaxed text-muted-foreground">
              Draft trips, shape day-by-day itineraries with inclusions and exclusions, publish when verified, and
              watch capacity fill with travelers who are ready to go.
            </p>
            <div className="mt-8 space-y-4 border-t border-border pt-8">
              <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Coming to Tripflow</p>
              <p className="text-base leading-relaxed text-foreground/90">
                Faster itinerary workflows and management tools that cut busywork — clearer traveler signals,
                simpler rostering, and less back-and-forth before departure.
              </p>
            </div>
            <div className="mt-8 flex flex-wrap gap-3">
              {isAgency ? (
                <Button size="lg" className="h-11 rounded-2xl px-5" render={<Link to="/agency/trips" />}>
                  My trips
                  <ArrowRight className="size-4" />
                </Button>
              ) : (
                <Button size="lg" className="h-11 rounded-2xl px-5" render={<Link to="/register" />}>
                  Register as agency
                  <ArrowRight className="size-4" />
                </Button>
              )}
              {!isAuthenticated ? (
                <Button
                  size="lg"
                  variant="outline"
                  className="h-11 rounded-2xl px-5"
                  render={<Link to="/login" />}
                >
                  Agency sign in
                </Button>
              ) : null}
            </div>
          </div>
          <div className="relative order-1 aspect-[4/5] overflow-hidden sm:aspect-[5/4] lg:order-2 lg:aspect-[4/5]">
            <img
              src={AGENCY_IMAGE}
              alt="Travel planner working with maps and itinerary notes"
              className="absolute inset-0 size-full object-cover"
            />
          </div>
        </div>
      </section>

      {/* Closing CTA */}
      <section className="border-t border-border bg-background">
        <div className="mx-auto flex max-w-6xl flex-col items-start justify-between gap-8 px-4 py-20 sm:flex-row sm:items-end sm:px-6">
          <div className="max-w-xl">
            <h2 className="text-3xl font-semibold tracking-tight sm:text-4xl">
              Start as a traveler or an agency
            </h2>
            <p className="mt-3 text-base leading-relaxed text-muted-foreground">
              Same marketplace. Different doors in. Pick the path that fits you.
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" className="h-11 rounded-2xl px-5" render={<Link to="/trips" />}>
              I want to travel
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="h-11 rounded-2xl px-5"
              render={<Link to={isAgency ? "/agency/trips" : "/register"} />}
            >
              I run trips
            </Button>
          </div>
        </div>
      </section>
    </div>
  )
}

import { Link } from "react-router-dom"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"

export function MePage() {
  const { user } = useAuth()

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <div className="mb-8">
        <h1 className="text-3xl font-semibold tracking-tight">Your account</h1>
        <p className="mt-2 text-muted-foreground">Signed in and ready for the marketplace flow.</p>
      </div>

      <Card className="rounded-2xl shadow-sm">
        <CardHeader>
          <CardTitle className="text-xl">Profile</CardTitle>
          <CardDescription>Details from `/api/users/me` after JWT auth.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded-2xl border border-border bg-muted/40 p-4">
              <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">Name</p>
              <p className="mt-1 font-medium">{user?.name ?? "—"}</p>
            </div>
            <div className="rounded-2xl border border-border bg-muted/40 p-4">
              <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">Email</p>
              <p className="mt-1 font-medium">{user?.email}</p>
            </div>
            <div className="rounded-2xl border border-border bg-muted/40 p-4">
              <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">Role</p>
              <p className="mt-1 font-medium">{user?.role}</p>
            </div>
            <div className="rounded-2xl border border-border bg-muted/40 p-4">
              <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">User ID</p>
              <p className="mt-1 font-medium">{user?.userId}</p>
            </div>
          </div>

          <div className="flex flex-wrap gap-3 pt-2">
            {user?.role === "CUSTOMER" ? (
              <Button className="rounded-2xl" render={<Link to="/bookings" />}>
                My bookings
              </Button>
            ) : null}
            {user?.role === "AGENCY" ? (
              <Button className="rounded-2xl" render={<Link to="/agency/trips" />}>
                My trips
              </Button>
            ) : null}
            <Button
              variant={user?.role === "CUSTOMER" || user?.role === "AGENCY" ? "outline" : "default"}
              className="rounded-2xl"
              render={<Link to="/trips" />}
            >
              Explore trips
            </Button>
            <Button variant="outline" className="rounded-2xl" render={<Link to="/" />}>
              Back to home
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

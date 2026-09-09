import { Link, NavLink, Outlet } from "react-router-dom"
import { Compass, LogOut } from "lucide-react"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"

export function AppShell() {
  const { isAuthenticated, user, logout } = useAuth()

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-40 border-b border-border/80 bg-background/90 backdrop-blur-sm">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
          <Link to="/" className="flex items-center gap-2.5">
            <span className="flex size-9 items-center justify-center rounded-2xl bg-primary text-primary-foreground">
              <Compass className="size-4.5" />
            </span>
            <span className="text-lg font-semibold tracking-tight">Tripflow</span>
          </Link>

          <nav className="flex items-center gap-2 sm:gap-3">
            {isAuthenticated ? (
              <>
                <NavLink
                  to="/me"
                  className={({ isActive }) =>
                    `hidden rounded-2xl px-3 py-2 text-sm transition-colors sm:inline-flex ${
                      isActive
                        ? "bg-accent text-accent-foreground"
                        : "text-muted-foreground hover:text-foreground"
                    }`
                  }
                >
                  {user?.name ?? user?.email}
                </NavLink>
                <Button variant="outline" size="sm" className="rounded-2xl" onClick={logout}>
                  <LogOut className="size-3.5" />
                  Sign out
                </Button>
              </>
            ) : (
              <>
                <Button variant="ghost" size="sm" className="rounded-2xl" render={<Link to="/login" />}>
                  Sign in
                </Button>
                <Button size="sm" className="rounded-2xl" render={<Link to="/register" />}>
                  Get started
                </Button>
              </>
            )}
          </nav>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  )
}

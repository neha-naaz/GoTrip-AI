import { useState, type FormEvent } from "react"
import { Link, Navigate, useNavigate } from "react-router-dom"
import { ApiError } from "@/api/client"
import { useAuth } from "@/auth/AuthContext"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

type Role = "CUSTOMER" | "AGENCY"

export function RegisterPage() {
  const { register, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [role, setRole] = useState<Role>("CUSTOMER")
  const [agencyName, setAgencyName] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/me" replace />
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await register({
        name: name.trim(),
        email: email.trim(),
        password,
        role,
        agencyName: role === "AGENCY" ? agencyName.trim() || name.trim() : undefined,
      })
      navigate("/me", { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Unable to create account")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-[calc(100vh-4rem)] max-w-6xl items-center justify-center px-4 py-12 sm:px-6">
      <Card className="w-full max-w-md rounded-2xl shadow-sm">
        <CardHeader className="gap-2">
          <CardTitle className="text-2xl font-semibold tracking-tight">Join Tripflow</CardTitle>
          <CardDescription>Create a traveler or agency account to get started.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={onSubmit}>
            <div className="space-y-2">
              <Label htmlFor="name">Full name</Label>
              <Input
                id="name"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="h-11 rounded-2xl"
                placeholder="Neha Sharma"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="h-11 rounded-2xl"
                placeholder="you@email.com"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                autoComplete="new-password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="h-11 rounded-2xl"
                placeholder="At least 8 characters"
              />
            </div>

            <div className="space-y-2">
              <Label>I am a</Label>
              <Select
                value={role}
                onValueChange={(value) => {
                  if (value === "CUSTOMER" || value === "AGENCY") {
                    setRole(value)
                  }
                }}
              >
                <SelectTrigger className="h-11 w-full rounded-2xl">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CUSTOMER">Traveler</SelectItem>
                  <SelectItem value="AGENCY">Travel agency</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {role === "AGENCY" ? (
              <div className="space-y-2">
                <Label htmlFor="agencyName">Agency name</Label>
                <Input
                  id="agencyName"
                  value={agencyName}
                  onChange={(e) => setAgencyName(e.target.value)}
                  className="h-11 rounded-2xl"
                  placeholder="Himalayan Trails"
                />
              </div>
            ) : null}

            {error ? (
              <p className="rounded-2xl bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>
            ) : null}

            <Button type="submit" className="h-11 w-full rounded-2xl" disabled={loading}>
              {loading ? "Creating account…" : "Create account"}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Already have an account?{" "}
            <Link to="/login" className="font-medium text-primary hover:underline">
              Sign in
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

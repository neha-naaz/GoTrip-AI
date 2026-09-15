import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import { AuthProvider } from "@/auth/AuthContext"
import { ProtectedRoute } from "@/auth/ProtectedRoute"
import { AppShell } from "@/components/layout/AppShell"
import { AgencyTripEditPage } from "@/pages/AgencyTripEditPage"
import { AgencyTripPreviewPage } from "@/pages/AgencyTripPreviewPage"
import { AgencyTripsPage } from "@/pages/AgencyTripsPage"
import { BookingsPage } from "@/pages/BookingsPage"
import { CreateTripPage } from "@/pages/CreateTripPage"
import { HomePage } from "@/pages/HomePage"
import { LoginPage } from "@/pages/LoginPage"
import { MePage } from "@/pages/MePage"
import { RegisterPage } from "@/pages/RegisterPage"
import { TripChatPage } from "@/pages/TripChatPage"
import { TripDetailPage } from "@/pages/TripDetailPage"
import { TripsPage } from "@/pages/TripsPage"

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppShell />}>
            <Route index element={<HomePage />} />
            <Route path="trips" element={<TripsPage />} />
            <Route path="trips/:tripId" element={<TripDetailPage />} />
            <Route
              path="trips/:tripId/chat"
              element={
                <ProtectedRoute>
                  <TripChatPage />
                </ProtectedRoute>
              }
            />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route
              path="me"
              element={
                <ProtectedRoute>
                  <MePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="bookings"
              element={
                <ProtectedRoute>
                  <BookingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="agency/trips"
              element={
                <ProtectedRoute>
                  <AgencyTripsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="agency/trips/new"
              element={
                <ProtectedRoute>
                  <CreateTripPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="agency/trips/:tripId/edit"
              element={
                <ProtectedRoute>
                  <AgencyTripEditPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="agency/trips/:tripId/preview"
              element={
                <ProtectedRoute>
                  <AgencyTripPreviewPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

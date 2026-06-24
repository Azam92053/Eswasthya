import React from 'react'
import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'

// Layout
import AppLayout from './components/layout/AppLayout'

// Public pages
import LoginPage    from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import NotFoundPage from './pages/NotFoundPage'

// User pages
import DashboardPage      from './pages/DashboardPage'
import HealthRecordsPage  from './pages/HealthRecordsPage'
import AlertsPage         from './pages/AlertsPage'
import ProfilePage        from './pages/ProfilePage'

// Admin pages
import AdminDashboardPage from './pages/AdminDashboardPage'
import AdminUsersPage     from './pages/AdminUsersPage'
import AdminRecordsPage   from './pages/AdminRecordsPage'

import { Spinner } from './components/ui'
import { ErrorBoundary } from './components/ErrorBoundary'

/* Guards */

/** Redirect to /login if unauthenticated */
function RequireAuth() {
  const { user, loading } = useAuth()
  if (loading) return (
    <div style={{
      minHeight: '100vh', display: 'flex',
      alignItems: 'center', justifyContent: 'center',
      background: 'var(--bg)',
    }}>
      <Spinner size={40} />
    </div>
  )
  return user ? <Outlet /> : <Navigate to="/login" replace />
}

/** Redirect to /dashboard if not ADMIN */
function RequireAdmin() {
  const { user, isAdmin } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return isAdmin ? <Outlet /> : <Navigate to="/dashboard" replace />
}

/** Redirect authenticated users away from login/register */
function PublicOnly() {
  const { user, loading } = useAuth()
  if (loading) return null
  return user ? <Navigate to="/dashboard" replace /> : <Outlet />
}

/* App */

function AppRoutes() {
  return (
    <Routes>
      {/* Public routes - redirect to dashboard if already logged in */}
      <Route element={<PublicOnly />}>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      {/* Protected routes — requires valid JWT */}
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          {/* Default redirect */}
          <Route index element={<Navigate to="/dashboard" replace />} />

          {/* User routes */}
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/health"    element={<HealthRecordsPage />} />
          <Route path="/alerts"    element={<AlertsPage />} />
          <Route path="/profile"   element={<ProfilePage />} />

          {/* Admin-only routes */}
          <Route element={<RequireAdmin />}>
            <Route path="/admin"          element={<AdminDashboardPage />} />
            <Route path="/admin/users"    element={<AdminUsersPage />} />
            <Route path="/admin/records"  element={<AdminRecordsPage />} />
          </Route>
        </Route>
      </Route>

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </ErrorBoundary>
  )
}

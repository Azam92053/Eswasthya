import React, { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'

export default function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>

      {/* Desktop sidebar */}
      <div style={{
        display: 'block',
        position: 'fixed',
        left: 0, top: 0,
        zIndex: 100,
      }} id="sidebar-desktop">
        <Sidebar />
      </div>

      {/* Mobile overlay backdrop */}
      {mobileOpen && (
        <div
          onClick={() => setMobileOpen(false)}
          style={{
            position: 'fixed', inset: 0, zIndex: 150,
            background: 'rgba(0,0,0,.6)', backdropFilter: 'blur(3px)',
          }}
          aria-hidden="true"
        />
      )}

      {/* Mobile slide-in sidebar */}
      {mobileOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, zIndex: 200 }}>
          <Sidebar mobile onClose={() => setMobileOpen(false)} />
        </div>
      )}

      {/* Main content */}
      <main style={{
        flex: 1,
        marginLeft: 240,
        minHeight: '100vh',
        background: 'var(--bg)',
        display: 'flex',
        flexDirection: 'column',
      }}>
        {/* Mobile topbar — hidden on desktop via CSS below */}
        <div id="topbar-mobile" className="on-dark" style={{
          position: 'sticky', top: 0, zIndex: 50,
          background: 'var(--slate-900)',
          padding: '14px 18px',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          boxShadow: '0 2px 12px rgba(0,0,0,.3)',
        }}>
          <button
            onClick={() => setMobileOpen(true)}
            style={{
              background: 'rgba(255,255,255,.08)', border: 'none', borderRadius: 8,
              cursor: 'pointer', width: 36, height: 36,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: '#fff', fontSize: 18,
            }}
            aria-label="Open navigation menu"
            aria-expanded={mobileOpen}
            aria-controls="sidebar-mobile"
          >
            <span aria-hidden="true">☰</span>
          </button>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ fontSize: 18 }}>🫀</span>
            <span style={{
              fontFamily: 'var(--font-display)', fontWeight: 600,
              fontSize: 16,
            }}>eSwasthya</span>
          </div>
          <div style={{ width: 36 }} />
        </div>

        {/* Page body */}
        <div style={{ padding: '32px 36px', maxWidth: 1200, width: '100%', flex: 1 }}>
          <Outlet />
        </div>
      </main>

      <style>{`
        /* Desktop: show sidebar, hide mobile topbar */
        @media (min-width: 769px) {
          #topbar-mobile  { display: none !important; }
          #sidebar-desktop { display: block !important; }
        }
        /* Mobile: hide sidebar, show topbar, remove margin */
        @media (max-width: 768px) {
          #sidebar-desktop { display: none !important; }
          #topbar-mobile   { display: flex !important; }
          main { margin-left: 0 !important; }
          main > div:last-child { padding: 16px !important; }
        }
      `}</style>
    </div>
  )
}

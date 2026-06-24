import React from 'react'
import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center',
      justifyContent: 'center', flexDirection: 'column',
      background: 'var(--bg)', textAlign: 'center', padding: '40px',
    }}>
      <div style={{ fontSize: 72, marginBottom: 16, opacity: .4 }}>◈</div>
      <h1 style={{
        fontFamily: 'var(--font-display)', fontSize: 64,
        fontWeight: 300, color: 'var(--primary)', lineHeight: 1,
      }}>404</h1>
      <p style={{ fontSize: 20, color: 'var(--text-muted)', margin: '12px 0 32px' }}>
        Page not found
      </p>
      <Link to="/dashboard" style={{
        padding: '11px 28px', background: 'var(--primary)',
        color: '#fff', borderRadius: 'var(--r-lg)',
        fontWeight: 600, fontSize: 14,
        boxShadow: '0 4px 16px rgba(0,109,119,.3)',
      }}>
        Go to Dashboard
      </Link>
    </div>
  )
}

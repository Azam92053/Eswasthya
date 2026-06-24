import React, { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: '◈', roles: ['STUDENT','EMPLOYEE','ADMIN'] },
  { to: '/health',    label: 'Health Records', icon: '♡', roles: ['STUDENT','EMPLOYEE'] },
  { to: '/alerts',    label: 'Alerts', icon: '◎', roles: ['STUDENT','EMPLOYEE','ADMIN'] },
  { to: '/profile',   label: 'Profile', icon: '◉', roles: ['STUDENT','EMPLOYEE','ADMIN'] },
  { type: 'divider', roles: ['ADMIN'] },
  { to: '/admin',         label: 'Admin Stats', icon: '⊞', roles: ['ADMIN'] },
  { to: '/admin/users',   label: 'Users', icon: '⊛', roles: ['ADMIN'] },
  { to: '/admin/records', label: 'All Records', icon: '⊟', roles: ['ADMIN'] },
]

export default function Sidebar({ mobile, onClose }) {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); navigate('/login') }

  const visible = navItems.filter(item => !item.roles || item.roles.includes(user?.role))

  return (
    <aside style={{
      width: 240, height: '100vh', position: 'fixed', left: 0, top: 0, zIndex: 100,
      background: 'var(--slate-900)',
      display: 'flex', flexDirection: 'column',
      borderRight: '1px solid rgba(255,255,255,.06)',
      ...(mobile && {
        position: 'fixed', top: 0, left: 0, width: '100%', maxWidth: 280,
        boxShadow: 'var(--shadow-xl)',
      }),
    }}>
      {/* Logo */}
      <div style={{
        padding: '24px 20px 20px',
        borderBottom: '1px solid rgba(255,255,255,.06)',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{
            width: 36, height: 36, borderRadius: 'var(--r-md)',
            background: 'linear-gradient(135deg, var(--teal-400), var(--teal-800))',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '18px',
          }}>🫀</div>
          <div className="on-dark">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '18px', fontWeight: 600, letterSpacing: '-.3px' }}>
              eSwasthya
            </div>
            <div style={{ fontSize: '10px', opacity: .85, textTransform: 'uppercase', letterSpacing: '1px' }}>
              Health Portal
            </div>
          </div>
        </div>
      </div>

      {/* User chip */}
      <div style={{ padding: '14px 20px', borderBottom: '1px solid rgba(255,255,255,.06)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{
            width: 34, height: 34, borderRadius: '50%',
            background: 'linear-gradient(135deg, var(--teal-600), var(--teal-900))',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: '#fff', fontSize: '13px', fontWeight: 700, flexShrink: 0,
          }}>
            {user?.name?.charAt(0)?.toUpperCase() || '?'}
          </div>
          <div style={{ overflow: 'hidden' }} className="on-dark">
            <div style={{ fontSize: '13px', fontWeight: 600, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {user?.name}
            </div>
            <div style={{ fontSize: '11px', opacity: .85, textTransform: 'uppercase', letterSpacing: '.6px' }}>
              {user?.role}
            </div>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav style={{ flex: 1, overflowY: 'auto', padding: '12px 10px' }}>
        {visible.map((item, i) => {
          if (item.type === 'divider') return (
            <div key={i} style={{
              height: 1, background: 'rgba(255,255,255,.08)',
              margin: '10px 10px', borderRadius: 1,
            }} />
          )
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/dashboard'}
              onClick={onClose}
                style={({ isActive }) => ({
                  display: 'flex', alignItems: 'center', gap: 10,
                  padding: '9px 12px', borderRadius: 'var(--r-md)',
                  marginBottom: 2,
                  color: isActive ? 'var(--text-on-dark)' : 'rgba(255,255,255,.6)',
                  background: isActive ? 'rgba(255,255,255,.06)' : 'transparent',
                  fontSize: '14px', fontWeight: isActive ? 600 : 400,
                  transition: 'all var(--t-fast)',
                  textDecoration: 'none',
                })}
              onMouseEnter={e => { if (!e.currentTarget.style.background.includes('.1')) e.currentTarget.style.background = 'rgba(255,255,255,.05)' }}
              onMouseLeave={e => { if (!e.currentTarget.style.background.includes('.1')) e.currentTarget.style.background = 'transparent' }}
            >
              <span style={{ fontSize: '16px', opacity: .8 }}>{item.icon}</span>
              {item.label}
            </NavLink>
          )
        })}
      </nav>

      {/* Logout */}
      <div style={{ padding: '12px 10px', borderTop: '1px solid rgba(255,255,255,.06)' }}>
        <button
          onClick={handleLogout}
          style={{
            width: '100%', display: 'flex', alignItems: 'center', gap: 10,
            padding: '9px 12px', borderRadius: 'var(--r-md)',
            background: 'transparent', border: 'none', cursor: 'pointer',
            color: 'rgba(255,255,255,.4)', fontSize: '14px',
            transition: 'all var(--t-fast)',
          }}
          onMouseEnter={e => { e.currentTarget.style.background = 'rgba(231,111,81,.15)'; e.currentTarget.style.color = 'var(--coral)' }}
          onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'rgba(255,255,255,.4)' }}
        >
          <span style={{ fontSize: '16px' }}>↩</span>
          Sign out
        </button>
      </div>
    </aside>
  )
}

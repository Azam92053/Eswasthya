import React, { useEffect, useState } from 'react'

/* Spinner */
export function Spinner({ size = 24, color = 'var(--primary)' }) {
  return (
    <div style={{
      width: size, height: size,
      border: `2px solid transparent`,
      borderTopColor: color,
      borderRadius: '50%',
      animation: 'spin .7s linear infinite',
      display: 'inline-block',
      flexShrink: 0,
    }} />
  )
}

/* Button */
export function Button({
  children, variant = 'primary', size = 'md',
  loading = false, icon, fullWidth, style = {}, ...props
}) {
  const base = {
    display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
    gap: '8px', border: 'none', borderRadius: 'var(--r-lg)',
    fontFamily: 'var(--font-body)', fontWeight: 600,
    cursor: loading || props.disabled ? 'not-allowed' : 'pointer',
    opacity: props.disabled ? 0.55 : 1,
    transition: 'all var(--t-base)',
    whiteSpace: 'nowrap',
    ...(fullWidth && { width: '100%' }),
  }
  const sizes = {
    sm: { padding: '7px 16px', fontSize: '13px' },
    md: { padding: '10px 22px', fontSize: '14px' },
    lg: { padding: '13px 28px', fontSize: '15px' },
    xl: { padding: '16px 36px', fontSize: '16px' },
  }
  const variants = {
    primary: {
      background: 'var(--primary)', color: '#fff',
      boxShadow: '0 2px 12px rgba(0,109,119,.30)',
    },
    secondary: {
      background: 'var(--teal-50)', color: 'var(--primary)',
      border: '1.5px solid var(--teal-200)',
    },
    ghost: {
      background: 'transparent', color: 'var(--primary)',
    },
    danger: {
      background: 'var(--danger)', color: '#fff',
      boxShadow: '0 2px 12px rgba(230,57,70,.25)',
    },
    outline: {
      background: 'transparent', color: 'var(--text)',
      border: '1.5px solid var(--border)',
    },
  }
  return (
    <button
      style={{ ...base, ...sizes[size], ...variants[variant], ...style }}
      onMouseEnter={e => {
        if (!props.disabled && !loading) {
          e.currentTarget.style.filter = 'brightness(1.07)'
          e.currentTarget.style.transform = 'translateY(-1px)'
        }
      }}
      onMouseLeave={e => {
        e.currentTarget.style.filter = ''
        e.currentTarget.style.transform = ''
      }}
      {...props}
    >
      {loading ? <Spinner size={15} color={variant === 'primary' || variant === 'danger' ? '#fff' : 'var(--primary)'} /> : icon}
      {children}
    </button>
  )
}

/* Input */
export function Input({ label, error, icon, style: styleProp, ...props }) {
  const baseStyle = {
    width: '100%', padding: icon ? '10px 14px 10px 38px' : '10px 14px',
    border: `1.5px solid ${error ? 'var(--danger)' : 'var(--border)'}`,
    borderRadius: 'var(--r-md)', fontSize: '14px',
    background: 'var(--surface)', color: 'var(--text)',
    transition: 'border-color var(--t-fast)', outline: 'none',
  }
  const mergedStyle = { ...baseStyle, ...(styleProp || {}) }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      {label && (
        <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-muted)' }}>
          {label}
        </label>
      )}
      <div style={{ position: 'relative' }}>
        {icon && (
          <span style={{
            position: 'absolute', left: '12px', top: '50%',
            transform: 'translateY(-50%)', color: 'var(--text-light)',
            pointerEvents: 'none', display: 'flex',
          }}>
            {icon}
          </span>
        )}
        <input
          style={mergedStyle}
          onFocus={e => e.target.style.borderColor = 'var(--primary)'}
          onBlur={e => e.target.style.borderColor = error ? 'var(--danger)' : 'var(--border)'}
          {...props}
        />
      </div>
      {error && <span style={{ fontSize: '12px', color: 'var(--danger)' }}>{error}</span>}
    </div>
  )
}

/* Select */
export function Select({ label, error, children, style: styleProp, ...props }) {
  const baseStyle = {
    width: '100%', padding: '10px 14px',
    border: `1.5px solid ${error ? 'var(--danger)' : 'var(--border)'}`,
    borderRadius: 'var(--r-md)', fontSize: '14px',
    background: 'var(--surface)', color: 'var(--text)',
    cursor: 'pointer', outline: 'none', transition: 'border-color var(--t-fast)',
  }
  const mergedStyle = { ...baseStyle, ...(styleProp || {}) }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      {label && (
        <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-muted)' }}>
          {label}
        </label>
      )}
      <select
        style={mergedStyle}
        onFocus={e => e.target.style.borderColor = 'var(--primary)'}
        onBlur={e => e.target.style.borderColor = error ? 'var(--danger)' : 'var(--border)'}
        {...props}
      >
        {children}
      </select>
      {error && <span style={{ fontSize: '12px', color: 'var(--danger)' }}>{error}</span>}
    </div>
  )
}

/* Textarea */
export function Textarea({ label, error, ...props }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      {label && (
        <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-muted)' }}>
          {label}
        </label>
      )}
      <textarea
        style={{
          width: '100%', padding: '10px 14px',
          border: `1.5px solid ${error ? 'var(--danger)' : 'var(--border)'}`,
          borderRadius: 'var(--r-md)', fontSize: '14px',
          background: 'var(--surface)', color: 'var(--text)',
          minHeight: 90, resize: 'vertical', outline: 'none',
          transition: 'border-color var(--t-fast)',
        }}
        onFocus={e => e.target.style.borderColor = 'var(--primary)'}
        onBlur={e => e.target.style.borderColor = error ? 'var(--danger)' : 'var(--border)'}
        {...props}
      />
      {error && <span style={{ fontSize: '12px', color: 'var(--danger)' }}>{error}</span>}
    </div>
  )
}

/* Modal */
export function Modal({ open, onClose, title, children, width = 520 }) {
  useEffect(() => {
    const handler = e => { if (e.key === 'Escape') onClose() }
    if (open) document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }, [open, onClose])

  if (!open) return null
  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed', inset: 0, zIndex: 1000,
        background: 'rgba(15,25,35,.5)', backdropFilter: 'blur(4px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: '20px', animation: 'fadeIn .2s ease',
      }}
    >
      <div
        onClick={e => e.stopPropagation()}
        style={{
          background: 'var(--surface)', borderRadius: 'var(--r-2xl)',
          width: '100%', maxWidth: width, maxHeight: '90vh', overflowY: 'auto',
          boxShadow: 'var(--shadow-xl)',
          animation: 'scaleIn .25s ease',
        }}
      >
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '20px 24px', borderBottom: '1px solid var(--border)',
        }}>
          <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '20px', fontWeight: 600 }}>
            {title}
          </h3>
          <button
            onClick={onClose}
            style={{
              background: 'var(--slate-100)', border: 'none', borderRadius: '8px',
              width: 32, height: 32, cursor: 'pointer', fontSize: '18px',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: 'var(--text-muted)', transition: 'all var(--t-fast)',
            }}
            onMouseEnter={e => { e.currentTarget.style.background = 'var(--slate-200)' }}
            onMouseLeave={e => { e.currentTarget.style.background = 'var(--slate-100)' }}
          >×</button>
        </div>
        <div style={{ padding: '24px' }}>{children}</div>
      </div>
    </div>
  )
}

/* Toast */
const toastColors = {
  success: { bg: '#d8f5ed', color: '#1d7a5f', border: '#b0e8d2' },
  error:   { bg: '#fde8ea', color: '#b02030', border: '#f8b4ba' },
  info:    { bg: '#ddf4f7', color: '#006d77', border: '#a8e6ea' },
  warning: { bg: '#fef6dd', color: '#b07c1a', border: '#f5dfaa' },
}
export function Toast({ message, type = 'info', onDismiss }) {
  useEffect(() => {
    const t = setTimeout(onDismiss, 4000)
    return () => clearTimeout(t)
  }, [onDismiss])

  const c = toastColors[type]
  return (
    <div
      style={{
        position: 'fixed', bottom: '24px', right: '24px', zIndex: 9999,
        background: c.bg, color: c.color, border: `1.5px solid ${c.border}`,
        borderRadius: 'var(--r-lg)', padding: '12px 20px',
        boxShadow: 'var(--shadow-lg)', maxWidth: 380,
        animation: 'slideLeft .3s ease', fontSize: '14px', fontWeight: 500,
        display: 'flex', gap: '10px', alignItems: 'center',
      }}
    >
      <span style={{ flex: 1 }}>{message}</span>
      <button
        onClick={onDismiss}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', fontSize: '16px' }}
      >×</button>
    </div>
  )
}

/* useToast hook */
export function useToast() {
  const [toasts, setToasts] = useState([])
  const show = (message, type = 'info') => {
    const id = Date.now()
    setToasts(t => [...t, { id, message, type }])
  }
  const dismiss = (id) => setToasts(t => t.filter(x => x.id !== id))

  const ToastContainer = () => (
    <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '10px' }}>
      {toasts.map(t => (
        <Toast key={t.id} message={t.message} type={t.type} onDismiss={() => dismiss(t.id)} />
      ))}
    </div>
  )

  return { show, ToastContainer }
}

/* StatCard */
export function StatCard({ label, value, sub, icon, color = 'var(--primary)', trend }) {
  return (
    <div className="card animate-fadeUp" style={{ padding: '22px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <p style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '.8px', marginBottom: 8 }}>
            {label}
          </p>
          <p style={{ fontFamily: 'var(--font-display)', fontSize: '32px', fontWeight: 600, color: 'var(--text)', lineHeight: 1 }}>
            {value ?? '—'}
          </p>
          {sub && <p style={{ fontSize: '12px', color: 'var(--text-light)', marginTop: 6 }}>{sub}</p>}
        </div>
        {icon && (
          <div style={{
            width: 44, height: 44, borderRadius: 'var(--r-lg)',
            background: `${color}18`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color, fontSize: '20px',
          }}>
            {icon}
          </div>
        )}
      </div>
    </div>
  )
}

/* PageHeader */
export function PageHeader({ title, subtitle, action }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 28, flexWrap: 'wrap', gap: 12 }}>
      <div>
        <h1 style={{ fontFamily: 'var(--font-display)', fontSize: '28px', fontWeight: 600, color: 'var(--text)' }}>
          {title}
        </h1>
        {subtitle && <p style={{ color: 'var(--text-muted)', marginTop: 4, fontSize: '14px' }}>{subtitle}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  )
}

/* EmptyState */
export function EmptyState({ icon, title, description, action }) {
  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      justifyContent: 'center', padding: '60px 24px', textAlign: 'center',
    }}>
      <div style={{ fontSize: '52px', marginBottom: 16, opacity: .6 }}>{icon}</div>
      <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '20px', marginBottom: 8 }}>{title}</h3>
      <p style={{ color: 'var(--text-muted)', fontSize: '14px', maxWidth: 320, marginBottom: 24 }}>{description}</p>
      {action}
    </div>
  )
}

/* BmiGauge */
export function BmiGauge({ value }) {
  if (!value) return null
  const pct = Math.min(Math.max(((value - 10) / 40) * 100, 0), 100)
  const color = value < 18.5 ? '#3b82f6' : value < 25 ? 'var(--green)' : value < 30 ? 'var(--amber)' : 'var(--coral)'
  const label = value < 18.5 ? 'Underweight' : value < 25 ? 'Normal' : value < 30 ? 'Overweight' : 'Obese'
  return (
    <div style={{ width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6, fontSize: '13px' }}>
        <span style={{ fontWeight: 700, color, fontSize: '22px', fontFamily: 'var(--font-display)' }}>
          {value.toFixed(1)}
        </span>
        <span className="badge" style={{ background: `${color}20`, color }}>{label}</span>
      </div>
      <div style={{ height: 8, borderRadius: 99, background: 'var(--slate-100)', overflow: 'hidden' }}>
        <div style={{ height: '100%', width: `${pct}%`, background: color, borderRadius: 99, transition: 'width 1s ease' }} />
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, fontSize: '10px', color: 'var(--text-light)' }}>
        <span>10</span><span>18.5</span><span>25</span><span>30</span><span>50+</span>
      </div>
    </div>
  )
}

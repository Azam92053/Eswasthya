import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/ui'
import styles from './LoginPage.module.css'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate  = useNavigate()
  const { show, ToastContainer } = useToast()
  const [form, setForm]       = useState({ username: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [errors, setErrors]   = useState({})

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async e => {
    e.preventDefault()
    const errs = {}
    if (!form.username) errs.username = 'Username required'
    if (!form.password) errs.password = 'Password required'
    if (Object.keys(errs).length) { setErrors(errs); return }
    setErrors({})
    setLoading(true)
    try {
      const user = await login(form)
      navigate(user.role === 'ADMIN' ? '/admin' : '/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message || 'Invalid credentials'
      show(msg, 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`${styles.loginContainer} on-dark`}>
      <ToastContainer />

      {/* Left panel - brand */}
      <div className={`${styles.leftPanel} hide-mobile`}>
        {/* Decorative ring */}
        <div className={styles.ringLarge} />
        <div className={styles.ringSmall} />

        <div className={styles.brandSection}>
          <div className={styles.brandRow}>
            <div className={styles.brandIcon}>🫀</div>
            <div>
              <div className={styles.brandName}>eSwasthya</div>
              <div className={styles.brandTagline}>Health Portal</div>
            </div>
          </div>

          <h2 className={styles.headline}>
            Your health,<br />
            <em className={styles.headlineEm}>tracked thoughtfully.</em>
          </h2>

          <p className={styles.description}>
            Monitor BMI, blood pressure, glucose, and activity levels. 
            Get instant alerts for abnormal readings. 
            Stay informed about your wellness journey.
          </p>

          <div className={styles.features}>
            {[
              { icon: '◈', label: 'Health Tracking' },
              { icon: '◎', label: 'Smart Alerts' },
              { icon: '⊞', label: 'Trend Analysis' },
            ].map(f => (
              <div key={f.label} className={styles.featureItem}>
                <div className={styles.featureIcon}>{f.icon}</div>
                <div className={styles.featureLabel}>{f.label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right panel - form */}
      <div className={styles.rightPanel}>
        <div className={styles.formContainer}>
          <h1 className={styles.formTitle}>Sign in</h1>
          <p className={styles.formSubtitle}>Welcome back. Enter your credentials below.</p>

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.inputGroup}>
              <label htmlFor="username" className={styles.label}>
                Username
              </label>
              <input
                id="username"
                type="text"
                placeholder="your_username"
                value={form.username}
                onChange={e => set('username', e.target.value)}
                autoComplete="username"
                className={`${styles.input} ${errors.username ? styles.inputError : ''}`}
              />
              {errors.username && <p className={styles.errorText}>{errors.username}</p>}
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor="password" className={styles.label}>
                Password
              </label>
              <input
                id="password"
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={e => set('password', e.target.value)}
                autoComplete="current-password"
                className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
              />
              {errors.password && <p className={styles.errorText}>{errors.password}</p>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className={styles.submitButton}
            >
              {loading ? (
                <>
                  <div className={styles.spinner} />
                  Signing in…
                </>
              ) : (
                <>
                  Sign in
                  <span aria-hidden="true">→</span>
                </>
              )}
            </button>
          </form>

          <p className={styles.footer}>
            New to eSwasthya?{' '}
            <Link to="/register" className={styles.link}>
              Create an account
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

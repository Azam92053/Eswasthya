import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button, Input, Select, useToast } from '../components/ui'
import styles from './RegisterPage.module.css'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const { show, ToastContainer } = useToast()

  const [form, setForm] = useState({
    username: '', email: '', password: '', confirmPassword: '',
    name: '', age: '', gender: '', role: 'STUDENT',
  })
  const [errors, setErrors]   = useState({})
  const [loading, setLoading] = useState(false)

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const validate = () => {
    const e = {}
    if (!form.username || form.username.length < 3) e.username = 'Min 3 characters, letters/numbers/_'
    if (!/^[a-zA-Z0-9_]+$/.test(form.username)) e.username = 'Only letters, numbers, underscores'
    if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) e.email = 'Valid email required'
    if (!form.password || form.password.length < 8) e.password = 'Minimum 8 characters'
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match'
    if (!form.name) e.name = 'Full name required'
    if (form.age && (form.age < 1 || form.age > 120)) e.age = 'Age must be 1–120'
    if (!form.role) e.role = 'Role required'
    setErrors(e)
    return !Object.keys(e).length
  }

  const handleSubmit = async e => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
        name: form.name,
        ...(form.age    && { age: parseInt(form.age) }),
        ...(form.gender && { gender: form.gender }),
        role: form.role,
      })
      show('Account created! Please sign in.', 'success')
      setTimeout(() => navigate('/login'), 1400)
    } catch (err) {
      const details = err.response?.data?.data
      if (details && typeof details === 'object') {
        setErrors(prev => ({ ...prev, ...details }))
        const firstError = Object.values(details).find(Boolean)
        show(firstError || err.response?.data?.message || 'Registration failed', 'error')
      } else {
        const msg = err.response?.data?.message || 'Registration failed'
        show(msg, 'error')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`${styles.registerContainer} on-dark`}>
      <ToastContainer />
      <div className={styles.formCard}>
        {/* Header */}
        <div className={styles.header}>
          <div className={styles.headerBrand}>
            <span style={{ fontSize: 28 }} aria-hidden="true">🫀</span>
            <span className={styles.headerTitle}>eSwasthya</span>
          </div>
          <h1 className={styles.headerHeading}>Create your account</h1>
          <p className={styles.headerSubtitle}>Start tracking your health journey today</p>
        </div>

        <form onSubmit={handleSubmit} className={styles.form} noValidate>
          <div className={styles.gridTwoCol}>
            <Input 
              label="Username *" 
              placeholder="john_doe" 
              value={form.username} 
              onChange={e => set('username', e.target.value)} 
              error={errors.username}
              autoComplete="username"
            />
            <Input 
              label="Full Name *" 
              placeholder="John Doe" 
              value={form.name} 
              onChange={e => set('name', e.target.value)} 
              error={errors.name}
              autoComplete="name"
            />
          </div>

          <Input 
            label="Email *" 
            type="email" 
            placeholder="john@example.com" 
            value={form.email} 
            onChange={e => set('email', e.target.value)} 
            error={errors.email}
            autoComplete="email"
          />

          <div className={styles.gridTwoCol}>
            <Input 
              label="Password *" 
              type="password" 
              placeholder="Min 8 chars" 
              value={form.password} 
              onChange={e => set('password', e.target.value)} 
              error={errors.password}
              autoComplete="new-password"
            />
            <Input 
              label="Confirm Password *" 
              type="password" 
              placeholder="Repeat password" 
              value={form.confirmPassword} 
              onChange={e => set('confirmPassword', e.target.value)} 
              error={errors.confirmPassword}
              autoComplete="new-password"
            />
          </div>

          <div className={styles.gridThreeCol}>
            <Input 
              label="Age" 
              type="number" 
              placeholder="25" 
              value={form.age} 
              onChange={e => set('age', e.target.value)} 
              error={errors.age}
              min="1"
              max="120"
            />
            <Select label="Gender" value={form.gender} onChange={e => set('gender', e.target.value)}>
              <option value="">—</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </Select>
            <Select label="Role *" value={form.role} onChange={e => set('role', e.target.value)} error={errors.role}>
              <option value="STUDENT">Student</option>
              <option value="EMPLOYEE">Employee</option>
              <option value="ADMIN">Admin</option>
            </Select>
          </div>

          <Button type="submit" variant="primary" size="lg" fullWidth loading={loading}>
            Create Account
          </Button>

          <p className={styles.footer}>
            Already have an account?{' '}
            <Link to="/login" className={styles.link}>
              Sign in
            </Link>
          </p>
        </form>
      </div>
    </div>
  )
}

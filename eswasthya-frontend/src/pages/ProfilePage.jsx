import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { userAPI } from '../api'
import { PageHeader, Button, Input, Select, useToast } from '../components/ui'
import styles from './ProfilePage.module.css'

export default function ProfilePage() {
  const { user, login } = useAuth()
  const { show, ToastContainer } = useToast()

  const [form, setForm]       = useState({ name:'', email:'', age:'', gender:'' })
  const [editing, setEditing] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors]   = useState({})

  useEffect(() => {
    if (user) {
      setForm({
        name:   user.name || '',
        email:  user.email || '',
        age:    user.age || '',
        gender: user.gender || '',
      })
    }
  }, [user])

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleSave = async () => {
    const e = {}
    if (!form.name) e.name = 'Name required'
    if (form.email && !/\S+@\S+\.\S+/.test(form.email)) e.email = 'Valid email required'
    if (form.age && (form.age < 1 || form.age > 120)) e.age = 'Age must be 1–120'
    setErrors(e)
    if (Object.keys(e).length) return

    setLoading(true)
    try {
      await userAPI.updateProfile({
        name: form.name,
        email: form.email,
        ...(form.age && { age: parseInt(form.age) }),
        ...(form.gender && { gender: form.gender }),
      })
      show('Profile updated successfully!', 'success')
      setEditing(false)
    } catch (err) {
      console.error('Profile update error:', err)
      show(err.response?.data?.message || 'Update failed', 'error')
    } finally {
      setLoading(false)
    }
  }

  const roleColors = { STUDENT:'#006d77', EMPLOYEE:'#7c3aed', ADMIN:'#dc2626' }
  const color = roleColors[user?.role] || 'var(--primary)'

  return (
    <div className={styles.container}>
      <ToastContainer />
      <div className={styles.pageHeader}>
        <PageHeader
          title="My Profile"
          subtitle="Manage your account information"
          action={
            !editing
              ? <Button variant="secondary" onClick={() => setEditing(true)}>Edit Profile</Button>
              : <div style={{ display:'flex', gap:10 }}>
                  <Button variant="outline" onClick={() => setEditing(false)}>Cancel</Button>
                  <Button variant="primary" loading={loading} onClick={handleSave}>Save Changes</Button>
                </div>
          }
        />
      </div>

      {/* Avatar card */}
      <div className={`card animate-fadeUp ${styles.profileCard}`}>
        <div className={styles.profileHeader}>
          <div
            className={styles.avatar}
            style={{
              background: `linear-gradient(135deg, ${color}cc, ${color})`,
              boxShadow: `0 6px 20px ${color}40`,
            }}
            aria-hidden="true"
          >
            {user?.name?.charAt(0)?.toUpperCase()}
          </div>
          <div className={styles.profileInfo}>
            <h2 className={styles.profileName}>{user?.name}</h2>
            <p className={styles.profileEmail}>@{user?.username}</p>
            <span className="badge" style={{ marginTop:8, background:`${color}18`, color }}>
              {user?.role}
            </span>
          </div>
        </div>
      </div>

      {/* Info grid */}
      <div className={`card animate-fadeUp ${styles.profileCard}`} style={{ animationDelay:'60ms' }}>
        <h3 className={styles.sectionTitle}>Personal Information</h3>

        {editing ? (
          <div className={styles.formGrid}>
            <div className={styles.formGroup}>
              <Input label="Full Name *" value={form.name} onChange={e => set('name', e.target.value)} error={errors.name} />
            </div>
            <div className={styles.formGroup}>
              <Input label="Email" type="email" value={form.email} onChange={e => set('email', e.target.value)} error={errors.email} />
            </div>
            <div className={styles.formGroup}>
              <Input label="Age" type="number" value={form.age} onChange={e => set('age', e.target.value)} error={errors.age} />
            </div>
            <div className={styles.formGroup}>
              <Select label="Gender" value={form.gender} onChange={e => set('gender', e.target.value)}>
                <option value="">— Not specified —</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </Select>
            </div>
          </div>
        ) : (
          <div className={styles.formGrid}>
            {[
              { label:'Full Name',  value: user?.name },
              { label:'Username',   value: `@${user?.username}` },
              { label:'Email',      value: user?.email },
              { label:'Age',        value: user?.age ? `${user.age} years` : '—' },
              { label:'Gender',     value: user?.gender || '—' },
              { label:'Role',       value: user?.role },
              { label:'Member since', value: user?.createdAt ? new Date(user.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'long', year:'numeric' }) : '—' },
            ].map(({ label, value }) => (
              <div 
                key={label} 
                className={styles.infoItem}
                style={{ gridColumn: label === 'Member since' ? 'span 2' : 'span 1' }}
              >
                <p className={styles.infoLabel}>{label}</p>
                <p className={styles.infoValue}>{value}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

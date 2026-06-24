import React, { useEffect, useState } from 'react'
import { alertAPI } from '../api'
import { PageHeader, Button, EmptyState, useToast, Spinner } from '../components/ui'
import styles from './AlertsPage.module.css'

const ALERT_META = {
  BMI_UNDERWEIGHT:    { icon:'⚖️', color:'#3b82f6', label:'BMI – Underweight' },
  BMI_OVERWEIGHT:     { icon:'⚖️', color:'#f59e0b', label:'BMI – Overweight' },
  BMI_OBESE:          { icon:'⚖️', color:'#dc2626', label:'BMI – Obese' },
  HIGH_BP_STAGE1:     { icon:'❤️', color:'#f97316', label:'BP – High Stage 1' },
  HIGH_BP_STAGE2:     { icon:'❤️', color:'#dc2626', label:'BP – High Stage 2' },
  LOW_BP:             { icon:'❤️', color:'#6366f1', label:'BP – Low' },
  LOW_GLUCOSE:        { icon:'🩸', color:'#6366f1', label:'Glucose – Low' },
  GLUCOSE_PREDIABETIC:{ icon:'🩸', color:'#f59e0b', label:'Glucose – Pre-diabetic' },
  GLUCOSE_DIABETIC:   { icon:'🩸', color:'#dc2626', label:'Glucose – Diabetic' },
  SEDENTARY_LIFESTYLE:{ icon:'🏃', color:'#64748b', label:'Activity – Sedentary' },
}

function AlertCard({ alert, onMarkRead }) {
  const meta = ALERT_META[alert.alertType] || { icon:'⚠️', color:'var(--text-muted)', label: alert.alertType }
  return (
    <div
      className={`animate-fadeUp ${styles.alertCard} ${!alert.isRead ? styles.alertCardUnread : ''}`}
    >
      {/* Icon */}
      <div
        className={styles.alertIcon}
        style={{ background: `${meta.color}18`, color: meta.color }}
      >
        {meta.icon}
        {!alert.isRead && (
          <div className={styles.alertUnreadDot} style={{ background: meta.color }} />
        )}
      </div>

      {/* Content */}
      <div className={styles.alertContent}>
        <div className={styles.alertMeta}>
          <span className={`badge ${styles.alertBadge}`} style={{ background:`${meta.color}18`, color:meta.color, fontSize:11 }}>
            {meta.label}
          </span>
          {!alert.isRead && (
            <span className={`badge badge-info ${styles.alertBadgeNew}`}>NEW</span>
          )}
        </div>
        <p className={`${styles.alertMessage} ${!alert.isRead ? styles.alertMessageUnread : ''}`}>
          {alert.message}
        </p>
        <p className={styles.alertDate}>
          {new Date(alert.createdAt).toLocaleDateString('en-IN', {
            weekday:'short', day:'numeric', month:'long', year:'numeric',
          })}
        </p>
      </div>

      {/* Action */}
      {!alert.isRead && (
        <button
          onClick={() => onMarkRead(alert.id)}
          className={styles.markReadButton}
          style={{ borderColor: `${meta.color}40`, color: meta.color }}
          aria-label={`Mark alert as read: ${alert.message}`}
        >
          Mark read
        </button>
      )}
    </div>
  )
}

export default function AlertsPage() {
  const [alerts, setAlerts]       = useState([])
  const [loading, setLoading]     = useState(true)
  const [filter, setFilter]       = useState('all')    // 'all' | 'unread'
  const [marking, setMarking]     = useState(false)
  const { show, ToastContainer }  = useToast()

  const load = async () => {
    setLoading(true)
    try {
      const res = await alertAPI.getAll()
      setAlerts(res.data.data || [])
    } catch { show('Failed to load alerts', 'error') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const markRead = async (id) => {
    try {
      await alertAPI.markRead(id)
      setAlerts(prev => prev.map(a => a.id === id ? { ...a, isRead: true } : a))
    } catch (err) {
      console.error('Failed to mark alert as read:', err)
      show('Failed to mark alert', 'error')
    }
  }

  const markAllRead = async () => {
    setMarking(true)
    try {
      await alertAPI.markAllRead()
      setAlerts(prev => prev.map(a => ({ ...a, isRead: true })))
      show('All alerts marked as read', 'success')
    } catch { show('Failed', 'error') }
    finally { setMarking(false) }
  }

  const filtered   = filter === 'unread' ? alerts.filter(a => !a.isRead) : alerts
  const unreadCount = alerts.filter(a => !a.isRead).length

  return (
    <div className={styles.container}>
      <ToastContainer />
      <div className={styles.pageHeader}>
        <PageHeader
          title="Health Alerts"
          subtitle={`${unreadCount} unread · ${alerts.length} total`}
          action={
            unreadCount > 0 && (
              <Button variant="secondary" loading={marking} onClick={markAllRead}>
                Mark all as read
              </Button>
            )
          }
        />
      </div>

      {/* Filter tabs */}
      <div className={styles.filterTabs} role="tablist" aria-label="Alert filters">
        {[
          { key:'all', label:`All (${alerts.length})` },
          { key:'unread', label:`Unread (${unreadCount})` },
        ].map(tab => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`${styles.filterTab} ${filter === tab.key ? styles.filterTabActive : ''}`}
            role="tab"
            aria-selected={filter === tab.key}
            aria-controls="alerts-panel"
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className={styles.loadingContainer}>
          <Spinner size={32} />
        </div>
      ) : filtered.length === 0 ? (
        <div className={`card ${styles.emptyState}`}>
          <EmptyState
            icon="✅"
            title={filter === 'unread' ? 'All caught up!' : 'No alerts yet'}
            description={filter === 'unread'
              ? 'You have no unread health alerts.'
              : 'Alerts appear automatically when abnormal health metrics are recorded.'}
          />
        </div>
      ) : (
        <div className="card" style={{ overflow:'hidden' }} id="alerts-panel" role="tabpanel">
          {filtered.map((alert, i) => (
            <AlertCard
              key={alert.id}
              alert={alert}
              onMarkRead={markRead}
            />
          ))}
        </div>
      )}
    </div>
  )
}

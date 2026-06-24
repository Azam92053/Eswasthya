import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { healthAPI, alertAPI } from '../api'
import { StatCard, PageHeader, BmiGauge, Spinner, Button, EmptyState } from '../components/ui'
import { BmiTrendChart, BloodPressureChart, GlucoseChart, ActivityBarChart } from '../components/charts/HealthCharts'
import HealthRecordForm from '../components/HealthRecordForm'
import styles from './DashboardPage.module.css'

function MetricCard({ title, value, category, categoryColor, unit, description }) {
  return (
    <div className={`card ${styles.metricCard}`}>
      <p className={styles.metricLabel}>{title}</p>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, marginBottom: 6 }}>
        <span style={{ fontFamily: 'var(--font-display)', fontSize: 30, fontWeight: 600 }}>
          {value ?? '—'}
        </span>
        {value && unit && <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{unit}</span>}
      </div>
      {category && (
        <span className="badge" style={{ background: `${categoryColor}20`, color: categoryColor }}>
          {category}
        </span>
      )}
      {!value && <p style={{ fontSize: 13, color: 'var(--text-light)' }}>Not recorded yet</p>}
    </div>
  )
}

function AlertItem({ alert }) {
  const colors = {
    BMI_UNDERWEIGHT: '#3b82f6', BMI_OVERWEIGHT: '#f59e0b', BMI_OBESE: '#ef4444',
    HIGH_BP_STAGE1: '#f97316', HIGH_BP_STAGE2: '#dc2626', LOW_BP: '#6366f1',
    LOW_GLUCOSE: '#8b5cf6', GLUCOSE_PREDIABETIC: '#f59e0b', GLUCOSE_DIABETIC: '#dc2626',
    SEDENTARY_LIFESTYLE: '#64748b',
  }
  const color = colors[alert.alertType] || 'var(--text-muted)'
  return (
    <div className={styles.alertItem}>
      <div
        className={`${styles.alertDot} ${!alert.isRead ? styles.alertDotUnread : ''}`}
        style={{ background: color, color }}
        aria-label={alert.isRead ? 'Read alert' : 'Unread alert'}
      />
      <div className={styles.alertContent}>
        <p className={`${styles.alertMessage} ${!alert.isRead ? styles.alertMessageUnread : ''}`}>
          {alert.message}
        </p>
        <p className={styles.alertDate}>
          {new Date(alert.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' })}
        </p>
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [summary, setSummary]     = useState(null)
  const [records, setRecords]     = useState([])
  const [alerts, setAlerts]       = useState([])
  const [loading, setLoading]     = useState(true)
  const [formOpen, setFormOpen]   = useState(false)

  const loadData = async () => {
    setLoading(true)
    try {
      const [dashRes, recordsRes, alertsRes] = await Promise.all([
        healthAPI.dashboard(),
        healthAPI.getAll(),
        alertAPI.getUnread(),
      ])
      setSummary(dashRes.data.data)
      setRecords(recordsRes.data.data || [])
      setAlerts((alertsRes.data.data || []).slice(0, 5))
    } catch (err) {
      console.error('Failed to load dashboard data:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [])

  if (loading) return (
    <div className={styles.loadingContainer}>
      <Spinner size={36} />
    </div>
  )

  const bpColor = s => {
    if (!s) return 'var(--text-muted)'
    if (s < 90)  return '#6366f1'
    if (s < 120) return 'var(--green)'
    if (s < 130) return '#f59e0b'
    if (s < 140) return '#f97316'
    return '#dc2626'
  }
  const glColor = g => {
    if (!g) return 'var(--text-muted)'
    if (g < 70)  return '#6366f1'
    if (g < 100) return 'var(--green)'
    if (g < 126) return '#f59e0b'
    return '#dc2626'
  }
  const bmiColor = b => {
    if (!b) return 'var(--text-muted)'
    if (b < 18.5) return '#3b82f6'
    if (b < 25)   return 'var(--green)'
    if (b < 30)   return '#f59e0b'
    return '#dc2626'
  }

  return (
    <div className={styles.container}>
      <HealthRecordForm open={formOpen} onClose={() => setFormOpen(false)} onSaved={loadData} />

      <div className={styles.pageHeader}>
        <PageHeader
          title={`Good ${new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'}, ${user?.name?.split(' ')[0]} 👋`}
          subtitle="Here's your health overview"
          action={
            <Button variant="primary" onClick={() => setFormOpen(true)} icon="＋">
              Log Today's Metrics
            </Button>
          }
        />
      </div>

      {/* Stats row */}
      <div className={styles.statsGrid}>
        <StatCard label="Total Records" value={summary?.totalRecords ?? 0} icon="📋" color="var(--primary)" />
        <StatCard label="Unread Alerts" value={summary?.unreadAlertCount ?? 0} icon="🔔" color="var(--coral)" sub="Tap Alerts to view" />
        <StatCard label="Avg BMI (30d)" value={summary?.avgBmi30Days?.toFixed(1) ?? '—'} icon="⚖️" color="var(--green)" />
        <StatCard label="Avg Glucose (30d)" value={summary?.avgGlucose30Days?.toFixed(0) ?? '—'} icon="🩸" color="var(--amber)" sub="mg/dL" />
      </div>

      <div className={styles.mainGrid}>
        {/* Latest snapshot */}
        <div>
          <h2 className={styles.sectionTitle}>Latest Snapshot</h2>
          <div className={`stagger ${styles.metricsGrid}`}>
            <MetricCard title="BMI" value={summary?.latestBmi?.toFixed(1)} category={summary?.latestBmiCategory} categoryColor={bmiColor(summary?.latestBmi)} />
            <MetricCard title="Blood Pressure" value={summary?.latestBloodPressure} unit="mmHg" category={summary?.latestBpCategory} categoryColor={bpColor(summary?.recentRecords?.[0]?.systolicBp)} />
            <MetricCard title="Glucose (fasting)" value={summary?.latestGlucose?.toFixed(0)} unit="mg/dL" category={summary?.latestGlucoseCategory} categoryColor={glColor(summary?.latestGlucose)} />
            <MetricCard title="Activity Level" value={summary?.latestActivityLevel} />
          </div>

          {summary?.latestBmi && (
            <div className={`card ${styles.bmiSection}`}>
              <p className={styles.bmiTitle}>BMI Scale</p>
              <BmiGauge value={summary.latestBmi} />
            </div>
          )}
        </div>

        {/* Alerts panel */}
        <div>
          <div className={styles.alertsHeader}>
            <h2 className={styles.sectionTitle}>Recent Alerts</h2>
            <button 
              onClick={() => navigate('/alerts')} 
              className={styles.viewAllButton}
              aria-label="View all alerts"
            >
              View all →
            </button>
          </div>
          <div className={`card ${styles.alertsCard}`}>
            {alerts.length === 0 ? (
              <div className={styles.noAlerts}>
                <div className={styles.noAlertsIcon}>✓</div>
                <p>No unread alerts</p>
              </div>
            ) : (
              alerts.map(a => <AlertItem key={a.id} alert={a} />)
            )}
          </div>
        </div>
      </div>

      {/* Charts */}
      {records.length > 0 ? (
        <div className={styles.chartsSection}>
          <h2 className={styles.chartsTitle}>Health Trends</h2>
          <div className={styles.chartsGrid}>
            {[
              { title:'BMI Over Time', chart:<BmiTrendChart data={records} /> },
              { title:'Blood Pressure Trend', chart:<BloodPressureChart data={records} /> },
              { title:'Glucose Trend (mg/dL)', chart:<GlucoseChart data={records} /> },
              { title:'Activity Distribution', chart:<ActivityBarChart data={records} /> },
            ].map(({ title, chart }) => (
              <div key={title} className={`card animate-fadeUp ${styles.chartCard}`}>
                <p className={styles.chartTitle}>{title}</p>
                {chart}
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className={`card ${styles.emptyState}`}>
          <EmptyState
            icon="📊"
            title="No health records yet"
            description="Log your first health metrics to start seeing trend charts and get personalized health insights."
            action={<Button variant="primary" onClick={() => setFormOpen(true)}>Log Your First Record</Button>}
          />
        </div>
      )}
    </div>
  )
}

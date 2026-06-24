
import React, { useEffect, useState } from 'react'
import { healthAPI } from '../api'
import { PageHeader, Button, EmptyState, Modal, useToast, Spinner } from '../components/ui'
import HealthRecordForm from '../components/HealthRecordForm'
import styles from './HealthRecordsPage.module.css'

const BADGE_COLORS = {
  // BMI
  'Normal weight':               '#2a9d8f',
  'Underweight':                 '#3b82f6',
  'Overweight':                  '#f59e0b',
  'Obese':                       '#dc2626',
  // Glucose
  'Normal (fasting)':            '#2a9d8f',
  'Pre-diabetic':                '#f59e0b',
  'Diabetic range':              '#dc2626',
  'Low (Hypoglycaemia)':         '#6366f1',
  // Blood Pressure (Normal defined once — also covers BMI Normal weight via 'Normal weight' key above)
  'Normal':                      '#2a9d8f',
  'Elevated':                    '#f59e0b',
  'High Stage 1':                '#f97316',
  'High Stage 2 (Hypertension)': '#dc2626',
  'Low (Hypotension)':           '#6366f1',
}

function Badge({ text }) {
  if (!text) return <span style={{ color: 'var(--text-light)', fontSize: 12 }}>—</span>
  const color = BADGE_COLORS[text] || 'var(--text-muted)'
  return (
    <span className={`badge ${styles.badge}`} style={{ background: `${color}18`, color, fontSize: 11 }}>
      {text}
    </span>
  )
}

export default function HealthRecordsPage() {
  const [records, setRecords]     = useState([])
  const [loading, setLoading]     = useState(true)
  const [formOpen, setFormOpen]   = useState(false)
  const [editing, setEditing]     = useState(null)
  const [deleteId, setDeleteId]   = useState(null)
  const [deleting, setDeleting]   = useState(false)
  const { show, ToastContainer }  = useToast()

  const load = async () => {
    setLoading(true)
    try {
      const res = await healthAPI.getAll()
      setRecords(res.data.data || [])
    } catch { show('Failed to load records', 'error') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await healthAPI.delete(deleteId)
      show('Record deleted', 'success')
      setDeleteId(null)
      load()
    } catch { show('Failed to delete', 'error') }
    finally { setDeleting(false) }
  }

  const handleDownload = async () => {
    try {
      const res = await healthAPI.report()
      const blob = new Blob([res.data], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `eswasthya-report-${new Date().toISOString().slice(0,10)}.txt`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Report download failed:', err)
      show('Failed to download report', 'error')
    }
  }

  return (
    <div className={styles.container}>
      <ToastContainer />
      <HealthRecordForm
        open={formOpen || !!editing}
        onClose={() => { setFormOpen(false); setEditing(null) }}
        onSaved={() => { setFormOpen(false); setEditing(null); load(); show('Record saved!', 'success') }}
        existing={editing}
      />
      <Modal open={!!deleteId} onClose={() => setDeleteId(null)} title="Delete Record" width={400}>
        <p style={{ color:'var(--text-muted)', marginBottom:24 }}>
          Are you sure you want to delete this health record? This will also delete any associated alerts. This action cannot be undone.
        </p>
        <div style={{ display:'flex', gap:10, justifyContent:'flex-end' }}>
          <Button variant="outline" onClick={() => setDeleteId(null)}>Cancel</Button>
          <Button variant="danger" loading={deleting} onClick={handleDelete}>Delete</Button>
        </div>
      </Modal>

      <div className={styles.pageHeader}>
        <PageHeader
          title="Health Records"
          subtitle={`${records.length} record${records.length !== 1 ? 's' : ''} logged`}
          action={
            <div className={styles.headerActions}>
              <Button variant="secondary" onClick={handleDownload} icon="↓">
                Download Report
              </Button>
              <Button variant="primary" onClick={() => setFormOpen(true)} icon="＋">
                Log New Record
              </Button>
            </div>
          }
        />
      </div>

      {loading ? (
        <div className={styles.loadingContainer}>
          <Spinner size={32} />
        </div>
      ) : records.length === 0 ? (
        <div className={`card ${styles.emptyState}`}>
          <EmptyState
            icon="📋"
            title="No health records yet"
            description="Start tracking your health metrics to build your history."
            action={<Button variant="primary" onClick={() => setFormOpen(true)}>Log Your First Record</Button>}
          />
        </div>
      ) : (
        <div className={`card ${styles.tableCard}`}>
          <table className={styles.table}>
            <thead className={styles.tableHead}>
              <tr>
                {['Date','BMI','Blood Pressure','Glucose (mg/dL)','Activity','Notes',''].map(h => (
                  <th key={h} className={styles.tableHeader}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {records.map((r, i) => (
                <tr 
                  key={r.id}
                  className={`animate-fadeUp ${styles.tableRow}`}
                  style={{ animationDelay: `${i * 30}ms` }}
                >
                  <td className={`${styles.tableCell} ${styles.tableCellDate}`}>
                    {new Date(r.recordDate).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' })}
                  </td>
                  <td className={styles.tableCell}>
                    {r.bmi ? (
                      <div>
                        <span className={styles.tableCellMetric}>{r.bmi?.toFixed(1)}</span>
                        <div style={{ marginTop:3 }}><Badge text={r.bmiCategory} /></div>
                      </div>
                    ) : <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>}
                  </td>
                  <td className={styles.tableCell}>
                    {r.bloodPressure ? (
                      <div>
                        <span className={styles.tableCellMetric}>
                          {r.bloodPressure}
                          <span className={styles.tableCellUnit}>mmHg</span>
                        </span>
                        <div style={{ marginTop:3 }}><Badge text={r.bpCategory} /></div>
                      </div>
                    ) : <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>}
                  </td>
                  <td className={styles.tableCell}>
                    {r.glucose ? (
                      <div>
                        <span className={styles.tableCellMetric}>{r.glucose?.toFixed(0)}</span>
                        <div style={{ marginTop:3 }}><Badge text={r.glucoseCategory} /></div>
                      </div>
                    ) : <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>}
                  </td>
                  <td className={styles.tableCell}>
                    {r.activityLevel ? (
                      <span className={`badge ${styles.badgeInfo}`} style={{ textTransform:'capitalize' }}>
                        {r.activityLevel?.toLowerCase()}
                      </span>
                    ) : <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>}
                  </td>
                  <td className={styles.tableCell}>
                    <span className={styles.tableCellNotes}>
                      {r.notes || '—'}
                    </span>
                  </td>
                  <td className={styles.tableCell}>
                    <div className={styles.tableCellActions}>
                      <button
                        onClick={() => setEditing(r)}
                        className={styles.actionButton}
                        aria-label={`Edit record from ${new Date(r.recordDate).toLocaleDateString()}`}
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => setDeleteId(r.id)}
                        className={styles.actionButtonDanger}
                        aria-label={`Delete record from ${new Date(r.recordDate).toLocaleDateString()}`}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

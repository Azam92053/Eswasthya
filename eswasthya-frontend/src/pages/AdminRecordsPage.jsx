import React, { useEffect, useState } from 'react'
import { adminAPI } from '../api'
import { PageHeader, Spinner, EmptyState } from '../components/ui'

const BADGE_COLORS = {
  'Normal weight': '#2a9d8f', 'Normal': '#2a9d8f',
  'Underweight': '#3b82f6',
  'Overweight': '#f59e0b',
  'Obese': '#dc2626',
  'Normal (fasting)': '#2a9d8f',
  'Pre-diabetic': '#f59e0b',
  'Diabetic range': '#dc2626',
  'Low (Hypoglycaemia)': '#6366f1',
  'Elevated': '#f59e0b',
  'High Stage 1': '#f97316',
  'High Stage 2 (Hypertension)': '#dc2626',
  'Low (Hypotension)': '#6366f1',
}

function MetricBadge({ value, unit, category }) {
  if (!value && value !== 0)
    return <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>
  const color = BADGE_COLORS[category] || 'var(--text-muted)'
  return (
    <div>
      <span style={{ fontWeight:600, fontSize:14 }}>
        {typeof value === 'number' && !Number.isInteger(value) ? value.toFixed(1) : value}
        {unit && <span style={{ fontSize:11, fontWeight:400, color:'var(--text-muted)' }}> {unit}</span>}
      </span>
      {category && (
        <div style={{ marginTop:3 }}>
          <span className="badge" style={{ background:`${color}18`, color, fontSize:10 }}>{category}</span>
        </div>
      )}
    </div>
  )
}

export default function AdminRecordsPage() {
  const [records, setRecords]   = useState([])
  const [loading, setLoading]   = useState(true)
  const [search, setSearch]     = useState('')
  const [sort, setSort]         = useState('date_desc')

  useEffect(() => {
    adminAPI.getAllRecords()
      .then(r => setRecords(r.data.data || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const sorted = [...records].sort((a, b) => {
    if (sort === 'date_desc') return new Date(b.recordDate) - new Date(a.recordDate)
    if (sort === 'date_asc')  return new Date(a.recordDate) - new Date(b.recordDate)
    if (sort === 'bmi_desc')  return (b.bmi ?? 0) - (a.bmi ?? 0)
    if (sort === 'user')      return a.userName.localeCompare(b.userName)
    return 0
  })

  const filtered = sorted.filter(r =>
    !search || r.userName?.toLowerCase().includes(search.toLowerCase()) ||
    r.recordDate?.includes(search)
  )

  return (
    <div className="animate-fadeIn">
      <PageHeader
        title="All Health Records"
        subtitle={`${records.length} records across all users`}
      />

      {/* Toolbar */}
      <div style={{ display:'flex', gap:12, marginBottom:20, flexWrap:'wrap' }}>
        <div style={{ position:'relative', flex:1, minWidth:220 }}>
          <span style={{ position:'absolute', left:12, top:'50%', transform:'translateY(-50%)', color:'var(--text-light)', fontSize:15 }}>🔍</span>
          <input
            placeholder="Search by user name or date…"
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{
              width:'100%', padding:'9px 14px 9px 36px',
              border:'1.5px solid var(--border)', borderRadius:'var(--r-md)',
              background:'var(--surface)', fontSize:14, outline:'none',
            }}
            onFocus={e => e.target.style.borderColor = 'var(--primary)'}
            onBlur={e => e.target.style.borderColor = 'var(--border)'}
          />
        </div>

        <select
          value={sort}
          onChange={e => setSort(e.target.value)}
          style={{
            padding:'9px 14px', border:'1.5px solid var(--border)',
            borderRadius:'var(--r-md)', fontSize:13, background:'var(--surface)',
            cursor:'pointer', outline:'none', color:'var(--text)',
          }}
        >
          <option value="date_desc">Newest first</option>
          <option value="date_asc">Oldest first</option>
          <option value="bmi_desc">Highest BMI first</option>
          <option value="user">Sort by user</option>
        </select>
      </div>

      {loading ? (
        <div style={{ display:'flex', justifyContent:'center', padding:80 }}>
          <Spinner size={32} />
        </div>
      ) : filtered.length === 0 ? (
        <div className="card">
          <EmptyState icon="📋" title="No records found" description="No health records have been logged yet." />
        </div>
      ) : (
        <div className="card" style={{ overflowX:'auto' }}>
          <table style={{ width:'100%', borderCollapse:'collapse' }}>
            <thead>
              <tr style={{ borderBottom:'2px solid var(--border)' }}>
                {['Date','User','BMI','Blood Pressure','Glucose','Activity','Notes'].map(h => (
                  <th key={h} style={{
                    padding:'12px 18px', textAlign:'left', fontSize:11,
                    fontWeight:700, color:'var(--text-muted)',
                    textTransform:'uppercase', letterSpacing:.7, whiteSpace:'nowrap',
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((r, i) => (
                <tr
                  key={r.id}
                  className="animate-fadeUp"
                  style={{ borderBottom:'1px solid var(--border)', animationDelay:`${i * 20}ms` }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--teal-50)'}
                  onMouseLeave={e => e.currentTarget.style.background = ''}
                >
                  <td style={{ padding:'13px 18px', fontWeight:600, fontSize:14, whiteSpace:'nowrap' }}>
                    {new Date(r.recordDate).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' })}
                  </td>

                  <td style={{ padding:'13px 18px' }}>
                    <div style={{ display:'flex', alignItems:'center', gap:8 }}>
                      <div style={{
                        width:28, height:28, borderRadius:'50%', flexShrink:0,
                        background:'linear-gradient(135deg, var(--teal-400), var(--teal-800))',
                        display:'flex', alignItems:'center', justifyContent:'center',
                        color:'#fff', fontSize:11, fontWeight:700,
                      }}>
                        {r.userName?.charAt(0)?.toUpperCase()}
                      </div>
                      <span style={{ fontWeight:500, fontSize:13 }}>{r.userName}</span>
                    </div>
                  </td>

                  <td style={{ padding:'13px 18px' }}>
                    <MetricBadge value={r.bmi} category={r.bmiCategory} />
                  </td>

                  <td style={{ padding:'13px 18px' }}>
                    <MetricBadge value={r.bloodPressure} unit="mmHg" category={r.bpCategory} />
                  </td>

                  <td style={{ padding:'13px 18px' }}>
                    <MetricBadge value={r.glucose ? Math.round(r.glucose) : null} unit="mg/dL" category={r.glucoseCategory} />
                  </td>

                  <td style={{ padding:'13px 18px' }}>
                    {r.activityLevel ? (
                      <span className="badge" style={{
                        background: r.activityLevel === 'HIGH' ? '#d8f5ed' : r.activityLevel === 'MODERATE' ? '#fef6dd' : '#fde8ea',
                        color: r.activityLevel === 'HIGH' ? '#1d7a5f' : r.activityLevel === 'MODERATE' ? '#b07c1a' : '#b02030',
                        fontSize:11, textTransform:'capitalize',
                      }}>
                        {r.activityLevel.toLowerCase()}
                      </span>
                    ) : <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>}
                  </td>

                  <td style={{ padding:'13px 18px', maxWidth:160 }}>
                    <span style={{ fontSize:12, color:'var(--text-muted)', display:'block', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
                      {r.notes || '—'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ padding:'12px 20px', borderTop:'1px solid var(--border)', fontSize:12, color:'var(--text-muted)', textAlign:'right' }}>
            Showing {filtered.length} of {records.length} records
          </div>
        </div>
      )}
    </div>
  )
}

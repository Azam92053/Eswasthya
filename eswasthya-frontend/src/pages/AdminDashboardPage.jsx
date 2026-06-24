import React, { useEffect, useState } from 'react'
import { adminAPI } from '../api'
import { PageHeader, StatCard, Spinner, EmptyState } from '../components/ui'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'

const PIE_COLORS = ['#006d77','#e76f51','#e9c46a','#2a9d8f','#4a90e2','#7c3aed']

function AlertsDistributionChart({ data }) {
  if (!data || !Object.keys(data).length) return null
  const chartData = Object.entries(data).map(([k, v]) => ({
    name: k.replace(/_/g, ' ').replace(/BMI|BP|GLUCOSE/g, m => m),
    value: v,
  }))
  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie data={chartData} cx="50%" cy="50%" outerRadius={100} dataKey="value" label={({ name, percent }) => `${(percent*100).toFixed(0)}%`} labelLine={false}>
          {chartData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
        </Pie>
        <Tooltip formatter={(v, n) => [v, n]} />
        <Legend wrapperStyle={{ fontSize: 11 }} />
      </PieChart>
    </ResponsiveContainer>
  )
}

function UserRoleChart({ data }) {
  if (!data) return null
  const chartData = Object.entries(data).map(([role, count]) => ({ role, count }))
  return (
    <ResponsiveContainer width="100%" height={180}>
      <BarChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
        <XAxis dataKey="role" tick={{ fontSize: 12 }} axisLine={false} tickLine={false} />
        <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} width={25} />
        <Tooltip cursor={{ fill:'rgba(0,109,119,.06)' }} />
        <Bar dataKey="count" fill="var(--primary)" radius={[6,6,0,0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    adminAPI.getStats()
      .then(r => setStats(r.data.data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return (
    <div style={{ display:'flex', justifyContent:'center', padding:80 }}>
      <Spinner size={36} />
    </div>
  )

  return (
    <div className="animate-fadeIn">
      <PageHeader
        title="Admin Dashboard"
        subtitle="Platform-wide health statistics and overview"
      />

      {/* Top stats */}
      <div className="stagger" style={{ display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(180px,1fr))', gap:16, marginBottom:28 }}>
        <StatCard label="Total Users"    value={stats?.totalUsers}         icon="👥" color="var(--primary)" />
        <StatCard label="Health Records" value={stats?.totalHealthRecords} icon="📋" color="var(--green)" />
        <StatCard label="Total Alerts"   value={stats?.totalAlerts}        icon="🔔" color="var(--coral)" />
        <StatCard label="Unread Alerts"  value={stats?.unreadAlerts}       icon="⚠️" color="var(--amber)" />
        <StatCard label="Avg BMI"    value={stats?.averageBmi?.toFixed(1) ?? '—'}    icon="⚖️" color="var(--primary)" sub="Platform average" />
        <StatCard label="Avg Glucose" value={stats?.averageGlucose?.toFixed(0) ?? '—'} icon="🩸" color="var(--amber)" sub="mg/dL" />
      </div>

      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:24, marginBottom:24 }}>
        {/* Users by role */}
        <div className="card" style={{ padding:'22px 24px' }}>
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:17, fontWeight:600, marginBottom:18 }}>Users by Role</h3>
          <UserRoleChart data={stats?.usersByRole} />
        </div>

        {/* Alert type distribution */}
        <div className="card" style={{ padding:'22px 24px' }}>
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:17, fontWeight:600, marginBottom:18 }}>Alert Distribution</h3>
          {stats?.alertsByType && Object.keys(stats.alertsByType).length > 0
            ? <AlertsDistributionChart data={stats.alertsByType} />
            : <EmptyState icon="📊" title="No alerts yet" description="" />
          }
        </div>
      </div>

      {/* User summaries table */}
      <div className="card" style={{ overflowX:'auto' }}>
        <div style={{ padding:'20px 24px 16px', borderBottom:'1px solid var(--border)' }}>
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:17, fontWeight:600 }}>User Health Overview</h3>
        </div>
        <table style={{ width:'100%', borderCollapse:'collapse' }}>
          <thead>
            <tr style={{ borderBottom:'2px solid var(--border)' }}>
              {['Name','Role','Records','Latest BMI','BMI Status','Unread Alerts'].map(h => (
                <th key={h} style={{ padding:'12px 20px', textAlign:'left', fontSize:11, fontWeight:700, color:'var(--text-muted)', textTransform:'uppercase', letterSpacing:.7 }}>
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {(stats?.userSummaries || []).map((u, i) => {
              const bmiColor = !u.latestBmi ? 'var(--text-light)'
                : u.latestBmi < 18.5 ? '#3b82f6' : u.latestBmi < 25 ? 'var(--green)'
                : u.latestBmi < 30 ? '#f59e0b' : '#dc2626'
              return (
                <tr key={u.userId}
                  className="animate-fadeUp"
                  style={{ borderBottom:'1px solid var(--border)', animationDelay:`${i*30}ms` }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--teal-50)'}
                  onMouseLeave={e => e.currentTarget.style.background = ''}
                >
                  <td style={{ padding:'14px 20px', fontWeight:600 }}>{u.name}</td>
                  <td style={{ padding:'14px 20px' }}>
                    <span className="badge badge-info" style={{ textTransform:'capitalize', fontSize:11 }}>
                      {u.role?.toLowerCase()}
                    </span>
                  </td>
                  <td style={{ padding:'14px 20px', fontFamily:'var(--font-display)', fontSize:18, fontWeight:600 }}>{u.recordCount}</td>
                  <td style={{ padding:'14px 20px', fontWeight:600, color:bmiColor }}>
                    {u.latestBmi?.toFixed(1) ?? '—'}
                  </td>
                  <td style={{ padding:'14px 20px' }}>
                    {u.latestBmiCategory ? (
                      <span className="badge" style={{ background:`${bmiColor}18`, color:bmiColor, fontSize:11 }}>
                        {u.latestBmiCategory}
                      </span>
                    ) : <span style={{ color:'var(--text-light)', fontSize:12 }}>—</span>}
                  </td>
                  <td style={{ padding:'14px 20px' }}>
                    {u.unreadAlertCount > 0 ? (
                      <span className="badge badge-danger">{u.unreadAlertCount}</span>
                    ) : (
                      <span style={{ color:'var(--text-light)', fontSize:12 }}>0</span>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}

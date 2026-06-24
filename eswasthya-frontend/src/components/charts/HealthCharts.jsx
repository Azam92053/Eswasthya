import React from 'react'
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, ReferenceLine, Legend, AreaChart, Area,
} from 'recharts'

const TEAL   = '#006d77'
const CORAL  = '#e76f51'
const AMBER  = '#e9c46a'
const GREEN  = '#2a9d8f'
const BLUE   = '#4a90e2'

const CustomTooltip = ({ active, payload, label, unit }) => {
  if (!active || !payload?.length) return null
  return (
    <div style={{
      background: 'var(--slate-900)', borderRadius: 10,
      padding: '10px 14px', boxShadow: 'var(--shadow-lg)',
    }} className="on-dark">
      <p style={{ color: 'rgba(255,255,255,.5)', fontSize: 11, marginBottom: 6 }}>{label}</p>
      {payload.map((p, i) => (
        <div key={i} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: p.color }} />
          <span style={{ color: 'var(--text-on-dark)', fontWeight: 600, fontSize: 14 }}>
            {p.value} {unit || p.name}
          </span>
        </div>
      ))}
    </div>
  )
}

/* BMI Trend */
export function BmiTrendChart({ data }) {
  if (!data?.length) return <EmptyChart label="No BMI data yet" />
  const chartData = data
    .filter(r => r.bmi)
    .slice(0, 20).reverse()
    .map(r => ({ date: r.recordDate, bmi: r.bmi }))

  return (
    <ResponsiveContainer width="100%" height={200}>
      <AreaChart data={chartData} margin={{ top: 5, right: 10, bottom: 5, left: 0 }}>
        <defs>
          <linearGradient id="bmiGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%"  stopColor={TEAL} stopOpacity={.2} />
            <stop offset="95%" stopColor={TEAL} stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
        <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} />
        <YAxis domain={[10, 45]} tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} width={32} />
        <Tooltip content={<CustomTooltip unit="" />} />
        <ReferenceLine y={18.5} stroke={BLUE}  strokeDasharray="4 2" strokeWidth={1.5} />
        <ReferenceLine y={25}   stroke={GREEN} strokeDasharray="4 2" strokeWidth={1.5} />
        <ReferenceLine y={30}   stroke={CORAL} strokeDasharray="4 2" strokeWidth={1.5} />
        <Area type="monotone" dataKey="bmi" stroke={TEAL} strokeWidth={2.5} fill="url(#bmiGrad)" dot={{ r: 4, fill: TEAL, strokeWidth: 0 }} activeDot={{ r: 6 }} />
      </AreaChart>
    </ResponsiveContainer>
  )
}

/* Blood Pressure Trend */
export function BloodPressureChart({ data }) {
  if (!data?.length) return <EmptyChart label="No BP data yet" />
  const chartData = data
    .filter(r => r.systolicBp)
    .slice(0, 20).reverse()
    .map(r => ({ date: r.recordDate, systolic: r.systolicBp, diastolic: r.diastolicBp }))

  return (
    <ResponsiveContainer width="100%" height={200}>
      <LineChart data={chartData} margin={{ top: 5, right: 10, bottom: 5, left: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
        <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} />
        <YAxis domain={[50, 180]} tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} width={32} />
        <Tooltip content={props => (
          <div style={{ background: 'var(--slate-900)', borderRadius: 10, padding: '10px 14px', boxShadow: 'var(--shadow-lg)' }}>
            <p style={{ color: 'rgba(255,255,255,.5)', fontSize: 11, marginBottom: 6 }}>{props.label}</p>
            {props.payload?.map((p, i) => (
              <div key={i} style={{ color: p.color, fontWeight: 600, fontSize: 13 }}>{p.name}: {p.value} mmHg</div>
            ))}
          </div>
        )} />
        <Legend wrapperStyle={{ fontSize: 11 }} />
        <ReferenceLine y={120} stroke={AMBER}  strokeDasharray="4 2" strokeWidth={1} />
        <ReferenceLine y={140} stroke={CORAL} strokeDasharray="4 2" strokeWidth={1} />
        <Line type="monotone" dataKey="systolic" stroke={CORAL} strokeWidth={2.5} dot={{ r: 3, fill: CORAL }} name="Systolic" />
        <Line type="monotone" dataKey="diastolic" stroke={BLUE} strokeWidth={2.5} dot={{ r: 3, fill: BLUE }} name="Diastolic" />
      </LineChart>
    </ResponsiveContainer>
  )
}

/* Glucose Trend */
export function GlucoseChart({ data }) {
  if (!data?.length) return <EmptyChart label="No glucose data yet" />
  const chartData = data
    .filter(r => r.glucose)
    .slice(0, 20).reverse()
    .map(r => ({ date: r.recordDate, glucose: r.glucose }))

  return (
    <ResponsiveContainer width="100%" height={200}>
      <AreaChart data={chartData} margin={{ top: 5, right: 10, bottom: 5, left: 0 }}>
        <defs>
          <linearGradient id="glGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%"  stopColor={AMBER} stopOpacity={.25} />
            <stop offset="95%" stopColor={AMBER} stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
        <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} />
        <YAxis domain={[40, 200]} tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} width={35} />
        <Tooltip content={<CustomTooltip unit="mg/dL" />} />
        <ReferenceLine y={70}  stroke={BLUE}  strokeDasharray="4 2" strokeWidth={1.5} />
        <ReferenceLine y={100} stroke={GREEN} strokeDasharray="4 2" strokeWidth={1.5} />
        <ReferenceLine y={126} stroke={CORAL} strokeDasharray="4 2" strokeWidth={1.5} />
        <Area type="monotone" dataKey="glucose" stroke={AMBER} strokeWidth={2.5} fill="url(#glGrad)" dot={{ r: 4, fill: AMBER, strokeWidth: 0 }} />
      </AreaChart>
    </ResponsiveContainer>
  )
}

/* Activity Bar Chart */
export function ActivityBarChart({ data }) {
  if (!data?.length) return <EmptyChart label="No activity data" />
  const levels = { LOW: 0, MODERATE: 0, HIGH: 0 }
  data.forEach(r => { if (r.activityLevel) levels[r.activityLevel]++ })
  const chartData = [
    { name: 'Low',      count: levels.LOW,      fill: CORAL },
    { name: 'Moderate', count: levels.MODERATE, fill: AMBER },
    { name: 'High',     count: levels.HIGH,     fill: GREEN },
  ]
  return (
    <ResponsiveContainer width="100%" height={180}>
      <BarChart data={chartData} margin={{ top: 5, right: 10, bottom: 5, left: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
        <XAxis dataKey="name" tick={{ fontSize: 12, fill: 'var(--text-muted)' }} tickLine={false} axisLine={false} />
        <YAxis tick={{ fontSize: 11, fill: 'var(--text-light)' }} tickLine={false} axisLine={false} width={25} allowDecimals={false} />
        <Tooltip cursor={{ fill: 'rgba(0,109,119,.06)' }} content={<CustomTooltip unit=" records" />} />
        <Bar dataKey="count" radius={[6, 6, 0, 0]}>
          {chartData.map((entry, i) => (
            <rect key={i} fill={entry.fill} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}

function EmptyChart({ label }) {
  return (
    <div style={{
      height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: 'var(--text-light)', fontSize: 13,
      border: '2px dashed var(--border)', borderRadius: 'var(--r-lg)',
    }}>
      {label}
    </div>
  )
}

import React, { useEffect, useState } from 'react'
import { adminAPI } from '../api'
import { PageHeader, Spinner, EmptyState } from '../components/ui'

const ROLE_COLORS = {
  STUDENT:  { bg: '#ddf4f7', color: '#006d77' },
  EMPLOYEE: { bg: '#ede9fe', color: '#7c3aed' },
  ADMIN:    { bg: '#fde8ea', color: '#dc2626' },
}

const GENDER_ICON = { MALE: '♂', FEMALE: '♀', OTHER: '⚧' }

export default function AdminUsersPage() {
  const [users, setUsers]     = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch]   = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')

  useEffect(() => {
    adminAPI.getUsers()
      .then(r => setUsers(r.data.data || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const filtered = users.filter(u => {
    const matchSearch =
      u.name.toLowerCase().includes(search.toLowerCase()) ||
      u.username.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase())
    const matchRole = roleFilter === 'ALL' || u.role === roleFilter
    return matchSearch && matchRole
  })

  return (
    <div className="animate-fadeIn">
      <PageHeader
        title="All Users"
        subtitle={`${users.length} registered users`}
      />

      {/* Filters */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 20, flexWrap: 'wrap' }}>
        <div style={{ position: 'relative', flex: 1, minWidth: 220 }}>
          <span style={{ position:'absolute', left:12, top:'50%', transform:'translateY(-50%)', color:'var(--text-light)', fontSize:15 }}>🔍</span>
          <input
            placeholder="Search by name, username or email…"
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{
              width:'100%', padding:'9px 14px 9px 36px',
              border:'1.5px solid var(--border)', borderRadius:'var(--r-md)',
              background:'var(--surface)', fontSize:14, outline:'none',
              transition:'border-color var(--t-fast)',
            }}
            onFocus={e => e.target.style.borderColor='var(--primary)'}
            onBlur={e => e.target.style.borderColor='var(--border)'}
          />
        </div>

        {/* Role filter pills */}
        <div style={{ display:'flex', gap:6 }}>
          {['ALL','STUDENT','EMPLOYEE','ADMIN'].map(role => {
            const rc = ROLE_COLORS[role]
            const isActive = roleFilter === role
            return (
              <button
                key={role}
                onClick={() => setRoleFilter(role)}
                style={{
                  padding:'8px 16px', borderRadius:'var(--r-md)', fontSize:12, fontWeight:600,
                  border: isActive
                    ? `1.5px solid ${rc?.color || 'var(--primary)'}`
                    : '1.5px solid var(--border)',
                  background: isActive ? (rc?.bg || 'var(--teal-50)') : 'var(--surface)',
                  color: isActive ? (rc?.color || 'var(--primary)') : 'var(--text-muted)',
                  cursor:'pointer', transition:'all var(--t-fast)',
                }}
              >
                {role === 'ALL' ? `All (${users.length})` : role}
              </button>
            )
          })}
        </div>
      </div>

      {loading ? (
        <div style={{ display:'flex', justifyContent:'center', padding:80 }}>
          <Spinner size={32} />
        </div>
      ) : filtered.length === 0 ? (
        <div className="card">
          <EmptyState icon="👤" title="No users found" description="Try adjusting your search or filter." />
        </div>
      ) : (
        <div className="card" style={{ overflowX:'auto' }}>
          <table style={{ width:'100%', borderCollapse:'collapse' }}>
            <thead>
              <tr style={{ borderBottom:'2px solid var(--border)' }}>
                {['User','Username','Email','Age','Gender','Role','Joined'].map(h => (
                  <th key={h} style={{
                    padding:'12px 18px', textAlign:'left', fontSize:11,
                    fontWeight:700, color:'var(--text-muted)',
                    textTransform:'uppercase', letterSpacing:.7, whiteSpace:'nowrap',
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((u, i) => {
                const rc = ROLE_COLORS[u.role] || { bg:'var(--slate-100)', color:'var(--text-muted)' }
                return (
                  <tr
                    key={u.id}
                    className="animate-fadeUp"
                    style={{ borderBottom:'1px solid var(--border)', animationDelay:`${i * 25}ms` }}
                    onMouseEnter={e => e.currentTarget.style.background = 'var(--teal-50)'}
                    onMouseLeave={e => e.currentTarget.style.background = ''}
                  >
                    {/* Avatar + Name */}
                    <td style={{ padding:'14px 18px' }}>
                      <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                        <div style={{
                          width:34, height:34, borderRadius:'50%', flexShrink:0,
                          background:`linear-gradient(135deg, ${rc.color}cc, ${rc.color})`,
                          display:'flex', alignItems:'center', justifyContent:'center',
                          color:'#fff', fontSize:13, fontWeight:700,
                        }}>
                          {u.name?.charAt(0)?.toUpperCase()}
                        </div>
                        <span style={{ fontWeight:600, fontSize:14 }}>{u.name}</span>
                      </div>
                    </td>
                    <td style={{ padding:'14px 18px', fontSize:13, color:'var(--text-muted)', fontFamily:'monospace' }}>
                      @{u.username}
                    </td>
                    <td style={{ padding:'14px 18px', fontSize:13, color:'var(--text-muted)' }}>
                      {u.email}
                    </td>
                    <td style={{ padding:'14px 18px', fontSize:14 }}>
                      {u.age ?? <span style={{ color:'var(--text-light)' }}>—</span>}
                    </td>
                    <td style={{ padding:'14px 18px', fontSize:16 }}>
                      {GENDER_ICON[u.gender] ?? <span style={{ color:'var(--text-light)', fontSize:13 }}>—</span>}
                    </td>
                    <td style={{ padding:'14px 18px' }}>
                      <span className="badge" style={{ background:rc.bg, color:rc.color, fontSize:11 }}>
                        {u.role}
                      </span>
                    </td>
                    <td style={{ padding:'14px 18px', fontSize:13, color:'var(--text-muted)', whiteSpace:'nowrap' }}>
                      {u.createdAt
                        ? new Date(u.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' })
                        : '—'}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>

          {/* Footer count */}
          <div style={{
            padding:'12px 20px', borderTop:'1px solid var(--border)',
            fontSize:12, color:'var(--text-muted)', textAlign:'right',
          }}>
            Showing {filtered.length} of {users.length} users
          </div>
        </div>
      )}
    </div>
  )
}

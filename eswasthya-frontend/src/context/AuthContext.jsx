import React, { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authAPI, userAPI } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [token, setToken]     = useState(null)
  const [loading, setLoading] = useState(true)

  // Rehydrate from localStorage on mount
  useEffect(() => {
    try {
      const savedToken = localStorage.getItem('eswasthya_token')
      const savedUser  = localStorage.getItem('eswasthya_user')
      if (savedToken && savedUser) {
        setToken(savedToken)
        // set the saved user optimistically, then refresh from server
        setUser(JSON.parse(savedUser))
        ;(async () => {
          try {
            const res = await userAPI.getProfile()
            const profile = res.data.data
            localStorage.setItem('eswasthya_user', JSON.stringify(profile))
            setUser(profile)
          } catch (e) {
            // ignore — keep savedUser
            console.warn('Failed to refresh profile on load', e)
          }
        })()
      }
    } catch {
      localStorage.removeItem('eswasthya_token')
      localStorage.removeItem('eswasthya_user')
    } finally {
      setLoading(false)
    }
  }, [])

  const login = useCallback(async (credentials) => {
    const res = await authAPI.login(credentials)
    const { token, csrfToken, ...userData } = res.data.data
    localStorage.setItem('eswasthya_token', token)
    if (csrfToken) {
      localStorage.setItem('eswasthya_csrf_token', csrfToken)
    }
    setToken(token)
    // Attempt to fetch full profile (includes email, age, gender, createdAt)
    try {
      const profileRes = await userAPI.getProfile()
      const profile = profileRes.data.data
      localStorage.setItem('eswasthya_user', JSON.stringify(profile))
      setUser(profile)
      return profile
    } catch (e) {
      // Fallback to what auth returned
      localStorage.setItem('eswasthya_user', JSON.stringify(userData))
      setUser(userData)
      return userData
    }
  }, [])

  const register = useCallback(async (data) => {
    const res = await authAPI.register(data)
    return res.data.data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('eswasthya_token')
    localStorage.removeItem('eswasthya_user')
    localStorage.removeItem('eswasthya_csrf_token')
    setToken(null)
    setUser(null)
  }, [])

  const isAdmin = user?.role === 'ADMIN'

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}

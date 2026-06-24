import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000, // 10 second timeout
})

// Request interceptor - attach token and CSRF header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('eswasthya_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // Add CSRF token for non-GET requests
    if (config.method && ['post', 'put', 'patch', 'delete'].includes(config.method.toLowerCase())) {
      const csrfToken = localStorage.getItem('eswasthya_csrf_token')
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor with retry logic
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Handle 401 - clear auth and redirect
    if (error.response?.status === 401) {
      localStorage.removeItem('eswasthya_token')
      localStorage.removeItem('eswasthya_user')
      localStorage.removeItem('eswasthya_csrf_token')
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    // Handle 403 - forbidden
    if (error.response?.status === 403) {
      const message = error.response?.data?.message || 'Access denied'
      console.error('Forbidden:', message)
      return Promise.reject(error)
    }

    // Handle 429 - rate limit
    if (error.response?.status === 429) {
      const message = error.response?.data?.message || 'Too many requests. Please try again later.'
      console.error('Rate limited:', message)
      return Promise.reject(new Error(message))
    }

    // Handle network errors
    if (!error.response) {
      const message = 'Network error. Please check your connection and try again.'
      console.error('Network error:', message)
      return Promise.reject(new Error(message))
    }

    // Retry logic for 5xx errors (max 2 retries)
    if (error.response?.status >= 500 && !originalRequest._retry) {
      originalRequest._retry = true
      await new Promise((resolve) => setTimeout(resolve, 1000))
      return api(originalRequest)
    }

    return Promise.reject(error)
  }
)

// Auth
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
}

// Users
export const userAPI = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.patch('/users/profile', data),
}

// Health records
export const healthAPI = {
  create: (data) => api.post('/health/records', data),
  getAll: () => api.get('/health/records'),
  getById: (id) => api.get(`/health/records/${id}`),
  getRange: (from, to) => api.get('/health/records/range', { params: { from, to } }),
  update: (id, data) => api.put(`/health/records/${id}`, data),
  delete: (id) => api.delete(`/health/records/${id}`),
  dashboard: () => api.get('/health/dashboard'),
  report: () => api.get('/health/report', { responseType: 'blob' }),
}

// Alerts
export const alertAPI = {
  getAll: () => api.get('/alerts'),
  getUnread: () => api.get('/alerts/unread'),
  getCount: () => api.get('/alerts/count'),
  markRead: (id) => api.put(`/alerts/${id}/read`),
  markAllRead: () => api.put('/alerts/read-all'),
}

// Admin
export const adminAPI = {
  getStats: () => api.get('/admin/stats'),
  getUsers: () => api.get('/admin/users'),
  getUserById: (id) => api.get(`/admin/users/${id}`),
  getAllRecords: () => api.get('/admin/health-records'),
}

// External health reference
export const externalAPI = {
  getTopics: () => api.get('/health/external/topics'),
  getBmiInfo: () => api.get('/health/external/bmi'),
  getNutrition: () => api.get('/health/external/nutrition'),
}

export default api
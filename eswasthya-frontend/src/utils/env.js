/**
 * Environment configuration validator
 */

const requiredEnvVars = []
const optionalEnvVars = [
  'VITE_API_BASE_URL',
  'VITE_APP_ENV',
  'VITE_ENABLE_ANALYTICS',
  'VITE_ENABLE_ERROR_REPORTING',
]

export function validateEnv() {
  const missing = requiredEnvVars.filter(key => !import.meta.env[key])
  
  if (missing.length > 0) {
    throw new Error(
      `Missing required environment variables: ${missing.join(', ')}`
    )
  }

  const warnings = optionalEnvVars.filter(key => !import.meta.env[key])
  
  if (warnings.length > 0 && import.meta.env.VITE_APP_ENV === 'production') {
    console.warn(
      `Optional environment variables not set: ${warnings.join(', ')}`
    )
  }
}

export function getEnvVar(key, defaultValue = null) {
  return import.meta.env[key] || defaultValue
}

export const config = {
  apiBaseUrl: getEnvVar('VITE_API_BASE_URL', '/api'),
  appEnv: getEnvVar('VITE_APP_ENV', 'development'),
  enableAnalytics: getEnvVar('VITE_ENABLE_ANALYTICS') === 'true',
  enableErrorReporting: getEnvVar('VITE_ENABLE_ERROR_REPORTING') !== 'false',
  isDevelopment: import.meta.env.DEV,
  isProduction: import.meta.env.PROD,
}
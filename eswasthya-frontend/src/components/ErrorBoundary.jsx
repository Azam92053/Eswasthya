import React, { Component } from 'react'

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null, errorInfo: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true }
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ error, errorInfo })
    console.error('Error caught by boundary:', error, errorInfo)
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null })
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--bg)',
          padding: '20px',
        }}>
          <div style={{
            maxWidth: '600px',
            background: 'var(--card)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--r-lg)',
            padding: '40px',
            boxShadow: '0 4px 24px rgba(0,0,0,.2)',
          }}>
            <h1 style={{
              fontFamily: 'var(--font-display)',
              color: 'var(--coral)',
              fontSize: '24px',
              marginBottom: '16px',
            }}>
              Something went wrong
            </h1>
            <p style={{
              color: 'var(--text-secondary)',
              marginBottom: '24px',
              lineHeight: '1.6',
            }}>
              We're sorry for the inconvenience. An unexpected error occurred.
            </p>
            {this.state.error && (
              <details style={{
                background: 'rgba(239,68,68,.1)',
                border: '1px solid rgba(239,68,68,.2)',
                borderRadius: 'var(--r-md)',
                padding: '16px',
                marginBottom: '24px',
              }}>
                <summary style={{
                  cursor: 'pointer',
                  fontWeight: 600,
                  color: 'var(--coral)',
                  marginBottom: '8px',
                }}>
                  Error Details
                </summary>
                <pre style={{
                  fontSize: '12px',
                  color: 'var(--text-secondary)',
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word',
                }}>
                  {this.state.error.toString()}
                </pre>
              </details>
            )}
            <button
              onClick={this.handleReset}
              style={{
                background: 'var(--teal-700)',
                color: '#fff',
                border: 'none',
                borderRadius: 'var(--r-md)',
                padding: '12px 24px',
                fontSize: '14px',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all var(--t-fast)',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--teal-600)'}
              onMouseLeave={e => e.currentTarget.style.background = 'var(--teal-700)'}
            >
              Try Again
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
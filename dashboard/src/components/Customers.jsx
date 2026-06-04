import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Customers() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('all')
  const [page, setPage] = useState(1)
  const [analyzingId, setAnalyzingId] = useState(null)
  const [generatingId, setGeneratingId] = useState(null)
  const [notification, setNotification] = useState(null)
  const perPage = 15

  useEffect(() => { loadCustomers() }, [])

  const loadCustomers = async () => {
    setLoading(true)
    try {
      const data = await api.getCustomers()
      setCustomers(data)
    } catch (e) {
      console.error('Failed to load customers:', e)
    }
    setLoading(false)
  }

  const handleAnalyze = async (customerId) => {
    setAnalyzingId(customerId)
    try {
      const result = await api.analyzeCustomer(customerId)
      setCustomers(prev => prev.map(c =>
        c.customerId === customerId
          ? { ...c, churnScore: result.churnScore || result.churn_score, riskLevel: result.riskLevel || result.risk_level }
          : c
      ))
      showNotification(`Churn score updated for ${customerId}`, 'success')
    } catch (e) {
      showNotification('Failed to analyze customer', 'error')
    }
    setAnalyzingId(null)
  }

  const handleGenerate = async (customerId) => {
    setGeneratingId(customerId)
    try {
      await api.generateCampaign(customerId)
      showNotification(`Campaign generated for ${customerId}!`, 'success')
    } catch (e) {
      showNotification('Failed to generate campaign', 'error')
    }
    setGeneratingId(null)
  }

  const showNotification = (msg, type) => {
    setNotification({ msg, type })
    setTimeout(() => setNotification(null), 3000)
  }

  const getChurnColor = (score) => {
    if (score == null) return 'var(--text-muted)'
    if (score >= 70) return 'var(--accent-red)'
    if (score >= 40) return 'var(--accent-amber)'
    return 'var(--accent-green)'
  }

  const getChurnGradient = (score) => {
    if (score == null) return 'rgba(255,255,255,0.1)'
    if (score >= 70) return 'var(--gradient-danger)'
    if (score >= 40) return 'var(--gradient-amber)'
    return 'var(--gradient-success)'
  }

  const filtered = customers.filter(c => {
    const matchesSearch = !search ||
      c.name?.toLowerCase().includes(search.toLowerCase()) ||
      c.email?.toLowerCase().includes(search.toLowerCase()) ||
      c.customerId?.toLowerCase().includes(search.toLowerCase())

    const risk = (c.riskLevel || '').toLowerCase()
    const matchesFilter = filter === 'all' ||
      (filter === 'high' && risk === 'high') ||
      (filter === 'medium' && risk === 'medium') ||
      (filter === 'low' && risk === 'low') ||
      (filter === 'unscored' && !c.churnScore && c.churnScore !== 0)

    return matchesSearch && matchesFilter
  })

  const totalPages = Math.ceil(filtered.length / perPage)
  const paginated = filtered.slice((page - 1) * perPage, page * perPage)

  if (loading) {
    return (
      <div className="page-content">
        <div className="loading-text">
          <span className="spinner"></span>
          Loading customers...
        </div>
      </div>
    )
  }

  return (
    <div className="page-content">
      {notification && (
        <div style={{
          position: 'fixed', top: 20, right: 20, zIndex: 2000,
          padding: '12px 20px', borderRadius: '10px',
          background: notification.type === 'success' ? 'rgba(16,185,129,0.15)' : 'rgba(239,68,68,0.15)',
          color: notification.type === 'success' ? 'var(--accent-green)' : 'var(--accent-red)',
          border: `1px solid ${notification.type === 'success' ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)'}`,
          fontSize: '13px', fontWeight: 600, animation: 'slideUp 0.3s var(--ease-out)'
        }}>
          {notification.msg}
        </div>
      )}

      <div className="search-bar">
        <div className="search-wrapper">
          <input
            type="text"
            className="search-input"
            placeholder="Search customers..."
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(1) }}
          />
        </div>
        <div className="filter-group">
          {['all', 'high', 'medium', 'low', 'unscored'].map(f => (
            <button
              key={f}
              className={`filter-btn ${filter === f ? 'active' : ''}`}
              onClick={() => { setFilter(f); setPage(1) }}
            >
              {f === 'all' ? 'All' : f.charAt(0).toUpperCase() + f.slice(1)}
            </button>
          ))}
        </div>
        <span style={{ marginLeft: 'auto', fontSize: '13px', color: 'var(--text-muted)' }}>
          {filtered.length} customer{filtered.length !== 1 ? 's' : ''}
        </span>
      </div>

      <div className="glass-card no-hover" style={{ padding: 0, overflow: 'hidden' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Recency</th>
              <th>Frequency</th>
              <th>Spend</th>
              <th>Churn Score</th>
              <th>Risk</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {paginated.map(c => (
              <tr key={c.customerId}>
                <td className="name-cell">{c.name}</td>
                <td>{c.email}</td>
                <td>{c.recency}d</td>
                <td>{c.frequency}</td>
                <td>${c.monetary?.toFixed(0)}</td>
                <td>
                  <div className="churn-bar">
                    <div className="churn-bar-track">
                      <div
                        className="churn-bar-fill"
                        style={{
                          width: `${c.churnScore ?? 0}%`,
                          background: getChurnGradient(c.churnScore)
                        }}
                      />
                    </div>
                    <span className="churn-bar-value" style={{ color: getChurnColor(c.churnScore) }}>
                      {c.churnScore != null ? Math.round(c.churnScore) : '—'}
                    </span>
                  </div>
                </td>
                <td>
                  {c.riskLevel ? (
                    <span className={`badge ${c.riskLevel.toLowerCase()}`}>
                      {c.riskLevel}
                    </span>
                  ) : (
                    <span className="text-muted" style={{ fontSize: '12px' }}>—</span>
                  )}
                </td>
                <td>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleAnalyze(c.customerId)}
                      disabled={analyzingId === c.customerId}
                    >
                      {analyzingId === c.customerId ? '⏳' : '🔍'} Analyze
                    </button>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => handleGenerate(c.customerId)}
                      disabled={generatingId === c.customerId || !c.churnScore}
                      title={!c.churnScore ? 'Analyze first' : ''}
                    >
                      {generatingId === c.customerId ? '⏳' : '✨'} Campaign
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button className="page-btn" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>
            ← Prev
          </button>
          {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
            let pageNum
            if (totalPages <= 7) {
              pageNum = i + 1
            } else if (page <= 4) {
              pageNum = i + 1
            } else if (page >= totalPages - 3) {
              pageNum = totalPages - 6 + i
            } else {
              pageNum = page - 3 + i
            }
            return (
              <button
                key={pageNum}
                className={`page-btn ${page === pageNum ? 'active' : ''}`}
                onClick={() => setPage(pageNum)}
              >
                {pageNum}
              </button>
            )
          })}
          <button className="page-btn" onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>
            Next →
          </button>
        </div>
      )}
    </div>
  )
}

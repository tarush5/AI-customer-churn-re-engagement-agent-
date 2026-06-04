import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Campaigns() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { loadLogs() }, [])

  const loadLogs = async () => {
    setLoading(true)
    try {
      const data = await api.getCampaignLogs()
      setLogs(Array.isArray(data) ? data : [])
    } catch (e) {
      console.error('Failed to load campaign logs:', e)
    }
    setLoading(false)
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return '—'
    try {
      const d = new Date(dateStr)
      return d.toLocaleDateString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      })
    } catch { return dateStr }
  }

  const getCampaignTypeClass = (type) => {
    if (!type) return ''
    const t = type.toLowerCase()
    if (t.includes('discount')) return 'discount'
    if (t.includes('loyalty')) return 'loyalty'
    if (t.includes('fomo') || t.includes('engagement')) return 'engagement'
    return 'engagement'
  }

  if (loading) {
    return (
      <div className="page-content">
        <div className="loading-text">
          <span className="spinner"></span>
          Loading campaign logs...
        </div>
      </div>
    )
  }

  if (logs.length === 0) {
    return (
      <div className="page-content">
        <div className="glass-card no-hover">
          <div className="empty-state">
            <div className="empty-state-icon">📭</div>
            <h3>No Campaigns Yet</h3>
            <p>Head over to the AI Agent section to generate and send your first re-engagement campaign.</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="page-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>
          {logs.length} campaign{logs.length !== 1 ? 's' : ''} sent
        </span>
        <button className="btn btn-secondary btn-sm" onClick={loadLogs}>
          🔄 Refresh
        </button>
      </div>

      <div className="glass-card no-hover" style={{ padding: 0, overflow: 'hidden' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Customer</th>
              <th>Campaign Type</th>
              <th>Channel</th>
              <th>Subject</th>
              <th>Status</th>
              <th>Sent At</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log, i) => (
              <tr key={log.id || i}>
                <td className="name-cell">
                  <div>{log.customerName || '—'}</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{log.customerEmail || ''}</div>
                </td>
                <td>
                  <span className={`badge ${getCampaignTypeClass(log.campaignType)}`}>
                    {log.campaignType || '—'}
                  </span>
                </td>
                <td>
                  <span className={`badge ${(log.channel || '').toLowerCase()}`}>
                    {log.channel === 'whatsapp' ? '💬 WhatsApp' : '📧 Email'}
                  </span>
                </td>
                <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {log.subject || '—'}
                </td>
                <td>
                  <span className={`badge ${(log.status || '').toLowerCase()}`}>
                    {log.status || '—'}
                  </span>
                </td>
                <td style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  {formatDate(log.sentAt)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

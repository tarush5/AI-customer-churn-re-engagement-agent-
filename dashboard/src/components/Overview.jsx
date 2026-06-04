import { useState, useEffect } from 'react'
import { api } from '../api'

export default function Overview() {
  const [stats, setStats] = useState(null)
  const [customers, setCustomers] = useState([])
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { loadData() }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [statsData, customersData, logsData] = await Promise.all([
        api.getStats().catch(() => null),
        api.getCustomers().catch(() => []),
        api.getCampaignLogs().catch(() => []),
      ])
      setStats(statsData)
      setCustomers(Array.isArray(customersData) ? customersData : [])
      setLogs(Array.isArray(logsData) ? logsData : [])
    } catch (e) {
      console.error('Failed to load overview data:', e)
    }
    setLoading(false)
  }

  const getChurnDistribution = () => {
    if (!customers.length) return { low: 0, medium: 0, high: 0, lowCount: 0, mediumCount: 0, highCount: 0 }
    const total = customers.length
    const high = customers.filter(c => (c.riskLevel || '').toUpperCase() === 'HIGH').length
    const medium = customers.filter(c => (c.riskLevel || '').toUpperCase() === 'MEDIUM').length
    const low = customers.filter(c => (c.riskLevel || '').toUpperCase() === 'LOW').length
    return {
      low: Math.round((low / total) * 100),
      medium: Math.round((medium / total) * 100),
      high: Math.round((high / total) * 100),
      lowCount: low, mediumCount: medium, highCount: high,
    }
  }

  const getReengagementRate = () => {
    if (!logs.length) return '0%'
    const opened = logs.filter(l => l.status === 'OPENED' || l.status === 'CLICKED').length
    return Math.round((opened / logs.length) * 100) + '%'
  }

  const getHighRiskCount = () => {
    return customers.filter(c => (c.riskLevel || '').toUpperCase() === 'HIGH').length
  }

  const recentLogs = logs.slice(-5).reverse()
  const dist = getChurnDistribution()

  if (loading) {
    return (
      <div className="page-content">
        <div className="stats-grid">
          {[1, 2, 3, 4].map(i => (
            <div key={i} className="glass-card shimmer-card">
              <div className="shimmer-line h-lg w-50"></div>
              <div className="shimmer-line w-75"></div>
            </div>
          ))}
        </div>
        <div className="loading-text">
          <span className="spinner"></span>
          Loading dashboard data...
        </div>
      </div>
    )
  }

  return (
    <div className="page-content">
      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="glass-card stat-card">
          <div className="stat-card-header">
            <div className="stat-icon blue">👤</div>
            <span className="stat-trend up">↑ Active</span>
          </div>
          <div className="stat-value">{stats?.totalCustomers || customers.length}</div>
          <div className="stat-label">Total Customers</div>
        </div>

        <div className="glass-card stat-card">
          <div className="stat-card-header">
            <div className="stat-icon red">⚠️</div>
            <span className="stat-trend down">↓ At Risk</span>
          </div>
          <div className="stat-value">{stats?.highRiskCount || getHighRiskCount()}</div>
          <div className="stat-label">High Risk Customers</div>
        </div>

        <div className="glass-card stat-card">
          <div className="stat-card-header">
            <div className="stat-icon purple">📨</div>
          </div>
          <div className="stat-value">{stats?.campaignsSent || logs.length}</div>
          <div className="stat-label">Campaigns Sent</div>
        </div>

        <div className="glass-card stat-card">
          <div className="stat-card-header">
            <div className="stat-icon green">📈</div>
            <span className="stat-trend up">↑ Growing</span>
          </div>
          <div className="stat-value">
            {stats?.reEngagementRate != null ? `${Math.round(stats.reEngagementRate)}%` : getReengagementRate()}
          </div>
          <div className="stat-label">Re-engagement Rate</div>
        </div>
      </div>

      {/* Charts Section */}
      <div className="chart-section">
        {/* Churn Distribution */}
        <div className="glass-card chart-card no-hover">
          <div className="chart-title">Churn Risk Distribution</div>
          <div className="bar-chart">
            <div className="bar-item">
              <div className="bar-label-row">
                <span className="bar-label">🟢 Low Risk</span>
                <span className="bar-value">{dist.lowCount} ({dist.low}%)</span>
              </div>
              <div className="bar-track">
                <div className="bar-fill green" style={{ width: `${dist.low || 2}%` }}></div>
              </div>
            </div>
            <div className="bar-item">
              <div className="bar-label-row">
                <span className="bar-label">🟡 Medium Risk</span>
                <span className="bar-value">{dist.mediumCount} ({dist.medium}%)</span>
              </div>
              <div className="bar-track">
                <div className="bar-fill amber" style={{ width: `${dist.medium || 2}%` }}></div>
              </div>
            </div>
            <div className="bar-item">
              <div className="bar-label-row">
                <span className="bar-label">🔴 High Risk</span>
                <span className="bar-value">{dist.highCount} ({dist.high}%)</span>
              </div>
              <div className="bar-track">
                <div className="bar-fill red" style={{ width: `${dist.high || 2}%` }}></div>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Campaigns */}
        <div className="glass-card chart-card no-hover">
          <div className="chart-title">Recent Campaigns</div>
          {recentLogs.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">📭</div>
              <h3>No campaigns yet</h3>
              <p>Generate and send your first campaign from the AI Agent section.</p>
            </div>
          ) : (
            <div className="campaign-timeline">
              {recentLogs.map((log, i) => (
                <div className="timeline-item" key={log.id || i}>
                  <div
                    className="timeline-dot"
                    style={{
                      background:
                        log.status === 'CLICKED' ? '#059669'
                        : log.status === 'OPENED' ? '#10b981'
                        : log.status === 'DELIVERED' ? '#8b5cf6'
                        : '#3b82f6',
                    }}
                  ></div>
                  <div className="timeline-content">
                    <div className="timeline-title">
                      {log.subject || 'Campaign'} → {log.customerName || 'Customer'}
                    </div>
                    <div className="timeline-meta">
                      <span className={`badge ${(log.status || '').toLowerCase()}`}>
                        {log.status}
                      </span>
                      <span>{log.sentAt ? new Date(log.sentAt).toLocaleDateString() : ''}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

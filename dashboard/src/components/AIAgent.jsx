import { useState, useEffect } from 'react'
import { api } from '../api'
import CampaignModal from './CampaignModal'

export default function AIAgent() {
  const [customers, setCustomers] = useState([])
  const [selectedId, setSelectedId] = useState('')
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [campaign, setCampaign] = useState(null)
  const [generating, setGenerating] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.getCustomers()
      .then(data => setCustomers(data))
      .catch(e => console.error('Failed to load customers:', e))
  }, [])

  useEffect(() => {
    if (selectedId) {
      const c = customers.find(c => c.customerId === selectedId)
      setSelectedCustomer(c || null)
      setCampaign(null)
      setError(null)
    } else {
      setSelectedCustomer(null)
      setCampaign(null)
    }
  }, [selectedId, customers])

  const handleGenerate = async () => {
    if (!selectedId) return
    setGenerating(true)
    setError(null)
    setCampaign(null)
    try {
      const result = await api.generateCampaign(selectedId)
      setCampaign(result)
    } catch (e) {
      setError('Failed to generate campaign. Make sure the backend services are running.')
      console.error(e)
    }
    setGenerating(false)
  }

  const handleSend = async () => {
    if (!campaign?.id) return
    setShowModal(true)
  }

  const getChurnColor = (score) => {
    if (score == null) return 'var(--text-muted)'
    if (score >= 70) return 'var(--accent-red)'
    if (score >= 40) return 'var(--accent-amber)'
    return 'var(--accent-green)'
  }

  return (
    <div className="page-content">
      <div className="ai-hero">
        <div className="ai-hero-icon">🧠</div>
        <h2>AI Campaign Agent</h2>
        <p>Select a customer, and let the AI analyze their behavior to craft a personalized re-engagement campaign</p>
      </div>

      <div className="ai-panels">
        {/* Left Panel — Customer Selection */}
        <div className="glass-card no-hover">
          <div className="panel-title">👤 Select Customer</div>

          <select
            className="customer-select"
            value={selectedId}
            onChange={e => setSelectedId(e.target.value)}
          >
            <option value="">— Choose a customer —</option>
            {customers.map(c => (
              <option key={c.customerId} value={c.customerId}>
                {c.customerId} — {c.name} {c.churnScore != null ? `(Score: ${Math.round(c.churnScore)})` : ''}
              </option>
            ))}
          </select>

          {selectedCustomer && (
            <div className="customer-details" style={{ animation: 'fadeIn 0.3s var(--ease-out)' }}>
              <div className="detail-item">
                <div className="detail-label">Name</div>
                <div className="detail-value">{selectedCustomer.name}</div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Email</div>
                <div className="detail-value" style={{ fontSize: '13px' }}>{selectedCustomer.email}</div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Recency</div>
                <div className="detail-value">{selectedCustomer.recency} days</div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Frequency</div>
                <div className="detail-value">{selectedCustomer.frequency} orders</div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Total Spend</div>
                <div className="detail-value">${selectedCustomer.monetary?.toFixed(0)}</div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Churn Score</div>
                <div className="detail-value" style={{ color: getChurnColor(selectedCustomer.churnScore) }}>
                  {selectedCustomer.churnScore != null ? `${Math.round(selectedCustomer.churnScore)}%` : 'Not analyzed'}
                </div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Risk Level</div>
                <div className="detail-value">
                  {selectedCustomer.riskLevel ? (
                    <span className={`badge ${selectedCustomer.riskLevel.toLowerCase()}`}>
                      {selectedCustomer.riskLevel}
                    </span>
                  ) : '—'}
                </div>
              </div>
              <div className="detail-item">
                <div className="detail-label">Channel</div>
                <div className="detail-value" style={{ textTransform: 'capitalize' }}>
                  {selectedCustomer.preferredChannel || 'email'}
                </div>
              </div>
            </div>
          )}

          <div className="generate-btn-wrapper">
            <button
              className={`btn btn-primary ${generating ? 'btn-loading' : ''}`}
              onClick={handleGenerate}
              disabled={!selectedId || generating}
              style={{ width: '100%' }}
            >
              {generating ? (
                <><span className="spinner" style={{ width: 14, height: 14, borderWidth: '2px' }}></span> AI is generating...</>
              ) : (
                <>✨ Generate Campaign</>
              )}
            </button>
          </div>
        </div>

        {/* Right Panel — Campaign Result */}
        <div className="glass-card no-hover">
          <div className="panel-title">📝 Campaign Preview</div>

          {error && (
            <div style={{
              padding: '14px 16px', borderRadius: '10px',
              background: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.15)',
              color: 'var(--accent-red)', fontSize: '13px', marginBottom: '16px'
            }}>
              ⚠️ {error}
            </div>
          )}

          {generating && (
            <div style={{ textAlign: 'center', padding: '40px 20px' }}>
              <div style={{ fontSize: '48px', marginBottom: '16px', animation: 'pulse 1.5s ease-in-out infinite' }}>🤖</div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginBottom: '8px' }}>
                AI is analyzing customer behavior...
              </p>
              <p style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
                Determining strategy and crafting message
              </p>
              <div style={{ marginTop: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div className="shimmer-line w-full" style={{ height: '12px' }}></div>
                <div className="shimmer-line w-75" style={{ height: '12px' }}></div>
                <div className="shimmer-line w-50" style={{ height: '12px' }}></div>
              </div>
            </div>
          )}

          {!generating && !campaign && !error && (
            <div className="ai-placeholder">
              <div className="ai-placeholder-icon">📩</div>
              <h3>Campaign will appear here</h3>
              <p>Select a customer and click "Generate Campaign" to see the AI-crafted message</p>
            </div>
          )}

          {campaign && !generating && (
            <div className="campaign-preview">
              <div className="campaign-type-badge">
                <span className={`badge ${(campaign.campaignType || campaign.campaign_type || '').toLowerCase().replace(/\s+/g, '_')}`}>
                  {campaign.campaignType || campaign.campaign_type || 'Campaign'}
                </span>
              </div>

              <div className="campaign-subject">
                {campaign.subject || 'Untitled Campaign'}
              </div>

              <div className={`message-preview ${(campaign.channel || 'email').toLowerCase()}`}>
                <p>{campaign.message || 'No message content'}</p>
              </div>

              {(campaign.strategyReasoning || campaign.strategy_reasoning) && (
                <div className="strategy-box">
                  <div className="strategy-box-title">🧠 AI Strategy Reasoning</div>
                  <p>{campaign.strategyReasoning || campaign.strategy_reasoning}</p>
                </div>
              )}

              <div className="campaign-actions">
                <button className="btn btn-success" onClick={handleSend} style={{ flex: 1 }}>
                  📤 Send Campaign
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {showModal && campaign && (
        <CampaignModal
          campaign={campaign}
          customer={selectedCustomer}
          onClose={() => setShowModal(false)}
          onSent={() => {
            setShowModal(false)
            setCampaign(null)
            setSelectedId('')
          }}
        />
      )}
    </div>
  )
}

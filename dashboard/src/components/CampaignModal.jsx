import { useState } from 'react'
import { api } from '../api'

export default function CampaignModal({ campaign, customer, onClose, onSent }) {
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)
  const [error, setError] = useState(null)

  const handleSend = async () => {
    setSending(true)
    setError(null)
    try {
      await api.sendCampaign(campaign.id)
      setSent(true)
      setTimeout(() => {
        onSent()
      }, 2000)
    } catch (e) {
      setError('Failed to send campaign. Please try again.')
      console.error(e)
    }
    setSending(false)
  }

  const channel = (campaign.channel || 'email').toLowerCase()
  const isWhatsApp = channel === 'whatsapp'

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-content">
        {sent ? (
          <div className="success-state">
            <div className="success-icon">✅</div>
            <h3>Campaign Sent!</h3>
            <p>
              Re-engagement message has been dispatched to {customer?.name || 'the customer'} via {isWhatsApp ? 'WhatsApp' : 'Email'}.
            </p>
          </div>
        ) : (
          <>
            <div className="modal-header">
              <h3>📤 Send Campaign</h3>
              <button className="modal-close" onClick={onClose}>✕</button>
            </div>

            {/* Channel Preview */}
            <div style={{ marginBottom: '20px' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.5px', fontWeight: 600 }}>
                {isWhatsApp ? '💬 WhatsApp Preview' : '📧 Email Preview'}
              </div>

              {isWhatsApp ? (
                /* WhatsApp Chat Bubble */
                <div style={{
                  background: 'rgba(16, 185, 129, 0.06)',
                  borderRadius: '12px',
                  padding: '20px',
                  border: '1px solid rgba(16, 185, 129, 0.12)'
                }}>
                  <div style={{
                    background: 'rgba(16, 185, 129, 0.12)',
                    borderRadius: '12px 12px 0 12px',
                    padding: '14px 18px',
                    maxWidth: '85%',
                    marginLeft: 'auto'
                  }}>
                    <p style={{ fontSize: '14px', lineHeight: '1.6', color: 'var(--text-secondary)', whiteSpace: 'pre-line' }}>
                      {campaign.message}
                    </p>
                    <div style={{ textAlign: 'right', fontSize: '11px', color: 'var(--text-muted)', marginTop: '6px' }}>
                      {new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} ✓✓
                    </div>
                  </div>
                </div>
              ) : (
                /* Email Preview */
                <div style={{
                  background: 'rgba(59, 130, 246, 0.04)',
                  borderRadius: '12px',
                  border: '1px solid rgba(59, 130, 246, 0.1)',
                  overflow: 'hidden'
                }}>
                  <div style={{
                    background: 'rgba(59, 130, 246, 0.08)',
                    padding: '14px 18px',
                    borderBottom: '1px solid rgba(59, 130, 246, 0.08)'
                  }}>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>
                      To: {customer?.email || 'customer@email.com'}
                    </div>
                    <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>
                      {campaign.subject || 'Re-engagement Campaign'}
                    </div>
                  </div>
                  <div style={{ padding: '18px' }}>
                    <p style={{ fontSize: '14px', lineHeight: '1.7', color: 'var(--text-secondary)', whiteSpace: 'pre-line' }}>
                      {campaign.message}
                    </p>
                    {/* CTA Button Mock */}
                    <div style={{ textAlign: 'center', marginTop: '20px' }}>
                      <span style={{
                        display: 'inline-block',
                        background: 'var(--gradient-primary)',
                        color: 'white',
                        padding: '10px 28px',
                        borderRadius: '8px',
                        fontSize: '14px',
                        fontWeight: 600
                      }}>
                        Shop Now →
                      </span>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Recipient Info */}
            <div style={{
              display: 'flex', gap: '12px', marginBottom: '20px',
              padding: '12px 16px', background: 'rgba(255,255,255,0.03)',
              borderRadius: '10px', border: '1px solid rgba(255,255,255,0.04)'
            }}>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '2px' }}>Recipient</div>
                <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>{customer?.name || '—'}</div>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '2px' }}>Channel</div>
                <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
                  {isWhatsApp ? '💬 WhatsApp' : '📧 Email'}
                </div>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '2px' }}>Type</div>
                <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
                  {campaign.campaignType || campaign.campaign_type || '—'}
                </div>
              </div>
            </div>

            {error && (
              <div style={{
                padding: '12px 16px', borderRadius: '10px', marginBottom: '16px',
                background: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.15)',
                color: 'var(--accent-red)', fontSize: '13px'
              }}>
                ⚠️ {error}
              </div>
            )}

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
              <button
                className={`btn btn-primary ${sending ? 'btn-loading' : ''}`}
                onClick={handleSend}
                disabled={sending}
              >
                {sending ? (
                  <><span className="spinner" style={{ width: 14, height: 14, borderWidth: '2px' }}></span> Sending...</>
                ) : (
                  <>📤 Confirm & Send</>
                )}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

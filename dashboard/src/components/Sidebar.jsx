export default function Sidebar({ active, onNavigate }) {
  const navItems = [
    { id: 'overview', icon: '📊', label: 'Overview' },
    { id: 'customers', icon: '👥', label: 'Customers' },
    { id: 'campaigns', icon: '📧', label: 'Campaigns' },
    { id: 'agent', icon: '🤖', label: 'AI Agent' },
  ]

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h1>RetainIQ</h1>
        <p>AI Retention Engine</p>
      </div>

      <nav className="sidebar-nav">
        {navItems.map(item => (
          <div
            key={item.id}
            className={`nav-item ${active === item.id ? 'active' : ''}`}
            onClick={() => onNavigate(item.id)}
          >
            <span className="nav-icon">{item.icon}</span>
            <span>{item.label}</span>
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="system-status">
          <span className="status-dot"></span>
          <span>All Systems Operational</span>
        </div>
      </div>
    </aside>
  )
}

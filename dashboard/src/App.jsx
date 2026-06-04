import { useState } from 'react'
import './index.css'
import Sidebar from './components/Sidebar'
import Overview from './components/Overview'
import Customers from './components/Customers'
import Campaigns from './components/Campaigns'
import AIAgent from './components/AIAgent'

function App() {
  const [activeSection, setActiveSection] = useState('overview')

  const sectionTitles = {
    overview: 'Dashboard Overview',
    customers: 'Customer Management',
    campaigns: 'Campaign History',
    agent: 'AI Campaign Agent',
  }

  const renderSection = () => {
    switch (activeSection) {
      case 'overview': return <Overview />
      case 'customers': return <Customers />
      case 'campaigns': return <Campaigns />
      case 'agent': return <AIAgent />
      default: return <Overview />
    }
  }

  return (
    <div className="app-layout">
      <Sidebar active={activeSection} onNavigate={setActiveSection} />
      <main className="main-content">
        <header className="content-header">
          <h2>{sectionTitles[activeSection]}</h2>
          <div className="header-actions">
            <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              RetainIQ v1.0
            </span>
          </div>
        </header>
        {renderSection()}
      </main>
    </div>
  )
}

export default App

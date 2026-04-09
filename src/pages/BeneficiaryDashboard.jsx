import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { Heart, Clipboard, Box, Upload, Bell, Map, LogOut } from 'lucide-react'
import { materialNeeds } from '../data/needs'
import { beneficiaryAssignedDonations } from '../data/beneficiary-donations'
import { generateBeneficiaryNotifications } from '../data/beneficiary-notifications'
import { RegisterNeeds } from '../components/beneficiary/RegisterNeeds'
import { DonationStatus } from '../components/beneficiary/DonationStatus'
import { ConfirmReception } from '../components/beneficiary/ConfirmReception'
import { BeneficiaryDeliveries } from '../components/beneficiary/BeneficiaryDeliveries'
import { BeneficiaryNotifications } from '../components/beneficiary/BeneficiaryNotifications'
import '../styles/pages/beneficiary-dashboard.css'

export function BeneficiaryDashboard() {
  const { user, logout } = useAuth()
  const [activeTab, setActiveTab] = useState('overview')
  const notifications = generateBeneficiaryNotifications(user?.id)
  const unreadNotifications = notifications.filter(n => !n.read).length
  const activePendingNeeds = materialNeeds.filter(n => n.status === 'Activa').length
  const assignedDonations = beneficiaryAssignedDonations.slice(0, 4)
  const deliveredDonations = beneficiaryAssignedDonations.filter(d => d.status === 'Entregada').length

  const handleLogout = () => {
    logout()
    window.location.href = '/'
  }

  return (
    <div className="beneficiary-dashboard">
      {/* Header */}
      <header className="beneficiary-header">
        <div className="header-left">
          <div className="header-logo">
            <Heart size={28} />
            <h1>DonaTrack</h1>
          </div>
        </div>
        <div className="header-right">
          <div className="entity-info">
            <p><strong>{user?.nombre}</strong></p>
            <small>{user?.contact}</small>
          </div>
          <button className="btn-logout" onClick={handleLogout}>
            <LogOut size={18} />
            Salir
          </button>
        </div>
      </header>

      {/* Navigation */}
      <nav className="beneficiary-nav">
        <Link to="#" onClick={() => setActiveTab('overview')} className={`nav-item ${activeTab === 'overview' ? 'active' : ''}`}>
          <Heart size={20} />
          <span>Inicio</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('needs')} className={`nav-item ${activeTab === 'needs' ? 'active' : ''}`}>
          <Clipboard size={20} />
          <span>Necesidades</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('donations')} className={`nav-item ${activeTab === 'donations' ? 'active' : ''}`}>
          <Box size={20} />
          <span>Donaciones</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('confirm')} className={`nav-item ${activeTab === 'confirm' ? 'active' : ''}`}>
          <Upload size={20} />
          <span>Confirmar</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('deliveries')} className={`nav-item ${activeTab === 'deliveries' ? 'active' : ''}`}>
          <Map size={20} />
          <span>Entregas</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('notifications')} className={`nav-item notifications-icon ${activeTab === 'notifications' ? 'active' : ''}`}>
          <div style={{ position: 'relative' }}>
            <Bell size={20} />
            {unreadNotifications > 0 && (
              <span className="notification-badge">{unreadNotifications}</span>
            )}
          </div>
          <span>Notificaciones</span>
        </Link>
      </nav>

      {/* Main Content */}
      <main className="beneficiary-main-content">
        {activeTab === 'overview' && (
          <div className="overview-section">
            {/* Stats Cards */}
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5E5FF' }}>
                  <Clipboard size={24} color="#667EEA" />
                </div>
                <div className="stat-content">
                  <h3>Necesidades Activas</h3>
                  <p className="stat-value">{activePendingNeeds}</p>
                  <small>Registradas</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#FFE5E5' }}>
                  <Box size={24} color="#E31C23" />
                </div>
                <div className="stat-content">
                  <h3>Donaciones Asignadas</h3>
                  <p className="stat-value">{beneficiaryAssignedDonations.length}</p>
                  <small>Totales</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5F9E5' }}>
                  <Upload size={24} color="#4CAF50" />
                </div>
                <div className="stat-content">
                  <h3>Entregas Confirmadas</h3>
                  <p className="stat-value">{deliveredDonations}</p>
                  <small>Recibidas</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5F3FF' }}>
                  <Bell size={24} color="#667EEA" />
                </div>
                <div className="stat-content">
                  <h3>Notificaciones</h3>
                  <p className="stat-value">{unreadNotifications}</p>
                  <small>Sin leer</small>
                </div>
              </div>
            </div>

            {/* Pending Needs */}
            <div className="card">
              <h2>Necesidades Activas</h2>
              <div className="needs-grid">
                {materialNeeds.filter(n => n.status === 'Activa').slice(0, 3).map(need => (
                  <div key={need.id} className="need-item">
                    <img src={need.image_url} alt={need.title} className="need-image" />
                    <div className="need-content">
                      <div>
                        <h4>{need.title}</h4>
                        <p>{need.description}</p>
                        <div className="need-meta">
                          <span className="category">{need.category}</span>
                          <span className={`priority priority-${need.priority.toLowerCase()}`}>
                            {need.priority}
                          </span>
                        </div>
                      </div>
                      <div className="need-quantity">
                        <strong>{need.quantity} {need.unit}</strong>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Recent Donations */}
            <div className="card">
              <h2>Donaciones Asignadas Recientemente</h2>
              <div className="donations-list">
                {assignedDonations.map(donation => (
                  <div key={donation.id} className="donation-row">
                    <div className="donor-info">
                      <h4>{donation.donor_name}</h4>
                      <small>{donation.title}</small>
                    </div>
                    <div className="donation-items">
                      <span className="item-count">{donation.items.length} items</span>
                    </div>
                    <div className="donation-status">
                      <span className={`status status-${donation.status.toLowerCase().replace(/\s/g, '-')}`}>
                        {donation.status}
                      </span>
                    </div>
                    <div className="donation-date">
                      <small>{donation.expected_delivery}</small>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'needs' && <RegisterNeeds />}
        {activeTab === 'donations' && <DonationStatus />}
        {activeTab === 'confirm' && <ConfirmReception />}
        {activeTab === 'deliveries' && <BeneficiaryDeliveries />}
        {activeTab === 'notifications' && <BeneficiaryNotifications notifications={notifications} />}
      </main>
    </div>
  )
}

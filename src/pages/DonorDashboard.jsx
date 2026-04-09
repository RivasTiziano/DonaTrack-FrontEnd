import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { Heart, Package, Users, Zap, Bell, Map, LogOut } from 'lucide-react'
import { donations } from '../data/donations'
import { missions, donorCategories, badges } from '../data/incentives'
import { generateUserNotifications } from '../data/notifications'
import { DonorDonations } from '../components/donor/DonorDonations'
import { BeneficiaryExplorer } from '../components/donor/BeneficiaryExplorer'
import { IncentivesSection } from '../components/donor/IncentivesSection'
import { ActiveDeliveries } from '../components/donor/ActiveDeliveries'
import { NotificationsCenter } from '../components/donor/NotificationsCenter'
import '../styles/pages/donor-dashboard.css'

export function DonorDashboard() {
  const { user, logout } = useAuth()
  const [activeTab, setActiveTab] = useState('overview')
  const notifications = generateUserNotifications(user?.id)
  const unreadNotifications = notifications.filter(n => !n.read).length
  const userDonations = donations.slice(0, 3)
  const completedMissions = missions.filter(m => m.completed).length
  const totalPoints = completedMissions * 100 + (user?.category === 'Platino' ? 50 : 0)

  const handleLogout = () => {
    logout()
    window.location.href = '/'
  }

  const userCategoryInfo = donorCategories.find(cat => cat.name === user?.category)

  return (
    <div className="donor-dashboard">
      {/* Header */}
      <header className="donor-header">
        <div className="header-left">
          <div className="header-logo">
            <Heart size={28} />
            <h1>DonaTrack</h1>
          </div>
        </div>
        <div className="header-right">
          <p>Bienvenido, <strong>{user?.nombre}</strong></p>
          <button className="btn-logout" onClick={handleLogout}>
            <LogOut size={18} />
            Salir
          </button>
        </div>
      </header>

      {/* Navigation */}
      <nav className="donor-nav">
        <Link to="#" onClick={() => setActiveTab('overview')} className={`nav-item ${activeTab === 'overview' ? 'active' : ''}`}>
          <Heart size={20} />
          <span>Inicio</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('donations')} className={`nav-item ${activeTab === 'donations' ? 'active' : ''}`}>
          <Package size={20} />
          <span>Mis Donaciones</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('beneficiaries')} className={`nav-item ${activeTab === 'beneficiaries' ? 'active' : ''}`}>
          <Users size={20} />
          <span>Entidades</span>
        </Link>
        <Link to="#" onClick={() => setActiveTab('incentives')} className={`nav-item ${activeTab === 'incentives' ? 'active' : ''}`}>
          <Zap size={20} />
          <span>Incentivos</span>
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
      <main className="donor-main-content">
        {activeTab === 'overview' && (
          <div className="overview-section">
            {/* Stats Cards */}
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#FFE5E5' }}>
                  <Package size={24} color="#E31C23" />
                </div>
                <div className="stat-content">
                  <h3>Donaciones</h3>
                  <p className="stat-value">{userDonations.length}</p>
                  <small>Realizadas</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5F3FF' }}>
                  <Zap size={24} color="#667EEA" />
                </div>
                <div className="stat-content">
                  <h3>Puntos</h3>
                  <p className="stat-value">{totalPoints}</p>
                  <small>Acumulados</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#FFF5E5' }}>
                  <Users size={24} color="#FF9800" />
                </div>
                <div className="stat-content">
                  <h3>Categoría</h3>
                  <p className="stat-value">{user?.category}</p>
                  <small>Tu nivel</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5F9E5' }}>
                  <Heart size={24} color="#4CAF50" />
                </div>
                <div className="stat-content">
                  <h3>Impacto</h3>
                  <p className="stat-value">150+</p>
                  <small>Beneficiarios</small>
                </div>
              </div>
            </div>

            {/* Category Progress */}
            <div className="card">
              <h2>Progreso en Categoría</h2>
              <div className="category-progress">
                <div className="category-info">
                  <div className="category-badge" style={{ backgroundColor: userCategoryInfo?.color }}>
                    {userCategoryInfo?.name.charAt(0)}
                  </div>
                  <div>
                    <h3>{userCategoryInfo?.name}</h3>
                    <p>{totalPoints} puntos acumulados</p>
                  </div>
                </div>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: `${(totalPoints % 100) + 50}%` }}></div>
                </div>
                <p className="progress-text">Próximo nivel a {(Math.floor(totalPoints / 100) + 1) * 100} puntos</p>
              </div>
            </div>

            {/* Recent Donations */}
            <div className="card">
              <h2>Donaciones Recientes</h2>
              <div className="recent-donations">
                {userDonations.map(donation => (
                  <div key={donation.id} className="donation-item">
                    <img src={donation.image_url} alt={donation.title} className="donation-image" />
                    <div className="donation-info">
                      <h4>{donation.title}</h4>
                      <p>{donation.entity_name}</p>
                      <div className="donation-meta">
                        <span className={`status status-${donation.status.toLowerCase()}`}>
                          {donation.status}
                        </span>
                        <span className="date">{donation.delivery_date}</span>
                      </div>
                    </div>
                    <div className="donation-units">
                      <strong>{donation.total_units}</strong>
                      <small>unidades</small>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Badges */}
            <div className="card">
              <h2>Insignias Obtenidas</h2>
              <div className="badges-grid">
                {badges.map(badge => (
                  <div key={badge.id} className="badge-item">
                    <div className="badge-icon">{badge.icon}</div>
                    <h4>{badge.name}</h4>
                    <p>{badge.description}</p>
                    <small>{new Date(badge.earnedDate).toLocaleDateString('es-AR')}</small>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'donations' && <DonorDonations />}
        {activeTab === 'beneficiaries' && <BeneficiaryExplorer />}
        {activeTab === 'incentives' && <IncentivesSection />}
        {activeTab === 'deliveries' && <ActiveDeliveries />}
        {activeTab === 'notifications' && <NotificationsCenter />}
      </main>
    </div>
  )
}

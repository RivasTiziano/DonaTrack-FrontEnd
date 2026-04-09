import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { Heart, Users, Package, Truck, TrendingUp, Upload, LogOut } from 'lucide-react'
import { registeredDonors } from '../data/donors-list'
import { pendingDonations } from '../data/donations-pending'
import { availableTrucks } from '../data/trucks'
import { currentMonthRanking } from '../data/donor-rankings'
import { RegisterDonor } from './admin/RegisterDonor'
import { ManageDonations } from './admin/ManageDonations'
import { AssignBeneficiary } from './admin/AssignBeneficiary'
import { ManageTrucks } from './admin/ManageTrucks'
import { DonorRankings } from './admin/DonorRankings'
import { ImportDonorsCSV } from './admin/ImportDonorsCSV'
import '../styles/pages/admin-dashboard.css'

export function AdminDashboard() {
  const { user, logout } = useAuth()
  const [activeTab, setActiveTab] = useState('overview')
  
  const activeTrucks = availableTrucks.filter(t => t.status === 'Disponible').length
  const pendingAssignments = pendingDonations.filter(d => d.status === 'Pendiente').length
  const topDonor = currentMonthRanking[0]

  const handleLogout = () => {
    logout()
    window.location.href = '/'
  }

  return (
    <div className="admin-dashboard">
      {/* Header */}
      <header className="admin-header">
        <div className="header-left">
          <div className="header-logo">
            <Heart size={28} />
            <h1>DonaTrack Admin</h1>
          </div>
        </div>
        <div className="header-right">
          <div className="admin-info">
            <p><strong>{user?.nombre || user?.name}</strong></p>
            <small>Administrador</small>
          </div>
          <button className="btn-logout" onClick={handleLogout}>
            <LogOut size={18} />
            Salir
          </button>
        </div>
      </header>

      {/* Navigation */}
      <nav className="admin-nav">
        <button onClick={() => setActiveTab('overview')} className={`nav-tab ${activeTab === 'overview' ? 'active' : ''}`}>
          <Heart size={20} />
          <span>Inicio</span>
        </button>
        <button onClick={() => setActiveTab('donors')} className={`nav-tab ${activeTab === 'donors' ? 'active' : ''}`}>
          <Users size={20} />
          <span>Donantes</span>
        </button>
        <button onClick={() => setActiveTab('donations')} className={`nav-tab ${activeTab === 'donations' ? 'active' : ''}`}>
          <Package size={20} />
          <span>Donaciones</span>
        </button>
        <button onClick={() => setActiveTab('assign')} className={`nav-tab ${activeTab === 'assign' ? 'active' : ''}`}>
          <Users size={20} />
          <span>Asignar</span>
        </button>
        <button onClick={() => setActiveTab('trucks')} className={`nav-tab ${activeTab === 'trucks' ? 'active' : ''}`}>
          <Truck size={20} />
          <span>Camiones</span>
        </button>
        <button onClick={() => setActiveTab('rankings')} className={`nav-tab ${activeTab === 'rankings' ? 'active' : ''}`}>
          <TrendingUp size={20} />
          <span>Rankings</span>
        </button>
        <button onClick={() => setActiveTab('import')} className={`nav-tab ${activeTab === 'import' ? 'active' : ''}`}>
          <Upload size={20} />
          <span>Importar</span>
        </button>
      </nav>

      {/* Main Content */}
      <main className="admin-content">
        {activeTab === 'overview' && (
          <div className="overview-section">
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5E5FF' }}>
                  <Users size={24} color="#667EEA" />
                </div>
                <div className="stat-content">
                  <h3>Donantes Registrados</h3>
                  <p className="stat-value">{registeredDonors.length}</p>
                  <small>Activos e inactivos</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#FFE5E5' }}>
                  <Package size={24} color="#E31C23" />
                </div>
                <div className="stat-content">
                  <h3>Donaciones Pendientes</h3>
                  <p className="stat-value">{pendingAssignments}</p>
                  <small>Esperando asignación</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5F9E5' }}>
                  <Truck size={24} color="#4CAF50" />
                </div>
                <div className="stat-content">
                  <h3>Camiones Disponibles</h3>
                  <p className="stat-value">{activeTrucks}</p>
                  <small>Listos para entregas</small>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ backgroundColor: '#E5F3FF' }}>
                  <TrendingUp size={24} color="#667EEA" />
                </div>
                <div className="stat-content">
                  <h3>Top Donante Mes</h3>
                  <p className="stat-value">{topDonor.donations}</p>
                  <small>{topDonor.name}</small>
                </div>
              </div>
            </div>

            <div className="card">
              <h2>Sistema de Administración</h2>
              <p>Bienvenido al panel de administración de DonaTrack. Desde aquí puedes:</p>
              <ul>
                <li>✓ Registrar nuevos donantes</li>
                <li>✓ Gestionar donaciones en depósito</li>
                <li>✓ Asignar beneficiarios</li>
                <li>✓ Administrar camiones</li>
                <li>✓ Ver rankings de donantes</li>
                <li>✓ Importar donantes desde CSV</li>
              </ul>
            </div>
          </div>
        )}

        {activeTab === 'donors' && <RegisterDonor />}
        {activeTab === 'donations' && <ManageDonations />}
        {activeTab === 'assign' && <AssignBeneficiary />}
        {activeTab === 'trucks' && <ManageTrucks />}
        {activeTab === 'rankings' && <DonorRankings />}
        {activeTab === 'import' && <ImportDonorsCSV />}
      </main>
    </div>
  )
}

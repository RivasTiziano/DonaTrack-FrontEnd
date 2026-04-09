import { useState } from 'react'
import { availableTrucks, truckStatuses } from '../../data/trucks'
import { Truck, Edit2, AlertCircle, CheckCircle } from 'lucide-react'
import '../../styles/pages/manage-trucks.css'

export function ManageTrucks() {
  const [trucks, setTrucks] = useState(availableTrucks)
  const [selectedTruck, setSelectedTruck] = useState(null)
  const [filter, setFilter] = useState('all')

  const filteredTrucks = filter === 'all' 
    ? trucks
    : trucks.filter(t => t.status === filter)

  const needsMaintenance = (nextDate) => {
    const today = new Date()
    const mainDate = new Date(nextDate)
    const daysLeft = Math.floor((mainDate - today) / (1000 * 60 * 60 * 24))
    return daysLeft <= 30 && daysLeft > 0
  }

  const insuranceExpiring = (expiryDate) => {
    const today = new Date()
    const expDate = new Date(expiryDate)
    const daysLeft = Math.floor((expDate - today) / (1000 * 60 * 60 * 24))
    return daysLeft <= 60 && daysLeft > 0
  }

  const getStatusColor = (status) => {
    const colors = {
      'Disponible': '#2ecc71',
      'En ruta': '#f39c12',
      'Mantenimiento': '#e74c3c',
      'Fuera de servicio': '#95a5a6'
    }
    return colors[status] || '#95a5a6'
  }

  return (
    <div className="manage-trucks-view">
      <div className="section-header">
        <h2>Administrar Camiones</h2>
        <p>Gestiona el estado y mantenimiento de la flota de camiones</p>
      </div>

      {/* Filter Tabs */}
      <div className="filter-tabs">
        <button className={`filter-tab ${filter === 'all' ? 'active' : ''}`} onClick={() => setFilter('all')}>
          Todos ({trucks.length})
        </button>
        {truckStatuses.map(status => (
          <button
            key={status}
            className={`filter-tab ${filter === status ? 'active' : ''}`}
            onClick={() => setFilter(status)}
          >
            {status} ({trucks.filter(t => t.status === status).length})
          </button>
        ))}
      </div>

      <div className="trucks-grid">
        {filteredTrucks.map(truck => (
          <div
            key={truck.id}
            className={`truck-card ${selectedTruck?.id === truck.id ? 'selected' : ''}`}
            onClick={() => setSelectedTruck(truck)}
          >
            <div className="truck-header">
              <div className="truck-icon-plate">
                <div className="truck-icon">
                  <Truck size={32} />
                </div>
                <div className="truck-plate">{truck.plate}</div>
              </div>
              <span className="status-badge" style={{ backgroundColor: getStatusColor(truck.status) }}>
                {truck.status}
              </span>
            </div>

            <div className="truck-info">
              <div className="info-row">
                <span className="label">Conductor:</span>
                <strong>{truck.driver_name}</strong>
              </div>
              <div className="info-row">
                <span className="label">Capacidad:</span>
                <strong>{truck.capacity}</strong>
              </div>
              <div className="info-row">
                <span className="label">Carga:</span>
                <strong>{truck.current_load}</strong>
              </div>
              <div className="info-row">
                <span className="label">Combustible:</span>
                <div className="fuel-bar">
                  <div className="fuel-level" style={{ width: `${truck.fuel}%` }}></div>
                </div>
                <span>{truck.fuel}%</span>
              </div>
            </div>

            {/* Alerts */}
            {needsMaintenance(truck.next_maintenance) && (
              <div className="alert maintenance">
                <AlertCircle size={16} />
                Mantenimiento próximo
              </div>
            )}
            {insuranceExpiring(truck.insurance_expiry) && (
              <div className="alert insurance">
                <AlertCircle size={16} />
                Seguro por vencer
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Truck Details Panel */}
      {selectedTruck && (
        <div className="truck-detail-panel">
          <div className="detail-header">
            <h3>Detalles del Camión {selectedTruck.plate}</h3>
          </div>

          <div className="details-grid">
            {/* Driver Info */}
            <div className="detail-section">
              <h4>Información del Conductor</h4>
              <div className="info-box">
                <div className="info-row">
                  <span className="label">Nombre:</span>
                  <strong>{selectedTruck.driver_name}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Teléfono:</span>
                  <a href={`tel:${selectedTruck.driver_phone}`}>{selectedTruck.driver_phone}</a>
                </div>
              </div>
            </div>

            {/* Capacity & Load */}
            <div className="detail-section">
              <h4>Capacidad de Carga</h4>
              <div className="info-box">
                <div className="info-row">
                  <span className="label">Capacidad Total:</span>
                  <strong>{selectedTruck.capacity}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Carga Actual:</span>
                  <strong>{selectedTruck.current_load}</strong>
                </div>
              </div>
            </div>

            {/* Maintenance */}
            <div className="detail-section">
              <h4>Mantenimiento</h4>
              <div className="info-box">
                <div className="info-row">
                  <span className="label">Último:</span>
                  <strong>{selectedTruck.last_maintenance}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Próximo:</span>
                  <strong>{selectedTruck.next_maintenance}</strong>
                </div>
              </div>
            </div>

            {/* Insurance */}
            <div className="detail-section">
              <h4>Seguro</h4>
              <div className="info-box">
                <div className="info-row">
                  <span className="label">Vencimiento:</span>
                  <strong>{selectedTruck.insurance_expiry}</strong>
                </div>
              </div>
            </div>

            {/* Fuel */}
            <div className="detail-section">
              <h4>Combustible</h4>
              <div className="fuel-display">
                <div className="fuel-indicator">
                  <div className="fuel-circle" style={{ width: `${selectedTruck.fuel}%`, height: `${selectedTruck.fuel}%` }}></div>
                </div>
                <span className="percentage">{selectedTruck.fuel}%</span>
              </div>
            </div>

            {/* Status */}
            <div className="detail-section">
              <h4>Estado Operacional</h4>
              <div className="status-display">
                <div className="status-card" style={{ borderColor: getStatusColor(selectedTruck.status) }}>
                  <CheckCircle size={24} color={getStatusColor(selectedTruck.status)} />
                  <span>{selectedTruck.status}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

import { useState } from 'react'
import { pendingDonations, donationStatuses, donationConditions, priorityLevels } from '../../data/donations-pending'
import { Package, Edit2, Trash2, AlertCircle } from 'lucide-react'
import '../../styles/pages/manage-donations.css'

export function ManageDonations() {
  const [donations, setDonations] = useState(pendingDonations)
  const [selectedDonation, setSelectedDonation] = useState(null)
  const [filter, setFilter] = useState('all')

  const filteredDonations = filter === 'all' 
    ? donations
    : donations.filter(d => d.status === filter)

  const handleUpdateStatus = (id, newStatus) => {
    setDonations(donations.map(d => 
      d.id === id ? { ...d, status: newStatus } : d
    ))
    setSelectedDonation(null)
  }

  const handleMarkAsExpired = (id) => {
    setDonations(donations.map(d => 
      d.id === id ? { ...d, status: 'Vencida' } : d
    ))
  }

  const getStatusColor = (status) => {
    const colors = {
      'Pendiente': '#3498db',
      'Asignada': '#2ecc71',
      'Revisión': '#f39c12',
      'Vencida': '#e74c3c'
    }
    return colors[status] || '#95a5a6'
  }

  const isExpiring = (expirationDate) => {
    if (!expirationDate) return false
    const today = new Date()
    const expireDate = new Date(expirationDate)
    const daysLeft = Math.floor((expireDate - today) / (1000 * 60 * 60 * 24))
    return daysLeft <= 30 && daysLeft > 0
  }

  return (
    <div className="manage-donations-view">
      <div className="section-header">
        <h2>Gestionar Donaciones</h2>
        <p>Control de donaciones en depósito y actualizaciones de estado</p>
      </div>

      {/* Filter Tabs */}
      <div className="filter-tabs">
        <button className={`filter-tab ${filter === 'all' ? 'active' : ''}`} onClick={() => setFilter('all')}>
          Todas ({donations.length})
        </button>
        {donationStatuses.map(status => (
          <button
            key={status}
            className={`filter-tab ${filter === status ? 'active' : ''}`}
            onClick={() => setFilter(status)}
          >
            {status} ({donations.filter(d => d.status === status).length})
          </button>
        ))}
      </div>

      <div className="donations-layout">
        {/* Donations List */}
        <div className="donations-list-section">
          <div className="list-header">
            <h3>Donaciones en Depósito</h3>
          </div>

          <div className="donations-list-items">
            {filteredDonations.map(donation => (
              <div
                key={donation.id}
                className={`donation-list-item ${selectedDonation?.id === donation.id ? 'selected' : ''} ${isExpiring(donation.expiration_date) ? 'expiring' : ''}`}
                onClick={() => setSelectedDonation(donation)}
              >
                {isExpiring(donation.expiration_date) && (
                  <div className="warning-icon">
                    <AlertCircle size={16} />
                  </div>
                )}
                <div className="item-header">
                  <h4>{donation.title}</h4>
                  <span className="status-badge" style={{ backgroundColor: getStatusColor(donation.status) }}>
                    {donation.status}
                  </span>
                </div>
                <div className="item-meta">
                  <span>{donation.donor_name}</span>
                  <span>•</span>
                  <span>{donation.items.length} tipos de items</span>
                </div>
                <small>{donation.received_date}</small>
              </div>
            ))}
          </div>
        </div>

        {/* Donation Details */}
        {selectedDonation && (
          <div className="donation-detail-panel">
            <div className="detail-header">
              <h3>{selectedDonation.title}</h3>
              <span className="priority-badge" style={{ background: 
                selectedDonation.priority_level === 'Crítica' ? '#e74c3c' :
                selectedDonation.priority_level === 'Alta' ? '#f39c12' :
                selectedDonation.priority_level === 'Media' ? '#3498db' : '#95a5a6'
              }}>
                {selectedDonation.priority_level}
              </span>
            </div>

            {/* Donor Info */}
            <div className="detail-section">
              <h4>Información del Donante</h4>
              <div className="info-box">
                <div className="info-row">
                  <span className="label">Nombre:</span>
                  <strong>{selectedDonation.donor_name}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Donante ID:</span>
                  <strong>{selectedDonation.donor_id}</strong>
                </div>
              </div>
            </div>

            {/* Items */}
            <div className="detail-section">
              <h4>Items ({selectedDonation.items.length})</h4>
              <div className="items-list">
                {selectedDonation.items.map((item, idx) => (
                  <div key={idx} className="item-detail">
                    <span>{item.name}</span>
                    <span className="quantity">{item.quantity} {item.unit}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Storage & Condition */}
            <div className="detail-section">
              <h4>Almacenamiento</h4>
              <div className="info-box">
                <div className="info-row">
                  <span className="label">Ubicación:</span>
                  <strong>{selectedDonation.storage_location}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Condición:</span>
                  <span className="condition-badge">{selectedDonation.condition}</span>
                </div>
                <div className="info-row">
                  <span className="label">Recibida:</span>
                  <strong>{selectedDonation.received_date}</strong>
                </div>
                {selectedDonation.expiration_date && (
                  <div className="info-row">
                    <span className="label">Vencimiento:</span>
                    <strong style={{ color: isExpiring(selectedDonation.expiration_date) ? '#e74c3c' : '#333' }}>
                      {selectedDonation.expiration_date}
                    </strong>
                  </div>
                )}
              </div>
            </div>

            {/* Status Update */}
            <div className="detail-section">
              <h4>Actualizar Estado</h4>
              <div className="status-buttons">
                {donationStatuses.map(status => (
                  <button
                    key={status}
                    className={`status-btn ${selectedDonation.status === status ? 'active' : ''}`}
                    onClick={() => handleUpdateStatus(selectedDonation.id, status)}
                  >
                    {status}
                  </button>
                ))}
              </div>
              {selectedDonation.expiration_date && isExpiring(selectedDonation.expiration_date) && (
                <button 
                  className="btn-mark-expired"
                  onClick={() => handleMarkAsExpired(selectedDonation.id)}
                >
                  <AlertCircle size={18} />
                  Marcar como Vencida
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

import { useState } from 'react'
import { beneficiaryAssignedDonations } from '../../data/beneficiary-donations'
import { Eye, MapPin, Clock } from 'lucide-react'
import '../../styles/pages/donation-status.css'

export function DonationStatus() {
  const [selectedDonation, setSelectedDonation] = useState(null)
  const [filter, setFilter] = useState('all')

  const filteredDonations = filter === 'all' 
    ? beneficiaryAssignedDonations
    : beneficiaryAssignedDonations.filter(d => d.status === filter)

  const statuses = [...new Set(beneficiaryAssignedDonations.map(d => d.status))]

  const getStatusColor = (status) => {
    const colors = {
      'Asignada': '#3498db',
      'Confirmada': '#2ecc71',
      'En tránsito': '#f39c12',
      'Entregada': '#27ae60',
      'Rechazada': '#e74c3c'
    }
    return colors[status] || '#95a5a6'
  }

  return (
    <div className="donation-status-view">
      <div className="section-header">
        <h2>Estado de Donaciones</h2>
        <p>Visualiza el estado de todas las donaciones asignadas a tu entidad</p>
      </div>

      {/* Filter Tabs */}
      <div className="status-filter-tabs">
        <button
          className={`filter-tab ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          Todas ({beneficiaryAssignedDonations.length})
        </button>
        {statuses.map(status => (
          <button
            key={status}
            className={`filter-tab ${filter === status ? 'active' : ''}`}
            onClick={() => setFilter(status)}
          >
            {status} ({beneficiaryAssignedDonations.filter(d => d.status === status).length})
          </button>
        ))}
      </div>

      <div className="donations-layout">
        {/* Donations List */}
        <div className="donations-list-section">
          <div className="list-header">
            <h3>Donaciones Asignadas</h3>
            <span className="count">{filteredDonations.length} donaciones</span>
          </div>

          <div className="donations-list-items">
            {filteredDonations.length > 0 ? (
              filteredDonations.map(donation => (
                <div
                  key={donation.id}
                  className={`donation-list-item ${selectedDonation?.id === donation.id ? 'selected' : ''}`}
                  onClick={() => setSelectedDonation(donation)}
                >
                  <div className="item-header">
                    <h4>{donation.title}</h4>
                    <span
                      className="status-badge"
                      style={{ backgroundColor: getStatusColor(donation.status) }}
                    >
                      {donation.status}
                    </span>
                  </div>
                  <div className="item-meta">
                    <span className="donor">{donation.donor_name}</span>
                    <span className="category">{donation.donor_category}</span>
                  </div>
                  <div className="item-details">
                    <span>{donation.items.length} items</span>
                    <span>•</span>
                    <span>{donation.expected_delivery}</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <p>No hay donaciones con este estado</p>
              </div>
            )}
          </div>
        </div>

        {/* Donation Details */}
        {selectedDonation && (
          <div className="donation-detail-panel">
            <div className="detail-header">
              <h3>{selectedDonation.title}</h3>
              <span
                className="status-badge-large"
                style={{ backgroundColor: getStatusColor(selectedDonation.status) }}
              >
                {selectedDonation.status}
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
                  <span className="label">Categoría:</span>
                  <span className="donor-category">{selectedDonation.donor_category}</span>
                </div>
              </div>
            </div>

            {/* Items */}
            <div className="detail-section">
              <h4>Items de la Donación</h4>
              <div className="items-list">
                {selectedDonation.items.map((item, idx) => (
                  <div key={idx} className="item-detail">
                    <span className="item-name">{item.name}</span>
                    <span className="item-quantity">{item.quantity}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Truck Info */}
            {selectedDonation.delivery_truck && (
              <div className="detail-section">
                <h4>Información del Transporte</h4>
                <div className="info-box">
                  <div className="info-row">
                    <span className="label">Placa:</span>
                    <strong>{selectedDonation.delivery_truck.plate}</strong>
                  </div>
                  <div className="info-row">
                    <span className="label">Conductor:</span>
                    <strong>{selectedDonation.delivery_truck.driver}</strong>
                  </div>
                  <div className="info-row">
                    <span className="label">Teléfono:</span>
                    <a href={`tel:${selectedDonation.delivery_truck.phone}`}>
                      {selectedDonation.delivery_truck.phone}
                    </a>
                  </div>
                </div>
              </div>
            )}

            {/* Delivery Info */}
            <div className="detail-section">
              <h4>Entrega</h4>
              <div className="info-box">
                <div className="info-row">
                  <Clock size={16} />
                  <span>Asignada: {selectedDonation.assigned_date}</span>
                </div>
                <div className="info-row">
                  <Clock size={16} />
                  <span>Entrega esperada: {selectedDonation.expected_delivery}</span>
                </div>
                <div className="info-row">
                  <MapPin size={16} />
                  <span>{selectedDonation.location.address}</span>
                </div>
              </div>
            </div>

            {/* Impact */}
            <div className="detail-section impact-section">
              <div className="impact-badge">
                <span>💚 {selectedDonation.impact}</span>
              </div>
            </div>

            {/* Received Info */}
            {selectedDonation.status === 'Entregada' && (
              <div className="detail-section">
                <h4>Confirmación de Recepción</h4>
                <div className="info-box">
                  <div className="info-row">
                    <span className="label">Recibida por:</span>
                    <strong>{selectedDonation.received_by}</strong>
                  </div>
                  <div className="info-row">
                    <span className="label">Fecha:</span>
                    <strong>{selectedDonation.delivery_date}</strong>
                  </div>
                </div>
              </div>
            )}

            <button className="btn-view-map">
              <Eye size={16} />
              Ver en Mapa
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

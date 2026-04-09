import { useState } from 'react'
import { beneficiaryAssignedDonations } from '../data/beneficiary-donations'
import { MapPin, TrendingUp, Clock, Phone } from 'lucide-react'
import '../styles/pages/beneficiary-deliveries.css'

export function BeneficiaryDeliveries() {
  const [selectedDelivery, setSelectedDelivery] = useState(null)
  
  const activeDeliveries = beneficiaryAssignedDonations.filter(d => 
    d.status === 'En tránsito' || d.status === 'Confirmada'
  )

  const calculateDistance = (truck, destination) => {
    const R = 6371
    const dLat = (destination.lat - truck.location.lat) * Math.PI / 180
    const dLon = (destination.lng - truck.location.lng) * Math.PI / 180
    const a = 
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(truck.location.lat * Math.PI / 180) * 
      Math.cos(destination.lat * Math.PI / 180) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (R * c).toFixed(1)
  }

  return (
    <div className="beneficiary-deliveries-view">
      <div className="section-header">
        <h2>Seguimiento de Entregas</h2>
        <p>Visualiza el recorrido de las donaciones que vas a recibir</p>
      </div>

      <div className="deliveries-layout">
        {/* Map Section */}
        <div className="map-section">
          <div className="map-container">
            <div className="map-header">
              <h4>Mapa de Entregas Activas</h4>
              <span className="active-count">{activeDeliveries.length} activas</span>
            </div>
            <div className="map-content">
              <div className="map-legend">
                <h5>Leyenda</h5>
                <div className="legend-item">
                  <div className="legend-icon" style={{ background: '#E74C3C' }}></div>
                  <span>Camión en ruta</span>
                </div>
                <div className="legend-item">
                  <div className="legend-icon" style={{ background: '#2ECC71' }}></div>
                  <span>Tu entidad (destino)</span>
                </div>
              </div>
              <p style={{ textAlign: 'center', color: '#999', marginTop: '50px' }}>
                Integrará Google Maps para mostrar ubicaciones en tiempo real
              </p>
            </div>
          </div>
        </div>

        {/* Deliveries Panel */}
        <div className="deliveries-panel">
          <div className="deliveries-list-header">
            <h3>Entregas en Progreso</h3>
            <div className="delivery-filters">
              <button className="filter-btn active">En ruta</button>
              <button className="filter-btn">Confirmadas</button>
            </div>
          </div>

          <div className="deliveries-list">
            {activeDeliveries.length > 0 ? (
              activeDeliveries.map(delivery => (
                <div 
                  key={delivery.id}
                  className={`delivery-item ${selectedDelivery?.id === delivery.id ? 'selected' : ''}`}
                  onClick={() => setSelectedDelivery(delivery)}
                >
                  <div className="delivery-status-indicator" style={{ 
                    backgroundColor: delivery.status === 'En tránsito' ? '#E74C3C' : '#F39C12'
                  }}></div>
                  
                  <div className="delivery-item-content">
                    <h4>{delivery.title}</h4>
                    <p className="delivery-donor">{delivery.donor_name}</p>
                    <small>{delivery.location.address}</small>
                  </div>

                  <span className={`delivery-status status-${delivery.status.toLowerCase().replace(/\s/g, '-')}`}>
                    {delivery.status}
                  </span>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <p>No hay entregas en progreso</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Selected Delivery Details */}
      {selectedDelivery && (
        <div className="delivery-details-card">
          <h3>{selectedDelivery.title}</h3>
          
          <div className="details-grid">
            {/* Donation Info */}
            <div className="detail-section">
              <h4>Información de la Donación</h4>
              <div className="donor-info">
                <div className="info-row">
                  <span className="label">Donante:</span>
                  <strong>{selectedDelivery.donor_name}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Categoría:</span>
                  <span className="donor-category">{selectedDelivery.donor_category}</span>
                </div>
                <div className="items-preview">
                  <strong>Items:</strong>
                  {selectedDelivery.items.map((item, idx) => (
                    <div key={idx} className="item-badge">
                      {item.name} - {item.quantity}
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Truck Info */}
            <div className="detail-section">
              <h4>Camión en Ruta</h4>
              <div className="truck-info">
                <div className="info-row">
                  <span className="label">Placa:</span>
                  <strong>{selectedDelivery.delivery_truck.plate}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Conductor:</span>
                  <strong>{selectedDelivery.delivery_truck.driver}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Teléfono:</span>
                  <a href={`tel:${selectedDelivery.delivery_truck.phone}`}>
                    <Phone size={14} />
                    {selectedDelivery.delivery_truck.phone}
                  </a>
                </div>
                <div className="info-row">
                  <span className="label">Ubicación:</span>
                  <span>{selectedDelivery.delivery_truck.location.address}</span>
                </div>
              </div>
            </div>

            {/* Journey Info */}
            <div className="detail-section">
              <h4>Ruta</h4>
              <div className="journey-info">
                <div className="journey-point">
                  <div className="point-indicator" style={{ background: '#E74C3C' }}></div>
                  <div>
                    <strong>Ubicación Actual del Camión</strong>
                    <p>{selectedDelivery.delivery_truck.location.address}</p>
                  </div>
                </div>
                <div className="journey-arrow">▼</div>
                <div className="journey-point">
                  <div className="point-indicator" style={{ background: '#2ECC71' }}></div>
                  <div>
                    <strong>Tu Entidad (Destino)</strong>
                    <p>{selectedDelivery.location.address}</p>
                  </div>
                </div>
              </div>
              <div className="distance-info">
                <MapPin size={16} />
                <span>Distancia: {calculateDistance(selectedDelivery.delivery_truck, selectedDelivery.location)} km</span>
              </div>
              <div className="eta-info">
                <Clock size={16} />
                <span>Tiempo estimado: {selectedDelivery.expected_delivery}</span>
              </div>
            </div>

            {/* Impact */}
            <div className="detail-section impact-section">
              <h4>Impacto Social</h4>
              <div className="impact-badge">
                <TrendingUp size={24} />
                <span>{selectedDelivery.impact}</span>
              </div>
            </div>
          </div>

          <div className="action-buttons">
            <button className="btn-contact">
              <Phone size={18} />
              Contactar Conductor
            </button>
            <button className="btn-location">
              <MapPin size={18} />
              Ver Ubicación Exacta
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

import { useState } from 'react'
import { beneficiaryAssignedDonations } from '../../data/beneficiary-donations'
import { donations as allDonations } from '../../data/donations.js'
import { MapPin, TrendingUp, Clock, Phone } from 'lucide-react'
import { InteractiveMap } from '../../components/map/InteractiveMap.jsx'
import '../../styles/pages/beneficiary-deliveries.css'

export function BeneficiaryDeliveries() {
  const [selectedDelivery, setSelectedDelivery] = useState(null)
  
  const activeDeliveries = beneficiaryAssignedDonations.filter(d => 
    d.status === 'En tránsito' || d.status === 'Confirmada'
  )

  const mapViewport = {
    lat: -34.6037,
    lng: -58.3816,
    zoom: 12,
  }

  const mapDonations = activeDeliveries.map(delivery => {
    const matchingDonation = allDonations.find(d => d.id === delivery.donation_id)
    return {
      ...matchingDonation,
      id: delivery.id,
      title: delivery.title,
      location: delivery.location,
      status: delivery.status,
    }
  })

  const handleSelectDonation = (deliveryId) => {
    const delivery = activeDeliveries.find(d => d.id === deliveryId)
    setSelectedDelivery(delivery)
  }

  const calculateDistance = (truck, destination) => {
    // Validar que existan los datos necesarios
    if (!truck?.location?.lat || !truck?.location?.lng || !destination?.lat || !destination?.lng) {
      return 'N/A'
    }

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
    <div className="deliveries-view">
      <div className="section-header">
        <h2>Seguimiento de Entregas</h2>
        <p>Visualiza el recorrido de las donaciones que vas a recibir</p>
      </div>

      <div className="deliveries-layout">
        {/* Map Section */}
        <div className="map-section">
          <div className="map-container">
            <InteractiveMap
              donations={mapDonations}
              viewport={mapViewport}
              onSelectDonation={handleSelectDonation}
            />
          </div>
        </div>

        {/* Deliveries Panel */}
        <div className="deliveries-panel">
          <div className="panel-header">
            <h3>Entregas en Progreso</h3>
            <span className="delivery-count">{activeDeliveries.length}</span>
          </div>

          <div className="deliveries-list">
            {activeDeliveries.length > 0 ? (
              activeDeliveries.map(delivery => (
                <div
                  key={delivery.id}
                  className={`delivery-item ${selectedDelivery?.id === delivery.id ? 'selected' : ''}`}
                  onClick={() => setSelectedDelivery(delivery)}
                >
                  <div className="delivery-header">
                    <h4 className="delivery-id">{delivery.title}</h4>
                    <span
                      className={`status-badge status-${delivery.status.toLowerCase().replace(/\s/g, '-')}`}
                    >
                      {delivery.status}
                    </span>
                  </div>
                  <div className="delivery-info">
                    <span className="info-tag">{delivery.donor_name}</span>
                    <span className="info-tag">{delivery.items.length} items</span>
                    <span className="info-tag distance">
                      {calculateDistance(delivery.delivery_truck, delivery.location)} km
                    </span>
                  </div>
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
        <div className="delivery-detail-panel">
          <div className="detail-panel-header">
            <h3>{selectedDelivery.title}</h3>
          </div>

          {/* Donation Info */}
          <div className="detail-section">
            <h4>Información de la Donación</h4>
            <div className="detail-info">
              <div className="info-item">
                <span className="label">Donante:</span>
                <strong>{selectedDelivery.donor_name}</strong>
              </div>
              <div className="info-item">
                <span className="label">Categoría:</span>
                <strong>{selectedDelivery.donor_category}</strong>
              </div>
              <div className="info-item">
                <span className="label">Items:</span>
              </div>
              <div style={{ marginLeft: '1rem' }}>
                {selectedDelivery.items.map((item, idx) => (
                  <div key={idx} className="info-item" style={{ marginTop: '0.25rem' }}>
                    <span>{item.name}</span>
                    <span>{item.quantity}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Truck Info */}
          <div className="detail-section">
            <h4>Camión en Ruta</h4>
            <div className="detail-info">
              <div className="info-item">
                <span className="label">Placa:</span>
                <strong>{selectedDelivery.delivery_truck.plate}</strong>
              </div>
              <div className="info-item">
                <span className="label">Conductor:</span>
                <strong>{selectedDelivery.delivery_truck.driver}</strong>
              </div>
              <div className="info-item">
                <span className="label">Teléfono:</span>
                <a href={`tel:${selectedDelivery.delivery_truck.phone}`} className="contact-link">
                  {selectedDelivery.delivery_truck.phone}
                </a>
              </div>
              <div className="info-item">
                <span className="label">Ubicación:</span>
                <span>{selectedDelivery.delivery_truck.location.address}</span>
              </div>
            </div>
          </div>

          {/* Journey Info */}
          <div className="detail-section">
            <h4>Ruta</h4>
            <div className="progress-steps">
              <div className={`step ${selectedDelivery.status === 'Confirmada' ? 'completed' : ''}`}>
                <div className="step-dot"></div>
                <span>Confirmada</span>
              </div>
              <div className={`step ${selectedDelivery.status === 'En tránsito' ? 'current' : selectedDelivery.status === 'Entregada' ? 'completed' : ''}`}>
                <div className="step-dot"></div>
                <span>En tránsito</span>
              </div>
              <div className={`step ${selectedDelivery.status === 'Entregada' ? 'completed' : ''}`}>
                <div className="step-dot"></div>
                <span>Entregada</span>
              </div>
            </div>

            <div className="location-info" style={{ marginTop: '1rem' }}>
              <strong>📍 Ubicación del Camión</strong>
              <p>{selectedDelivery.delivery_truck.location.address}</p>
            </div>

            <div className="location-info" style={{ marginTop: '1rem' }}>
              <strong>📍 Tu Entidad (Destino)</strong>
              <p>{selectedDelivery.location.address}</p>
            </div>

            <div className="info-item" style={{ marginTop: '1rem' }}>
              <MapPin size={16} />
              <span><strong>Distancia:</strong> {calculateDistance(selectedDelivery.delivery_truck, selectedDelivery.location)} km</span>
            </div>

            <div className="info-item" style={{ marginTop: '0.5rem' }}>
              <Clock size={16} />
              <span><strong>Entrega esperada:</strong> {selectedDelivery.expected_delivery}</span>
            </div>
          </div>

          {/* Impact */}
          <div className="detail-section">
            <h4>Impacto Social</h4>
            <div className="impact-badge">
              <TrendingUp size={20} />
              <strong>{selectedDelivery.impact}</strong>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

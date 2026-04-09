import { useState } from 'react'
import { MapPin, TrendingUp, Clock, Phone } from 'lucide-react'
import { activeDeliveries } from '../data/deliveries'
import '../styles/pages/active-deliveries.css'

export function ActiveDeliveries() {
  const [selectedDelivery, setSelectedDelivery] = useState(null)
  const [mapZoom, setMapZoom] = useState(13)

  const calculateDistance = (truck, destination) => {
    // Fórmula simplificada para calcular distancia (haversine)
    const R = 6371 // Radio de la Tierra en km
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
    <div className="active-deliveries-view">
      <div className="section-header">
        <h2>Seguimiento de Entregas</h2>
        <p>Visualiza el recorrido de tus donaciones en tiempo real</p>
      </div>

      <div className="deliveries-layout">
        {/* Map Section */}
        <div className="map-section">
          <div className="map-container">
            {/* Mapa interactivo simulado */}
            <div className="map-placeholder">
              <div className="map-header">
                <h4>Mapa de Entregas Activas</h4>
                <div className="map-controls">
                  <button onClick={() => setMapZoom(Math.min(mapZoom + 1, 20))}>+</button>
                  <span>{mapZoom}</span>
                  <button onClick={() => setMapZoom(Math.max(mapZoom - 1, 5))}>−</button>
                </div>
              </div>
              <div className="map-content" style={{ background: '#f0f4f8' }}>
                <div className="map-legend">
                  <h5>Leyenda</h5>
                  <div className="legend-item">
                    <div className="legend-icon" style={{ background: '#E74C3C' }}></div>
                    <span>Camión activo</span>
                  </div>
                  <div className="legend-item">
                    <div className="legend-icon" style={{ background: '#F39C12' }}></div>
                    <span>Punto de entrega</span>
                  </div>
                  <div className="legend-item">
                    <div className="legend-icon" style={{ background: '#2ECC71' }}></div>
                    <span>Destino</span>
                  </div>
                </div>
                <p style={{ textAlign: 'center', color: '#999', marginTop: '50px' }}>
                  Integrará Google Maps o Leaflet para mostrar ubicaciones en tiempo real
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Deliveries Panel */}
        <div className="deliveries-panel">
          <div className="deliveries-list-header">
            <h3>Entregas en Progreso ({activeDeliveries.length})</h3>
            <div className="delivery-filters">
              <button className="filter-btn active">Activas</button>
              <button className="filter-btn">Completadas</button>
            </div>
          </div>

          <div className="deliveries-list">
            {activeDeliveries.map(delivery => (
              <div 
                key={delivery.id}
                className={`delivery-item ${selectedDelivery?.id === delivery.id ? 'selected' : ''}`}
                onClick={() => setSelectedDelivery(delivery)}
              >
                <div className="delivery-status-indicator" style={{ 
                  backgroundColor: delivery.status === 'En tránsito' ? '#E74C3C' : '#F39C12' 
                }}></div>
                
                <div className="delivery-item-content">
                  <h4>{delivery.donationTitle}</h4>
                  <p className="delivery-entity">{delivery.destination.address}</p>
                  <div className="delivery-progress">
                    <div className="progress-bar">
                      <div className="progress-fill" style={{ width: `${delivery.progress}%` }}></div>
                    </div>
                    <span className="progress-percent">{delivery.progress}%</span>
                  </div>
                </div>

                <div className="delivery-time">
                  <Clock size={16} />
                  <span>{delivery.estimatedTime}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Selected Delivery Details */}
      {selectedDelivery && (
        <div className="delivery-details-card">
          <h3>{selectedDelivery.donationTitle}</h3>
          
          <div className="details-grid">
            {/* Truck Info */}
            <div className="detail-section">
              <h4>Camión en Ruta</h4>
              <div className="truck-info">
                <div className="info-row">
                  <span className="label">Placa:</span>
                  <strong>{selectedDelivery.truck.plate}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Conductor:</span>
                  <strong>{selectedDelivery.truck.driver}</strong>
                </div>
                <div className="info-row">
                  <span className="label">Teléfono:</span>
                  <a href={`tel:${selectedDelivery.truck.phone}`}>{selectedDelivery.truck.phone}</a>
                </div>
                <div className="info-row">
                  <span className="label">Ubicación:</span>
                  <span>{selectedDelivery.truck.location.address}</span>
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
                    <strong>Ubicación Actual</strong>
                    <p>{selectedDelivery.truck.location.address}</p>
                  </div>
                </div>
                <div className="journey-arrow">→</div>
                <div className="journey-point">
                  <div className="point-indicator" style={{ background: '#2ECC71' }}></div>
                  <div>
                    <strong>Destino</strong>
                    <p>{selectedDelivery.destination.address}</p>
                  </div>
                </div>
              </div>
              <div className="distance-info">
                <MapPin size={16} />
                <span>Distancia: {calculateDistance(selectedDelivery.truck, selectedDelivery.destination)} km</span>
              </div>
              <div className="eta-info">
                <Clock size={16} />
                <span>Tiempo estimado: {selectedDelivery.estimatedTime}</span>
              </div>
            </div>

            {/* Delivery Status */}
            <div className="detail-section">
              <h4>Estado de Entrega</h4>
              <div className="status-timeline">
                <div className="timeline-item completed">
                  <div className="timeline-dot">✓</div>
                  <div className="timeline-content">
                    <strong>Donación Confirmada</strong>
                    <small>2025-10-15 14:00</small>
                  </div>
                </div>
                <div className="timeline-item completed">
                  <div className="timeline-dot">✓</div>
                  <div className="timeline-content">
                    <strong>En Tránsito</strong>
                    <small>2025-10-15 14:30</small>
                  </div>
                </div>
                <div className="timeline-item active">
                  <div className="timeline-dot">📍</div>
                  <div className="timeline-content">
                    <strong>En Camino</strong>
                    <small>{selectedDelivery.lastUpdate}</small>
                  </div>
                </div>
                <div className="timeline-item pending">
                  <div className="timeline-dot"></div>
                  <div className="timeline-content">
                    <strong>Entrega Esperada</strong>
                    <small>{selectedDelivery.estimatedTime}</small>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <button className="btn-contact-driver">
            <Phone size={18} />
            Contactar Conductor
          </button>
        </div>
      )}
    </div>
  )
}

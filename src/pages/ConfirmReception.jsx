import { useState } from 'react'
import { beneficiaryAssignedDonations } from '../data/beneficiary-donations'
import { Upload, X, Check } from 'lucide-react'
import '../styles/pages/confirm-reception.css'

export function ConfirmReception() {
  const [selectedDonation, setSelectedDonation] = useState(null)
  const [uploadedPhotos, setUploadedPhotos] = useState([])
  const [notes, setNotes] = useState('')
  const [confirmedDonations, setConfirmedDonations] = useState([])

  const pendingDonations = beneficiaryAssignedDonations.filter(
    d => (d.status === 'En tránsito' || d.status === 'Confirmada') && !confirmedDonations.includes(d.id)
  )

  const handlePhotoUpload = (e) => {
    const files = Array.from(e.target.files)
    files.forEach(file => {
      const reader = new FileReader()
      reader.onload = (event) => {
        setUploadedPhotos([...uploadedPhotos, {
          id: Date.now() + Math.random(),
          preview: event.target.result,
          name: file.name
        }])
      }
      reader.readAsDataURL(file)
    })
  }

  const handleRemovePhoto = (id) => {
    setUploadedPhotos(uploadedPhotos.filter(p => p.id !== id))
  }

  const handleConfirmReception = () => {
    if (!selectedDonation) {
      alert('Selecciona una donación')
      return
    }
    if (uploadedPhotos.length === 0) {
      alert('Debes cargar al menos una foto')
      return
    }

    setConfirmedDonations([...confirmedDonations, selectedDonation.id])
    setUploadedPhotos([])
    setNotes('')
    setSelectedDonation(null)
    alert(`✅ Recepción confirmada para: ${selectedDonation.title}`)
  }

  return (
    <div className="confirm-reception-view">
      <div className="section-header">
        <h2>Confirmar Recepción de Donaciones</h2>
        <p>Carga fotos de las donaciones recibidas para confirmar su entrega</p>
      </div>

      <div className="confirm-layout">
        {/* Donations List */}
        <div className="pending-donations">
          <div className="list-header">
            <h3>Donaciones Pendientes de Confirmar</h3>
            <span className="badge">{pendingDonations.length}</span>
          </div>

          <div className="donations-list">
            {pendingDonations.length > 0 ? (
              pendingDonations.map(donation => (
                <div
                  key={donation.id}
                  className={`donation-item ${selectedDonation?.id === donation.id ? 'selected' : ''}`}
                  onClick={() => setSelectedDonation(donation)}
                >
                  <div className="item-selector">
                    <div className="checkbox" />
                  </div>
                  <div className="item-content">
                    <h4>{donation.title}</h4>
                    <p>{donation.donor_name} • {donation.items.length} items</p>
                    <small>Esperada: {donation.expected_delivery}</small>
                  </div>
                  <span className={`status status-${donation.status.toLowerCase().replace(/\s/g, '-')}`}>
                    {donation.status}
                  </span>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <Check size={32} />
                <p>¡Todas las donaciones han sido confirmadas!</p>
              </div>
            )}
          </div>
        </div>

        {/* Confirmation Form */}
        {selectedDonation && (
          <div className="confirmation-form">
            <div className="form-header">
              <h3>{selectedDonation.title}</h3>
              <button className="close-btn" onClick={() => setSelectedDonation(null)}>×</button>
            </div>

            {/* Donation Details */}
            <div className="form-section">
              <h4>Detalles de la Donación</h4>
              <div className="details-info">
                <div className="info-item">
                  <span className="label">De:</span>
                  <span>{selectedDonation.donor_name}</span>
                </div>
                <div className="info-item">
                  <span className="label">Items:</span>
                  <div className="items-list">
                    {selectedDonation.items.map((item, idx) => (
                      <div key={idx} className="item">
                        {item.name} - {item.quantity}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Photo Upload */}
            <div className="form-section upload-section">
              <h4>Cargar Fotos de la Donación</h4>
              <p className="help-text">Sube mínimo 1 foto mostrando los productos recibidos</p>

              <div className="upload-area">
                <input
                  type="file"
                  id="photo-upload"
                  multiple
                  accept="image/*"
                  onChange={handlePhotoUpload}
                  style={{ display: 'none' }}
                />
                <label htmlFor="photo-upload" className="upload-label">
                  <Upload size={32} />
                  <strong>Click para subir fotos</strong>
                  <small>o arrastra y suelta</small>
                </label>
              </div>

              {/* Photos Preview */}
              {uploadedPhotos.length > 0 && (
                <div className="photos-grid">
                  {uploadedPhotos.map(photo => (
                    <div key={photo.id} className="photo-item">
                      <img src={photo.preview} alt={photo.name} />
                      <button
                        className="remove-photo-btn"
                        onClick={() => handleRemovePhoto(photo.id)}
                      >
                        <X size={16} />
                      </button>
                      <small>{photo.name}</small>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Notes */}
            <div className="form-section">
              <h4>Notas (Opcional)</h4>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Agrega observaciones sobre el estado de la donación..."
                rows={3}
              />
            </div>

            {/* Action Buttons */}
            <div className="form-actions">
              <button className="btn-cancel" onClick={() => setSelectedDonation(null)}>
                Cancelar
              </button>
              <button
                className="btn-confirm"
                onClick={handleConfirmReception}
                disabled={uploadedPhotos.length === 0}
              >
                <Check size={18} />
                Confirmar Recepción
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Confirmed Donations */}
      {confirmedDonations.length > 0 && (
        <div className="confirmed-section">
          <h3>Donaciones Confirmadas Hoy</h3>
          <div className="confirmed-list">
            {confirmedDonations.map(id => {
              const donation = beneficiaryAssignedDonations.find(d => d.id === id)
              return (
                <div key={id} className="confirmed-item">
                  <Check size={20} color="#2ECC71" />
                  <div>
                    <strong>{donation.title}</strong>
                    <small>{donation.donor_name}</small>
                  </div>
                  <span className="checkmark">✓</span>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

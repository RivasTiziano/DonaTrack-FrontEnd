import { useState } from 'react'
import { donations } from '../../data/donations'
import { donationCategories, donationStatuses } from '../../data/beneficiaries'
import { Package, Search, Download } from 'lucide-react'
import '../../styles/pages/donor-donations.css'

const normalizeText = (value = '') =>
  value
    .toString()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')

export function DonorDonations() {
  const [selectedStatus, setSelectedStatus] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedSubcategory, setSelectedSubcategory] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  const selectedCategoryData = selectedCategory
    ? donationCategories.find(cat => cat.id === selectedCategory)
    : null

  const filteredDonations = donations.filter(donation => {
    const searchableText = normalizeText([
      donation.title,
      donation.description,
      donation.entity_name,
      ...donation.items.map(item => `${item.name} ${item.quantity}`)
    ].join(' '))

    const normalizedSearchTerm = normalizeText(searchTerm)
    const normalizedCategoryName = normalizeText(selectedCategoryData?.name || '')
    const normalizedSubcategory = normalizeText(selectedSubcategory)

    const matchesStatus = !selectedStatus || donation.status === selectedStatus
    const matchesSearch = !normalizedSearchTerm || searchableText.includes(normalizedSearchTerm)
    const matchesCategory = !selectedCategory || searchableText.includes(normalizedCategoryName)
    const matchesSubcategory = !selectedSubcategory || searchableText.includes(normalizedSubcategory)

    return matchesStatus && matchesSearch && matchesCategory && matchesSubcategory
  })

  return (
    <div className="donor-donations-view">
      <div className="section-header">
        <h2>Mis Donaciones</h2>
        <p>Filtrar y gestionar tus donaciones realizadas</p>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-row">
          <div className="search-filter">
            <Search size={18} />
            <input
              type="text"
              placeholder="Buscar donación por nombre..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>Estado</label>
            <select value={selectedStatus} onChange={(e) => setSelectedStatus(e.target.value)}>
              <option value="">Ver Todos</option>
              {donationStatuses.map(status => (
                <option key={status} value={status}>{status}</option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label>Categoría</label>
            <select 
              value={selectedCategory} 
              onChange={(e) => {
                setSelectedCategory(e.target.value)
                setSelectedSubcategory('')
              }}
            >
              <option value="">Ver Todas</option>
              {donationCategories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>

          {selectedCategory && selectedCategoryData && (
            <div className="filter-group">
              <label>Subcategoría</label>
              <select value={selectedSubcategory} onChange={(e) => setSelectedSubcategory(e.target.value)}>
                <option value="">Ver Todas</option>
                {selectedCategoryData.subcategories.map(subcat => (
                  <option key={subcat} value={subcat}>{subcat}</option>
                ))}
              </select>
            </div>
          )}

          <button className="btn-export">
            <Download size={18} />
            Descargar Reporte
          </button>
        </div>
      </div>

      {/* Donations Table/Grid */}
      <div className="donations-grid">
        {filteredDonations.length > 0 ? (
          filteredDonations.map(donation => (
            <div key={donation.id} className="donation-card">
              <div className="donation-card-image">
                <img src={donation.image_url} alt={donation.title} />
                <span className={`status-badge status-${donation.status.toLowerCase()}`}>
                  {donation.status}
                </span>
              </div>
              <div className="donation-card-content">
                <h3>{donation.title}</h3>
                <p className="entity-name">{donation.entity_name}</p>
                
                <div className="donation-details">
                  <div className="detail-item">
                    <span className="detail-label">Entregados:</span>
                    <span className="detail-value">{donation.total_units} unidades</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Beneficiarios:</span>
                    <span className="detail-value">{donation.beneficiaries_count} personas</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Fecha:</span>
                    <span className="detail-value">{donation.delivery_date}</span>
                  </div>
                </div>

                <div className="donation-items-list">
                  <strong>Items:</strong>
                  <ul>
                    {donation.items.map((item, idx) => (
                      <li key={idx}>{item.name} - {item.quantity}</li>
                    ))}
                  </ul>
                </div>

                <div className="donation-footer">
                  <button className="btn-view-detail">Ver Detalle</button>
                  <button className="btn-track">Rastrear</button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <Package size={48} />
            <h3>No hay donaciones</h3>
            <p>No se encontraron donaciones con los filtros seleccionados</p>
          </div>
        )}
      </div>
    </div>
  )
}

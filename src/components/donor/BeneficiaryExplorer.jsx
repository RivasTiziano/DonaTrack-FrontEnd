import { useState } from 'react'
import { beneficiaryEntities } from '../../data/beneficiaries'
import { MapPin, Users, Star, ExternalLink } from 'lucide-react'
import '../../styles/pages/beneficiary-explorer.css'

export function BeneficiaryExplorer() {
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedEntity, setSelectedEntity] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')

  const categories = [...new Set(beneficiaryEntities.map(e => e.category))]

  const filteredEntities = beneficiaryEntities.filter(entity => {
    const matchesCategory = !selectedCategory || entity.category === selectedCategory
    const matchesSearch = !searchTerm || entity.name.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesCategory && matchesSearch
  })

  return (
    <div className="beneficiary-explorer">
      <div className="section-header">
        <h2>Entidades Beneficiarias</h2>
        <p>Navega por las organizaciones que reciben donaciones</p>
      </div>

      <div className="explorer-layout">
        {/* Sidebar */}
        <div className="explorer-sidebar">
          <div className="search-box">
            <input
              type="text"
              placeholder="Buscar entidad..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="categories-filter">
            <h3>Categorias</h3>
            <div className="category-list">
              <button
                className={`category-btn ${!selectedCategory ? 'active' : ''}`}
                onClick={() => setSelectedCategory('')}
              >
                Ver Todas ({beneficiaryEntities.length})
              </button>
              {categories.map(category => {
                const count = beneficiaryEntities.filter(e => e.category === category).length
                return (
                  <button
                    key={category}
                    className={`category-btn ${selectedCategory === category ? 'active' : ''}`}
                    onClick={() => setSelectedCategory(category)}
                  >
                    {category} ({count})
                  </button>
                )
              })}
            </div>
          </div>

          {selectedEntity && (
            <div className="entity-preview">
              <button className="close-btn" onClick={() => setSelectedEntity(null)}>×</button>
              <h3>{selectedEntity.name}</h3>
              <div className="verification-badge">
                {selectedEntity.verified && '✓ Verificada'}
              </div>
              <p className="entity-category">{selectedEntity.category}</p>
              <p>{selectedEntity.description}</p>
              <div className="preview-stats">
                <div className="stat">
                  <strong>{selectedEntity.contacts}</strong>
                  <small>Contactos</small>
                </div>
                <div className="stat">
                  <strong>{selectedEntity.rating}</strong>
                  <small>Rating</small>
                </div>
              </div>
              <button className="btn-visit">Ver en Mapa</button>
            </div>
          )}
        </div>

        {/* Main Content */}
        <div className="explorer-content">
          {filteredEntities.length > 0 ? (
            <div className="entities-grid">
              {filteredEntities.map(entity => (
                <div 
                  key={entity.id} 
                  className="entity-card"
                  onClick={() => setSelectedEntity(entity)}
                >
                  <div className="entity-image-wrapper">
                    <img src={entity.image} alt={entity.name} className="entity-image" />
                    {entity.verified && <div className="verified-badge">✓</div>}
                  </div>

                  <div className="entity-card-content">
                    <h3>{entity.name}</h3>
                    <p className="category-tag">{entity.category}</p>
                    <p className="description">{entity.description}</p>

                    <div className="entity-meta">
                      <div className="meta-item">
                        <Users size={16} />
                        <span>{entity.contacts} contactos</span>
                      </div>
                      <div className="meta-item">
                        <Star size={16} />
                        <span>{entity.rating} ⭐</span>
                      </div>
                    </div>

                    <div className="entity-location">
                      <MapPin size={14} />
                      <small>{entity.location.address}</small>
                    </div>

                    <button className="btn-interact">
                      Ver Más <ExternalLink size={14} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <h3>No hay entidades</h3>
              <p>No se encontraron entidades con los criterios de búsqueda</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

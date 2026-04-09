import { useState } from 'react'
import { materialNeeds, beneficiaryCategories, priorityLevels, needStatuses } from '../data/needs'
import { Plus, Edit2, Trash2, X } from 'lucide-react'
import '../styles/pages/register-needs.css'

export function RegisterNeeds() {
  const [needs, setNeeds] = useState(materialNeeds)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [filter, setFilter] = useState('Activa')
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: '',
    subcategory: '',
    quantity: '',
    unit: 'unidades',
    priority: 'Media',
    deadline: ''
  })

  const handleAddNeed = () => {
    setFormData({
      title: '',
      description: '',
      category: '',
      subcategory: '',
      quantity: '',
      unit: 'unidades',
      priority: 'Media',
      deadline: ''
    })
    setEditingId(null)
    setShowForm(true)
  }

  const handleEditNeed = (need) => {
    setFormData(need)
    setEditingId(need.id)
    setShowForm(true)
  }

  const handleDeleteNeed = (id) => {
    setNeeds(needs.filter(n => n.id !== id))
  }

  const handleSaveNeed = () => {
    if (!formData.title || !formData.category || !formData.quantity || !formData.deadline) {
      alert('Por favor completa todos los campos requeridos')
      return
    }

    if (editingId) {
      setNeeds(needs.map(n => n.id === editingId ? { ...formData, id: editingId } : n))
    } else {
      const newNeed = {
        ...formData,
        id: `need-${Date.now()}`,
        status: 'Activa',
        created_at: new Date().toISOString().split('T')[0]
      }
      setNeeds([newNeed, ...needs])
    }
    setShowForm(false)
  }

  const selectedCategory = formData.category 
    ? beneficiaryCategories.find(cat => cat.id === formData.category)
    : null

  const filteredNeeds = needs.filter(n => n.status === filter)

  return (
    <div className="register-needs-view">
      <div className="section-header">
        <div>
          <h2>Gestión de Necesidades</h2>
          <p>Registra y gestiona las necesidades de tu entidad</p>
        </div>
        <button className="btn-add-need" onClick={handleAddNeed}>
          <Plus size={20} />
          Nueva Necesidad
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="filter-tabs">
        {needStatuses.map(status => (
          <button
            key={status}
            className={`filter-tab ${filter === status ? 'active' : ''}`}
            onClick={() => setFilter(status)}
          >
            {status} ({needs.filter(n => n.status === status).length})
          </button>
        ))}
      </div>

      {/* Modal Form */}
      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingId ? 'Editar Necesidad' : 'Nueva Necesidad'}</h3>
              <button className="close-btn" onClick={() => setShowForm(false)}>
                <X size={24} />
              </button>
            </div>

            <div className="form-body">
              <div className="form-group">
                <label>Título *</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  placeholder="Ej: Alimentos no perecederos"
                />
              </div>

              <div className="form-group">
                <label>Descripción *</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Describe la necesidad en detalle"
                  rows={3}
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Categoría *</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value, subcategory: '' })}
                  >
                    <option value="">Selecciona una categoría</option>
                    {beneficiaryCategories.map(cat => (
                      <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                  </select>
                </div>

                {selectedCategory && (
                  <div className="form-group">
                    <label>Subcategoría *</label>
                    <select
                      value={formData.subcategory}
                      onChange={(e) => setFormData({ ...formData, subcategory: e.target.value })}
                    >
                      <option value="">Selecciona una subcategoría</option>
                      {selectedCategory.subcategories.map(subcat => (
                        <option key={subcat} value={subcat}>{subcat}</option>
                      ))}
                    </select>
                  </div>
                )}
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Cantidad *</label>
                  <input
                    type="number"
                    value={formData.quantity}
                    onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                    placeholder="Ej: 100"
                  />
                </div>

                <div className="form-group">
                  <label>Unidad *</label>
                  <select
                    value={formData.unit}
                    onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                  >
                    <option>unidades</option>
                    <option>kg</option>
                    <option>litros</option>
                    <option>metros</option>
                    <option>pares</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Prioridad *</label>
                  <select
                    value={formData.priority}
                    onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                  >
                    {priorityLevels.map(level => (
                      <option key={level} value={level}>{level}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label>Fecha Límite *</label>
                <input
                  type="date"
                  value={formData.deadline}
                  onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
                />
              </div>

              <div className="form-actions">
                <button className="btn-cancel" onClick={() => setShowForm(false)}>Cancelar</button>
                <button className="btn-save" onClick={handleSaveNeed}>
                  {editingId ? 'Guardar Cambios' : 'Crear Necesidad'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Needs Grid */}
      <div className="needs-management-grid">
        {filteredNeeds.length > 0 ? (
          filteredNeeds.map(need => (
            <div key={need.id} className="need-management-card">
              <img src={need.image_url} alt={need.title} className="need-card-image" />
              <div className="need-card-content">
                <h3>{need.title}</h3>
                <p className="description">{need.description}</p>
                
                <div className="need-meta-tags">
                  <span className="meta-tag category">{need.category}</span>
                  <span className={`meta-tag priority priority-${need.priority.toLowerCase()}`}>
                    {need.priority}
                  </span>
                  <span className={`meta-tag status status-${need.status.toLowerCase().replace(/\s/g, '-')}`}>
                    {need.status}
                  </span>
                </div>

                <div className="need-details">
                  <div className="detail">
                    <span className="label">Cantidad:</span>
                    <strong>{need.quantity} {need.unit}</strong>
                  </div>
                  <div className="detail">
                    <span className="label">Vence:</span>
                    <strong>{need.deadline}</strong>
                  </div>
                </div>

                <div className="need-actions">
                  <button className="btn-edit" onClick={() => handleEditNeed(need)}>
                    <Edit2 size={16} />
                    Editar
                  </button>
                  <button className="btn-delete" onClick={() => handleDeleteNeed(need.id)}>
                    <Trash2 size={16} />
                    Eliminar
                  </button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <p>No hay necesidades con este estado</p>
          </div>
        )}
      </div>
    </div>
  )
}

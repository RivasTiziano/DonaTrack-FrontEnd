import { useState } from 'react'
import { registeredDonors, donorCategories } from '../../data/donors-list'
import { Plus, Edit2, Trash2, X } from 'lucide-react'
import '../../styles/pages/register-donor.css'

export function RegisterDonor() {
  const [donors, setDonors] = useState(registeredDonors)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    category: ''
  })

  const handleAddDonor = () => {
    setFormData({ name: '', email: '', phone: '', category: '' })
    setEditingId(null)
    setShowForm(true)
  }

  const handleEditDonor = (donor) => {
    setFormData(donor)
    setEditingId(donor.id)
    setShowForm(true)
  }

  const handleDeleteDonor = (id) => {
    setDonors(donors.filter(d => d.id !== id))
  }

  const handleSaveDonor = () => {
    if (!formData.name || !formData.email || !formData.category) {
      alert('Por favor completa los campos requeridos')
      return
    }

    if (editingId) {
      setDonors(donors.map(d => d.id === editingId ? { ...d, ...formData } : d))
    } else {
      const newDonor = {
        id: `donor-${Date.now()}`,
        ...formData,
        registration_date: new Date().toISOString().split('T')[0],
        total_donations: 0,
        status: 'Activo'
      }
      setDonors([newDonor, ...donors])
    }
    setShowForm(false)
  }

  return (
    <div className="register-donor-view">
      <div className="section-header">
        <div>
          <h2>Administrar Donantes</h2>
          <p>Registra nuevos donantes y gestiona sus perfiles</p>
        </div>
        <button className="btn-add-donor" onClick={handleAddDonor}>
          <Plus size={20} />
          Nuevo Donante
        </button>
      </div>

      {/* Modal Form */}
      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingId ? 'Editar Donante' : 'Registrar Nuevo Donante'}</h3>
              <button className="close-btn" onClick={() => setShowForm(false)}>
                <X size={24} />
              </button>
            </div>

            <div className="form-body">
              <div className="form-group">
                <label>Nombre Completo *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Ej: Juan Pérez"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Email *</label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    placeholder="email@example.com"
                  />
                </div>

                <div className="form-group">
                  <label>Teléfono</label>
                  <input
                    type="tel"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    placeholder="+34 911 234567"
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Categoría *</label>
                <select
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                >
                  <option value="">Selecciona una categoría</option>
                  {donorCategories.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>

              <div className="form-actions">
                <button className="btn-cancel" onClick={() => setShowForm(false)}>Cancelar</button>
                <button className="btn-save" onClick={handleSaveDonor}>
                  {editingId ? 'Guardar Cambios' : 'Registrar Donante'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Donors Table */}
      <div className="table-container">
        <table className="donors-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Email</th>
              <th>Teléfono</th>
              <th>Categoría</th>
              <th>Donaciones</th>
              <th>Fecha Registro</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {donors.map(donor => (
              <tr key={donor.id}>
                <td><strong>{donor.name}</strong></td>
                <td>{donor.email}</td>
                <td>{donor.phone}</td>
                <td><span className="category-badge">{donor.category}</span></td>
                <td style={{ textAlign: 'center' }}>{donor.total_donations}</td>
                <td>{donor.registration_date}</td>
                <td>
                  <div className="action-buttons">
                    <button className="btn-edit" onClick={() => handleEditDonor(donor)}>
                      <Edit2 size={16} />
                    </button>
                    <button className="btn-delete" onClick={() => handleDeleteDonor(donor.id)}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

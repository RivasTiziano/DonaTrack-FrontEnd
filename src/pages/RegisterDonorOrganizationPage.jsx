import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { ArrowLeft, Plus, Trash2 } from 'lucide-react'
import { donorOrganizationTypes } from '../data/registration-options'
import '../styles/registration.css'

export function RegisterDonorOrganizationPage() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    razonSocial: '',
    tipo: '',
    rubro: '',
    email: '',
    telefono: '',
    contactos: [
      { nombre: '', email: '', telefono: '' }
    ]
  })

  const [errors, setErrors] = useState({})

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }))
    }
  }

  const handleContactoChange = (index, field, value) => {
    const nuevoContacto = [...formData.contactos]
    nuevoContacto[index][field] = value
    setFormData(prev => ({
      ...prev,
      contactos: nuevoContacto
    }))
  }

  const agregarContacto = () => {
    setFormData(prev => ({
      ...prev,
      contactos: [...prev.contactos, { nombre: '', email: '', telefono: '' }]
    }))
  }

  const eliminarContacto = (index) => {
    if (formData.contactos.length > 1) {
      setFormData(prev => ({
        ...prev,
        contactos: prev.contactos.filter((_, i) => i !== index)
      }))
    }
  }

  const validateForm = () => {
    const newErrors = {}

    if (!formData.razonSocial.trim()) newErrors.razonSocial = 'La razón social es requerida'
    if (!formData.tipo) newErrors.tipo = 'El tipo de organización es requerido'
    if (!formData.rubro.trim()) newErrors.rubro = 'El rubro es requerido'
    if (!formData.email.trim() || !formData.email.includes('@')) newErrors.email = 'Email válido es requerido'
    if (!formData.telefono.trim()) newErrors.telefono = 'El teléfono es requerido'

    // Validar al menos un contacto representante
    const tieneContactoValido = formData.contactos.some(c => 
      c.nombre.trim() && c.email.trim()
    )
    if (!tieneContactoValido) newErrors.contactos = 'Debe agregar al menos un representante con nombre y email'

    return newErrors
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const newErrors = validateForm()

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    console.log('Datos del formulario (Organización Donante):', formData)
    alert('¡Registro exitoso! Se ha enviado un correo de confirmación.')
    navigate('/')
  }

  return (
    <div className="registration-container">
      <div className="registration-wrapper">
        <Link to="/registro" className="back-link">
          <ArrowLeft size={20} />
          Volver
        </Link>

        <div className="form-container">
          <div className="form-header">
            <h2>Registro de Organización Donante</h2>
            <p>Completa el formulario para registrar tu organización</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>
                Razón Social <span className="required">*</span>
              </label>
              <input
                type="text"
                name="razonSocial"
                value={formData.razonSocial}
                onChange={handleInputChange}
                placeholder="Nombre de la organización"
              />
              {errors.razonSocial && <span className="form-error">{errors.razonSocial}</span>}
            </div>

            <div className="form-groups-row">
              <div className="form-group">
                <label>
                  Tipo de Organización <span className="required">*</span>
                </label>
                <select
                  name="tipo"
                  value={formData.tipo}
                  onChange={handleInputChange}
                >
                  <option value="">Selecciona un tipo</option>
                  {donorOrganizationTypes.map(tipo => (
                    <option key={tipo.value} value={tipo.value}>
                      {tipo.label}
                    </option>
                  ))}
                </select>
                {errors.tipo && <span className="form-error">{errors.tipo}</span>}
              </div>
              <div className="form-group">
                <label>
                  Rubro <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="rubro"
                  value={formData.rubro}
                  onChange={handleInputChange}
                  placeholder="Rubro o sector"
                />
                {errors.rubro && <span className="form-error">{errors.rubro}</span>}
              </div>
            </div>

            <div className="form-groups-row">
              <div className="form-group">
                <label>
                  Email <span className="required">*</span>
                </label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="contacto@organizacion.com"
                />
                {errors.email && <span className="form-error">{errors.email}</span>}
              </div>
              <div className="form-group">
                <label>
                  Teléfono <span className="required">*</span>
                </label>
                <input
                  type="tel"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleInputChange}
                  placeholder="+54 11 1234-5678"
                />
                {errors.telefono && <span className="form-error">{errors.telefono}</span>}
              </div>
            </div>

            {/* Contactos Representantes */}
            <div className="contact-group">
              <h4>Personas Representantes <span className="required">*</span></h4>
              {errors.contactos && <span className="form-error">{errors.contactos}</span>}

              {formData.contactos.map((contacto, index) => (
                <div key={index} style={{ 
                  background: '#fff', 
                  padding: '1rem', 
                  borderRadius: '6px', 
                  marginBottom: '1rem',
                  border: '1px solid #ddd'
                }}>
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    marginBottom: '0.75rem'
                  }}>
                    <label style={{ fontWeight: '600', marginBottom: 0 }}>
                      Representante {index + 1}
                    </label>
                    {formData.contactos.length > 1 && (
                      <button
                        type="button"
                        onClick={() => eliminarContacto(index)}
                        style={{
                          background: '#ffe6e6',
                          border: 'none',
                          padding: '0.5rem',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.25rem',
                          color: '#e74c3c'
                        }}
                      >
                        <Trash2 size={16} />
                      </button>
                    )}
                  </div>

                  <div className="form-groups-row">
                    <div className="form-group">
                      <label>Nombre <span className="required">*</span></label>
                      <input
                        type="text"
                        value={contacto.nombre}
                        onChange={(e) => handleContactoChange(index, 'nombre', e.target.value)}
                        placeholder="Nombre del representante"
                      />
                    </div>
                    <div className="form-group">
                      <label>Email <span className="required">*</span></label>
                      <input
                        type="email"
                        value={contacto.email}
                        onChange={(e) => handleContactoChange(index, 'email', e.target.value)}
                        placeholder="email@ejemplo.com"
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Teléfono</label>
                    <input
                      type="tel"
                      value={contacto.telefono}
                      onChange={(e) => handleContactoChange(index, 'telefono', e.target.value)}
                      placeholder="+54 11 1234-5678"
                    />
                  </div>
                </div>
              ))}

              <button
                type="button"
                onClick={agregarContacto}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.75rem 1rem',
                  background: '#f0f0f0',
                  border: '1px dashed #667eea',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  color: '#667eea',
                  fontWeight: '500',
                  width: '100%',
                  justifyContent: 'center'
                }}
              >
                <Plus size={18} />
                Agregar otro representante
              </button>
            </div>

            <div className="form-actions">
              <Link to="/registro">
                <button type="button" className="btn-cancel">
                  Cancelar
                </button>
              </Link>
              <button type="submit" className="btn-submit">
                Registrar Organización
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

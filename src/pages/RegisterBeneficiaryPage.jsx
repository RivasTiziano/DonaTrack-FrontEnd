import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { ArrowLeft, Plus, Trash2 } from 'lucide-react'
import '../styles/registration.css'

export function RegisterBeneficiaryPage() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    razonSocial: '',
    direccion: '',
    telefono: '',
    contactos: [
      { nombre: '', email: '' }
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
      contactos: [...prev.contactos, { nombre: '', email: '' }]
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
    if (!formData.direccion.trim()) newErrors.direccion = 'La dirección completa es requerida'
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

    console.log('Datos del formulario (Entidad Beneficiaria):', formData)
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
            <h2>Registro de Entidad Beneficiaria</h2>
            <p>Completa el formulario para registrar tu entidad beneficiaria</p>
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
                placeholder="Nombre de la entidad"
              />
              {errors.razonSocial && <span className="form-error">{errors.razonSocial}</span>}
            </div>

            <div className="form-group">
              <label>
                Dirección Completa <span className="required">*</span>
              </label>
              <input
                type="text"
                name="direccion"
                value={formData.direccion}
                onChange={handleInputChange}
                placeholder="Calle, número, localidad, provincia"
              />
              {errors.direccion && <span className="form-error">{errors.direccion}</span>}
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

            {/* Contactos Representantes */}
            <div className="contact-group">
              <h4>Personas Representantes <span className="required">*</span></h4>
              <p style={{ fontSize: '0.85rem', color: '#666', margin: '0 0 1rem 0' }}>
                Agrega los datos de contacto de las personas autorizadas a operar en nombre de la entidad
              </p>
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
                        placeholder="email@entidad.com"
                      />
                    </div>
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

            <div style={{ 
              background: '#e8f4f8', 
              padding: '1rem', 
              borderRadius: '6px', 
              marginTop: '1.5rem',
              borderLeft: '4px solid #3498db'
            }}>
              <p style={{ margin: 0, fontSize: '0.9rem', color: '#2c3e50' }}>
                <strong>ℹ️ Información importante:</strong> Después del registro, podrás agregar tus necesidades materiales 
                y empezar a recibir donaciones que se adapten a tus requerimientos.
              </p>
            </div>

            <div className="form-actions">
              <Link to="/registro">
                <button type="button" className="btn-cancel">
                  Cancelar
                </button>
              </Link>
              <button type="submit" className="btn-submit">
                Registrar Entidad
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

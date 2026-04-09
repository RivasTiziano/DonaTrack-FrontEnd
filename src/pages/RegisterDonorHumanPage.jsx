import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { ArrowLeft, Mail, Phone, MessageCircle } from 'lucide-react'
import '../styles/registration.css'

export function RegisterDonorHumanPage() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    edad: '',
    documento: '',
    genero: '',
    direccion: '',
    email: '',
    telefono: '',
    whatsapp: '',
    contactosPrefijo: {
      email: true,
      telefono: false,
      whatsapp: false
    },
    contactoPreferido: 'email'
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

  const handleContactoChange = (type) => {
    setFormData(prev => ({
      ...prev,
      contactosPrefijo: {
        ...prev.contactosPrefijo,
        [type]: !prev.contactosPrefijo[type]
      }
    }))
  }

  const handleContactoPreferido = (e) => {
    setFormData(prev => ({
      ...prev,
      contactoPreferido: e.target.value
    }))
  }

  const validateForm = () => {
    const newErrors = {}

    if (!formData.nombre.trim()) newErrors.nombre = 'El nombre es requerido'
    if (!formData.apellido.trim()) newErrors.apellido = 'El apellido es requerido'
    if (!formData.edad) newErrors.edad = 'La edad es requerida'
    if (!formData.documento.trim()) newErrors.documento = 'El número de documento es requerido'
    if (!formData.genero) newErrors.genero = 'El género es requerido'
    if (!formData.direccion.trim()) newErrors.direccion = 'La dirección es requerida'
    if (!formData.email.trim() || !formData.email.includes('@')) newErrors.email = 'Email válido es requerido'
    
    const tieneAlgunContacto = formData.contactosPrefijo.email || formData.contactosPrefijo.telefono || formData.contactosPrefijo.whatsapp
    if (!tieneAlgunContacto) newErrors.contactos = 'Debe seleccionar al menos un medio de contacto'

    return newErrors
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const newErrors = validateForm()

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    // Aquí iría la llamada a la API para registrar el donante
    console.log('Datos del formulario (Donante Humano):', formData)
    
    // Por ahora, solo mostramos mensaje de éxito
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
            <h2>Registro de Persona Donante</h2>
            <p>Completa el formulario para registrarte como donante</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-groups-row">
              <div className="form-group">
                <label>
                  Nombre <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  placeholder="Tu nombre"
                />
                {errors.nombre && <span className="form-error">{errors.nombre}</span>}
              </div>
              <div className="form-group">
                <label>
                  Apellido <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="apellido"
                  value={formData.apellido}
                  onChange={handleInputChange}
                  placeholder="Tu apellido"
                />
                {errors.apellido && <span className="form-error">{errors.apellido}</span>}
              </div>
            </div>

            <div className="form-groups-row">
              <div className="form-group">
                <label>
                  Edad <span className="required">*</span>
                </label>
                <input
                  type="number"
                  name="edad"
                  value={formData.edad}
                  onChange={handleInputChange}
                  placeholder="Tu edad"
                  min="18"
                />
                {errors.edad && <span className="form-error">{errors.edad}</span>}
              </div>
              <div className="form-group">
                <label>
                  Nº Documento <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="documento"
                  value={formData.documento}
                  onChange={handleInputChange}
                  placeholder="DNI/Cédula"
                />
                {errors.documento && <span className="form-error">{errors.documento}</span>}
              </div>
            </div>

            <div className="form-groups-row">
              <div className="form-group">
                <label>
                  Género <span className="required">*</span>
                </label>
                <select
                  name="genero"
                  value={formData.genero}
                  onChange={handleInputChange}
                >
                  <option value="">Selecciona un género</option>
                  <option value="masculino">Masculino</option>
                  <option value="femenino">Femenino</option>
                  <option value="otro">Otro</option>
                  <option value="prefiero-no-decir">Prefiero no decir</option>
                </select>
                {errors.genero && <span className="form-error">{errors.genero}</span>}
              </div>
              <div className="form-group">
                <label>
                  Dirección <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="direccion"
                  value={formData.direccion}
                  onChange={handleInputChange}
                  placeholder="Tu dirección completa"
                />
                {errors.direccion && <span className="form-error">{errors.direccion}</span>}
              </div>
            </div>

            <div className="form-group">
              <label>
                Email <span className="required">*</span>
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="tu@email.com"
              />
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            <div className="form-groups-row">
              <div className="form-group">
                <label>Teléfono (Opcional)</label>
                <input
                  type="tel"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleInputChange}
                  placeholder="+54 11 1234-5678"
                />
              </div>
              <div className="form-group">
                <label>WhatsApp (Opcional)</label>
                <input
                  type="tel"
                  name="whatsapp"
                  value={formData.whatsapp}
                  onChange={handleInputChange}
                  placeholder="+54 11 1234-5678"
                />
              </div>
            </div>

            <div className="contact-group">
              <h4>Medios de contacto preferidos <span className="required">*</span></h4>
              <div className="contact-types">
                <div className="contact-type">
                  <input
                    type="checkbox"
                    id="contact-email"
                    checked={formData.contactosPrefijo.email}
                    onChange={() => handleContactoChange('email')}
                    disabled
                  />
                  <label htmlFor="contact-email" style={{ opacity: 0.7 }}>
                    <Mail size={16} style={{ verticalAlign: 'middle', marginRight: '0.5rem' }} />
                    Email (obligatorio)
                  </label>
                </div>
                <div className="contact-type">
                  <input
                    type="checkbox"
                    id="contact-phone"
                    checked={formData.contactosPrefijo.telefono}
                    onChange={() => handleContactoChange('telefono')}
                  />
                  <label htmlFor="contact-phone">
                    <Phone size={16} style={{ verticalAlign: 'middle', marginRight: '0.5rem' }} />
                    Teléfono
                  </label>
                </div>
                <div className="contact-type">
                  <input
                    type="checkbox"
                    id="contact-whatsapp"
                    checked={formData.contactosPrefijo.whatsapp}
                    onChange={() => handleContactoChange('whatsapp')}
                  />
                  <label htmlFor="contact-whatsapp">
                    <MessageCircle size={16} style={{ verticalAlign: 'middle', marginRight: '0.5rem' }} />
                    WhatsApp
                  </label>
                </div>
              </div>

              {errors.contactos && <span className="form-error" style={{ marginTop: '0.75rem', display: 'block' }}>{errors.contactos}</span>}

              <div className="default-contact">
                <label style={{ marginBottom: '0.5rem', display: 'block' }}>
                  Contacto predeterminado para notificaciones
                </label>
                <select 
                  value={formData.contactoPreferido} 
                  onChange={handleContactoPreferido}
                >
                  <option value="email">Email</option>
                  {formData.contactosPrefijo.telefono && <option value="telefono">Teléfono</option>}
                  {formData.contactosPrefijo.whatsapp && <option value="whatsapp">WhatsApp</option>}
                </select>
              </div>
            </div>

            <div className="form-actions">
              <Link to="/registro">
                <button type="button" className="btn-cancel">
                  Cancelar
                </button>
              </Link>
              <button type="submit" className="btn-submit">
                Registrarse
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

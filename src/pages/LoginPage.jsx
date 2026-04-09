import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Heart, Mail, Lock } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import '../styles/registration.css'

export function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })

  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  // Datos de prueba para donantes
  const testDonors = [
    {
      id: 'donor-1',
      nombre: 'Juan',
      apellido: 'Pérez',
      email: 'juan@example.com',
      password: '123456',
      role: 'donor',
      phone: '+549123456789',
      category: 'Platino',
      joinDate: '2025-06-15'
    },
    {
      id: 'donor-2',
      nombre: 'María',
      apellido: 'García',
      email: 'maria@example.com',
      password: '123456',
      role: 'donor',
      phone: '+549987654321',
      category: 'Oro',
      joinDate: '2025-08-20'
    }
  ]

  // Datos de prueba para entidades beneficiarias
  const testBeneficiaries = [
    {
      id: 'entity-1',
      nombre: 'Fundación Despierta',
      email: 'info@fundaciondespierta.org',
      password: '123456',
      role: 'beneficiary',
      phone: '+541123456789',
      contact: 'María López',
      location: 'Villa Crespo, CABA',
      joinDate: '2025-01-10'
    },
    {
      id: 'entity-2',
      nombre: 'Red Solidaria Norte',
      email: 'contacto@redsolitaria.org',
      password: '123456',
      role: 'beneficiary',
      phone: '+541198765432',
      contact: 'Carlos Mendez',
      location: 'Belgrano, CABA',
      joinDate: '2025-03-15'
    }
  ]

  // Datos de prueba para administradores
  const testAdmins = [
    {
      id: 'admin-1',
      nombre: 'Admin',
      apellido: 'Sistema',
      email: 'admin@donatrack.com',
      password: 'admin123',
      role: 'admin',
      phone: '+541100000000',
      joinDate: '2025-01-01'
    }
  ]

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

  const validateForm = () => {
    const newErrors = {}

    if (!formData.email.trim()) {
      newErrors.email = 'El email es requerido'
    } else if (!formData.email.includes('@')) {
      newErrors.email = 'Email inválido'
    }

    if (!formData.password.trim()) {
      newErrors.password = 'La contraseña es requerida'
    }

    return newErrors
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const newErrors = validateForm()

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setLoading(true)

    // Simulamos la llamada a la API
    setTimeout(() => {
      const donor = testDonors.find(d => d.email === formData.email && d.password === formData.password)
      const beneficiary = testBeneficiaries.find(b => b.email === formData.email && b.password === formData.password)
      const admin = testAdmins.find(a => a.email === formData.email && a.password === formData.password)

      if (donor) {
        const { password, ...userWithoutPassword } = donor
        login(userWithoutPassword)
        navigate('/donante/dashboard')
      } else if (beneficiary) {
        const { password, ...userWithoutPassword } = beneficiary
        login(userWithoutPassword)
        navigate('/entidad/dashboard')
      } else if (admin) {
        const { password, ...userWithoutPassword } = admin
        login(userWithoutPassword)
        navigate('/admin/dashboard')
      } else {
        setErrors({ email: 'Email o contraseña incorrectos' })
      }
      setLoading(false)
    }, 1000)
  }

  return (
    <div className="registration-container">
      <div className="registration-wrapper">
        <Link to="/" style={{ textDecoration: 'none', marginBottom: '1.5rem', display: 'inline-flex' }}>
          <div className="back-link">
            <Heart size={20} />
            Volver al inicio
          </div>
        </Link>

        <div className="form-container">
          <div className="form-header">
            <h2>Iniciar Sesión</h2>
            <p>Accede a tu cuenta DonaTrack</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>
                Email <span className="required">*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <Mail size={18} style={{ 
                  position: 'absolute', 
                  left: '12px', 
                  top: '50%', 
                  transform: 'translateY(-50%)',
                  color: '#999',
                  pointerEvents: 'none'
                }} />
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="tu@email.com"
                  style={{ paddingLeft: '42px' }}
                />
              </div>
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            <div className="form-group">
              <label>
                Contraseña <span className="required">*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <Lock size={18} style={{ 
                  position: 'absolute', 
                  left: '12px', 
                  top: '50%', 
                  transform: 'translateY(-50%)',
                  color: '#999',
                  pointerEvents: 'none'
                }} />
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="Tu contraseña"
                  style={{ paddingLeft: '42px' }}
                />
              </div>
              {errors.password && <span className="form-error">{errors.password}</span>}
            </div>

            <div style={{ 
              textAlign: 'right', 
              marginBottom: '1.5rem'
            }}>
              <Link to="/" style={{ 
                color: '#667eea', 
                fontSize: '0.9rem',
                textDecoration: 'none',
                fontWeight: '500'
              }}>
                ¿Olvidaste tu contraseña?
              </Link>
            </div>

            <button 
              type="submit" 
              className="btn-submit"
              style={{ width: '100%' }}
              disabled={loading}
            >
              {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </button>
          </form>

          <div style={{ 
            marginTop: '1.5rem', 
            paddingTop: '1.5rem', 
            borderTop: '1px solid #ddd',
            textAlign: 'center'
          }}>
            <p style={{ color: '#666', margin: '0 0 1rem 0' }}>
              ¿No tienes cuenta?{' '}
              <Link to="/registro" style={{ 
                color: '#667eea', 
                fontWeight: '600',
                textDecoration: 'none'
              }}>
                Regístrate aquí
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

import { Heart, Users, Building2, ArrowRight } from 'lucide-react'
import { createElement } from 'react'
import { Link } from 'react-router-dom'
import '../styles/registration.css'

export function RegisterPage() {
  const registrationOptions = [
    {
      id: 'donor-human',
      title: 'Soy una persona donante',
      description: 'Registro como persona humana para realizar donaciones',
      icon: Heart,
      path: '/registro/donante-humano'
    },
    {
      id: 'donor-organization',
      title: 'Soy una organización donante',
      description: 'Registro como empresa, ONG u otra institución',
      icon: Building2,
      path: '/registro/donante-organizacion'
    },
    {
      id: 'beneficiary',
      title: 'Soy una entidad beneficiaria',
      description: 'Registro como institución para recibir donaciones',
      icon: Users,
      path: '/registro/entidad-beneficiaria'
    }
  ]

  return (
    <div className="registration-container">
      <div className="registration-wrapper">
        <Link to="/" className="back-link">
          <Heart size={20} />
          Volver al inicio
        </Link>

        <div className="registration-header">
          <h1>DonaTrack</h1>
          <p>Completa tu registro para empezar a colaborar</p>
        </div>

        <div className="registration-options">
          {registrationOptions.map((option) => (
            <Link
              key={option.id}
              to={option.path}
              style={{ textDecoration: 'none' }}
            >
              <div className="registration-card">
                <div className="registration-card-icon">
                  {createElement(option.icon, { size: 24 })}
                </div>
                <h3>{option.title}</h3>
                <p>{option.description}</p>
                <div style={{ marginTop: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#667eea', fontWeight: '500', fontSize: '0.9rem' }}>
                  Continuar <ArrowRight size={16} />
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

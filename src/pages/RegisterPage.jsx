import { Heart, ArrowRight } from 'lucide-react'
import { createElement } from 'react'
import { Link } from 'react-router-dom'
import { registrationOptions } from '../data/registration-options'
import '../styles/registration.css'

export function RegisterPage() {
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

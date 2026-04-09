import { Heart } from 'lucide-react'
import { NavLink } from 'react-router-dom'

export function TopNav() {
  return (
    <header className="dt-header">
      <div className="container dt-header-row">
        <NavLink className="brand" to="/">
          <span className="brand-mark" aria-hidden="true">
            <Heart size={20} />
          </span>
          DonaTrack
        </NavLink>

        <nav className="dt-nav" aria-label="Navegacion principal">
          <a href="/#about">Acerca de</a>
          <a href="/#donations">Donaciones</a>
          <a href="/#gallery">Galeria</a>
          <NavLink to="/explorar-donaciones">Explorar donaciones</NavLink>
        </nav>

        <div className="auth-actions">
          <NavLink to="/login" style={{ textDecoration: 'none' }}>
            <button type="button" style={{ 
              background: 'transparent', 
              color: '#667eea',
              border: '1px solid #667eea',
              padding: '0.68rem 1.05rem',
              borderRadius: '999px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 160ms ease',
              marginRight: '0.5rem'
            }}>
              Iniciar sesión
            </button>
          </NavLink>
          <NavLink to="/registro" style={{ textDecoration: 'none' }}>
            <button type="button" className="btn btn-primary">Registrarse</button>
          </NavLink>
        </div>
      </div>
    </header>
  )
}
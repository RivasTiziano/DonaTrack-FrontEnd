import { ArrowRight, Camera, CheckCircle, Heart, Mail, MapPin, Phone, Users } from 'lucide-react'
import { createElement } from 'react'
import { Link } from 'react-router-dom'
import { TopNav } from '../components/TopNav.jsx'
import {
  featuredDonations,
  footerLinks,
  heroContent,
  heroStats,
  landingFeatures,
  recentDeliveries,
} from '../data/landing.js'

export function LandingPage() {
  return (
    <>
      <TopNav />

      <main id="inicio">
        <section className="hero">
          <div className="container hero-grid">
            <div>
              <span className="kicker">{heroContent.kicker}</span>
              <h1>{heroContent.title}</h1>
              <p>{heroContent.description}</p>
              <div className="hero-actions">
                <Link to="/registro" style={{ textDecoration: 'none' }}>
                  <button type="button" className="btn btn-primary">
                    Empezar a donar
                  </button>
                </Link>
                <Link to="/explorar-donaciones" className="btn btn-outline hero-link-button">
                  {heroContent.ctaLabel} <ArrowRight size={16} />
                </Link>
              </div>
              <div className="stats" aria-label="Estadisticas destacadas">
                {heroStats.map((stat) => (
                  <div key={stat.label} className="stat">
                    <strong>{stat.value}</strong>
                    <span>{stat.label}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="hero-visual">
              <img
                src={heroContent.heroImage}
                alt="Voluntarios preparando donaciones"
              />
            </div>
          </div>
        </section>

        <section className="section" id="about">
          <div className="container">
            <h2>Nuestro proposito</h2>
            <p className="section-copy">
              Hacemos que la solidaridad sea medible y visible para fortalecer la confianza.
            </p>
            <div className="feature-grid">
              {landingFeatures.map((feature) => (
                <article key={feature.title} className="feature-card">
                  <div className="feature-icon" aria-hidden="true">
                    {createElement(feature.icon, { size: 20 })}
                  </div>
                  <h3>{feature.title}</h3>
                  <p>{feature.description}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="section" id="donations">
          <div className="container">
            <h2>Donaciones destacadas del mes</h2>
            <p className="section-copy">Ejemplos de entregas recientes con su trazabilidad.</p>
            <div className="donation-grid">
              {featuredDonations.map((donation) => (
                <article key={donation.id} className="donation-card">
                  <img src={donation.image} alt={donation.item} />
                  <h3>{donation.item}</h3>
                  <p>
                    <strong>Donante:</strong> {donation.donorName}
                  </p>
                  <p className="meta">{donation.date}</p>
                  <p className="meta">Beneficiario: {donation.beneficiary}</p>
                  <button type="button" className="btn btn-outline">
                    Ver detalles
                  </button>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="section" id="gallery">
          <div className="container">
            <h2>Galeria de donaciones</h2>
            <p className="section-copy">Consulta entregas reales sin necesidad de iniciar sesion.</p>
            <div className="gallery-grid">
              {recentDeliveries.map((item) => (
                <figure key={item.id} className="gallery-item">
                  <img src={item.image} alt={item.title} />
                  <figcaption>{item.title}</figcaption>
                </figure>
              ))}
            </div>
          </div>
        </section>

        <section className="cta">
          <div className="container">
            <div className="cta-box">
              <h2>Sumate a una red de ayuda con evidencia real</h2>
              <p>
                Registra tus donaciones o solicita apoyo en una plataforma clara,
                colaborativa y centrada en el impacto social.
              </p>
              <div className="cta-actions">
                <Link to="/registro" style={{ textDecoration: 'none' }}>
                  <button type="button" className="btn btn-outline">
                    Registrarme como donante
                  </button>
                </Link>
                <Link to="/registro/entidad-beneficiaria" style={{ textDecoration: 'none' }}>
                  <button type="button" className="btn btn-outline">
                    Registrarme como beneficiario
                  </button>
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer" id="contact">
        <div className="container">
          <div className="footer-grid">
            <div>
              <div className="brand">
                <span className="brand-mark" aria-hidden="true">
                  <Heart size={20} />
                </span>
                DonaTrack
              </div>
              <p>Transparencia para transformar la confianza en ayuda concreta.</p>
            </div>

            <div>
              <h4>Enlaces rapidos</h4>
              <ul>
                {footerLinks.quickLinks.map((link) => (
                  <li key={link.label}><a href={link.href}>{link.label}</a></li>
                ))}
              </ul>
            </div>

            <div>
              <h4>Acceso</h4>
              <ul>
                {footerLinks.accessLinks.map((link) => (
                  <li key={link.label}><a href={link.href}>{link.label}</a></li>
                ))}
              </ul>
            </div>

            <div>
              <h4>Contacto</h4>
              <ul>
                <li><Mail size={14} /> info@donatrack.org</li>
                <li><Phone size={14} /> +52 55 1234 5678</li>
                <li><MapPin size={14} /> Buenos Aires, Argentina</li>
              </ul>
            </div>
          </div>

          <div className="legal">
            {footerLinks.legalLinks.map((link) => (
              <a key={link.label} href={link.href}>{link.label}</a>
            ))}
          </div>
        </div>
      </footer>
    </>
  )
}

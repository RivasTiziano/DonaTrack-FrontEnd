import { missions, badges, donorCategories } from '../../data/incentives'
import { CheckCircle2, Zap, Award } from 'lucide-react'
import '../../styles/pages/incentives-section.css'

export function IncentivesSection() {
  const completedMissions = missions.filter(m => m.completed)
  const activeMissions = missions.filter(m => !m.completed)

  return (
    <div className="incentives-view">
      <div className="section-header">
        <h2>Zona de Incentivos</h2>
        <p>Completa misiones, obtén insignias y sube de categoría</p>
      </div>

      {/* Categories Info */}
      <div className="categories-section">
        <h3>Categorías de Donantes</h3>
        <div className="categories-list">
          {donorCategories.map(cat => (
            <div key={cat.level} className="category-bar">
              <div className="category-indicator" style={{ backgroundColor: cat.color }}>
                {cat.level}
              </div>
              <div className="category-info">
                <strong>{cat.name}</strong>
                <small>{cat.minPoints}+ puntos</small>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Missions */}
      <div className="missions-tabs">
        <div className="tab-header">
          <h3>Misiones</h3>
          <div className="tab-stats">
            <span className="stat-badge">{completedMissions.length} Completadas</span>
            <span className="stat-badge active">{activeMissions.length} Activas</span>
          </div>
        </div>

        {/* Active Missions */}
        <div className="missions-section">
          <h4>Misiones Activas</h4>
          <div className="missions-grid">
            {activeMissions.length > 0 ? (
              activeMissions.map(mission => (
                <div key={mission.id} className="mission-card active">
                  <div className="mission-header">
                    <div className="mission-icon">{mission.icon}</div>
                    <div className="mission-reward">
                      <Zap size={16} />
                      <span>+{mission.reward} pts</span>
                    </div>
                  </div>
                  <h4>{mission.title}</h4>
                  <p className="description">{mission.description}</p>
                  <div className="requirement">
                    <strong>Requisito:</strong> {mission.requirement}
                  </div>
                  {mission.progress && (
                    <div className="progress-section">
                      <div className="progress-bar">
                        <div 
                          className="progress-fill" 
                          style={{ width: `${mission.progress}%` }}
                        ></div>
                      </div>
                      <span className="progress-text">{mission.progress}% completado</span>
                    </div>
                  )}
                  <button className="btn-mission-track">Rastrear Progreso</button>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <p>¡Todas las misiones completadas!</p>
              </div>
            )}
          </div>
        </div>

        {/* Completed Missions */}
        <div className="missions-section">
          <h4>Misiones Completadas</h4>
          <div className="missions-grid">
            {completedMissions.map(mission => (
              <div key={mission.id} className="mission-card completed">
                <div className="mission-header">
                  <div className="mission-icon">{mission.icon}</div>
                  <CheckCircle2 size={24} color="#4CAF50" />
                </div>
                <h4>{mission.title}</h4>
                <p className="description">{mission.description}</p>
                <div className="completed-date">
                  ✓ Completada el {new Date(mission.completedDate).toLocaleDateString('es-AR')}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Badges */}
      <div className="badges-section">
        <h3>Insignias Obtenidas</h3>
        <div className="badges-display">
          {badges.length > 0 ? (
            <div className="badges-grid">
              {badges.map(badge => (
                <div key={badge.id} className="badge-display-card">
                  <div className="badge-display-icon">{badge.icon}</div>
                  <h4>{badge.name}</h4>
                  <p>{badge.description}</p>
                  <small>
                    <Award size={12} />
                    {new Date(badge.earnedDate).toLocaleDateString('es-AR')}
                  </small>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>Completa misiones para ganar insignias</p>
            </div>
          )}
        </div>
      </div>

      {/* Achievement Statistics */}
      <div className="statistics-section">
        <h3>Estadísticas de Logros</h3>
        <div className="stats-boxes">
          <div className="stat-box">
            <div className="stat-number">{completedMissions.length}/{missions.length}</div>
            <p>Misiones Completadas</p>
          </div>
          <div className="stat-box">
            <div className="stat-number">{badges.length}</div>
            <p>Insignias Obtenidas</p>
          </div>
          <div className="stat-box">
            <div className="stat-number">{completedMissions.reduce((acc, m) => acc + m.reward, 0)}</div>
            <p>Puntos Totales</p>
          </div>
          <div className="stat-box">
            <div className="stat-number">50%</div>
            <p>Progreso General</p>
          </div>
        </div>
      </div>
    </div>
  )
}

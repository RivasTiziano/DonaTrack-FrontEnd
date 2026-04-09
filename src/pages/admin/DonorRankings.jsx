import { useState } from 'react'
import { currentMonthRanking, rankingHistory } from '../../data/donor-rankings'
import { TrendingUp, Medal } from 'lucide-react'
import '../../styles/pages/donor-rankings.css'

export function DonorRankings() {
  const [selectedMonth, setSelectedMonth] = useState('current')

  const currentRanking = selectedMonth === 'current' ? currentMonthRanking : rankingHistory[0].ranking

  const getMedalIcon = (rank) => {
    if (rank === 1) return '🥇'
    if (rank === 2) return '🥈'
    if (rank === 3) return '🥉'
    return `${rank}º`
  }

  return (
    <div className="donor-rankings-view">
      <div className="section-header">
        <h2>Ranking de Donantes</h2>
        <p>Visualiza la actividad y desempeño de donantes mensualmente</p>
      </div>

      {/* Month Selector */}
      <div className="month-selector">
        <button 
          className={`month-btn ${selectedMonth === 'current' ? 'active' : ''}`}
          onClick={() => setSelectedMonth('current')}
        >
          Mes Actual
        </button>
        {rankingHistory.map((history, idx) => (
          <button
            key={history.month}
            className={`month-btn ${selectedMonth === `history-${idx}` ? 'active' : ''}`}
            onClick={() => setSelectedMonth(`history-${idx}`)}
          >
            {history.month}
          </button>
        ))}
      </div>

      {/* Current Month Ranking */}
      {selectedMonth === 'current' && (
        <div className="ranking-container">
          <div className="ranking-header">
            <h3>Donantes Más Activos - Octubre 2025</h3>
          </div>

          <div className="ranking-list">
            {currentRanking.map((donor) => (
              <div key={donor.donor_id} className="ranking-item" style={{ '--rank': donor.rank }}>
                <div className="rank-position">
                  <span className="rank-number">{getMedalIcon(donor.rank)}</span>
                </div>

                <div className="rank-info">
                  <div className="donor-name">{donor.name}</div>
                  <div className="donor-stats">
                    <span className="stat">
                      <strong>{donor.donations}</strong> donaciones
                    </span>
                    <span className="stat">
                      <strong>{donor.items_count}</strong> items
                    </span>
                  </div>
                </div>

                <div className="impact-score">
                  <div className="score-label">Impacto</div>
                  <div className="score-bar">
                    <div className="score-fill" style={{ width: `${donor.impact_score}%` }}></div>
                  </div>
                  <div className="score-value">{donor.impact_score}%</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Historical Rankings */}
      {selectedMonth !== 'current' && (
        <div className="ranking-container">
          {rankingHistory.map((history, idx) => (
            selectedMonth === `history-${idx}` && (
              <div key={history.month}>
                <div className="ranking-header">
                  <h3>Donantes Más Activos - {history.month}</h3>
                </div>

                <div className="ranking-list">
                  {history.ranking.map((donor) => (
                    <div key={donor.donor_id} className="ranking-item" style={{ '--rank': donor.rank }}>
                      <div className="rank-position">
                        <span className="rank-number">{getMedalIcon(donor.rank)}</span>
                      </div>

                      <div className="rank-info">
                        <div className="donor-name">{donor.name}</div>
                        <div className="donor-stats">
                          <span className="stat">
                            <strong>{donor.donations}</strong> donaciones
                          </span>
                          <span className="stat">
                            <strong>{donor.items_count}</strong> items
                          </span>
                        </div>
                      </div>

                      <div className="impact-score">
                        <div className="score-label">Impacto</div>
                        <div className="score-bar">
                          <div className="score-fill" style={{ width: `${donor.impact_score}%` }}></div>
                        </div>
                        <div className="score-value">{donor.impact_score}%</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )
          ))}
        </div>
      )}

      {/* Stats Summary */}
      <div className="stats-summary">
        <div className="summary-card">
          <TrendingUp size={24} />
          <div>
            <h4>Total de Donaciones</h4>
            <p className="value">{currentRanking.reduce((sum, d) => sum + d.donations, 0)}</p>
          </div>
        </div>
        <div className="summary-card">
          <Medal size={24} />
          <div>
            <h4>Promedio de Items</h4>
            <p className="value">{Math.round(currentRanking.reduce((sum, d) => sum + d.items_count, 0) / currentRanking.length)}</p>
          </div>
        </div>
        <div className="summary-card">
          <TrendingUp size={24} />
          <div>
            <h4>Impacto Combinado</h4>
            <p className="value">{Math.round(currentRanking.reduce((sum, d) => sum + d.impact_score, 0) / currentRanking.length)}%</p>
          </div>
        </div>
      </div>
    </div>
  )
}

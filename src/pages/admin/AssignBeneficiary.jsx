import { useState, useMemo } from 'react'
import { pendingDonations } from '../../data/donations-pending'
import { Heart, CheckCircle } from 'lucide-react'
import '../../styles/pages/assign-beneficiary.css'

export function AssignBeneficiary() {
  const [donations, setDonations] = useState(pendingDonations)
  const [selectedDonation, setSelectedDonation] = useState(null)
  const [selectedBeneficiary, setSelectedBeneficiary] = useState(null)

  // Mock beneficiaries list with needs and metadata
  const beneficiaries = [
    { 
      id: 'entity-1', 
      name: 'Fundación Despierta', 
      location: 'Villa Crespo, CABA',
      people_served: 150,
      needs: ['Alimentos', 'Ropa', 'Medicamentos', 'Útiles escolares'],
      capacity: 500,
      current_recipients: 120
    },
    { 
      id: 'entity-2', 
      name: 'Red Solidaria', 
      location: 'Belgrano, CABA',
      people_served: 95,
      needs: ['Alimentos', 'Ropa'],
      capacity: 300,
      current_recipients: 85
    },
    { 
      id: 'entity-3', 
      name: 'Centro Comunitario San Pedro', 
      location: 'La Boca, CABA',
      people_served: 120,
      needs: ['Útiles escolares', 'Ropa', 'Medicamentos'],
      capacity: 400,
      current_recipients: 95
    },
    { 
      id: 'entity-4', 
      name: 'Hogar de Ancianos María', 
      location: 'Flores, CABA',
      people_served: 60,
      needs: ['Medicamentos', 'Alimentos'],
      capacity: 150,
      current_recipients: 58
    }
  ]

  // Algorithm to calculate compatibility score
  const calculateCompatibilityScore = (donation, beneficiary) => {
    let score = 0
    const weights = {
      needsMatch: 0.4,      // 40%
      capacity: 0.2,        // 20%
      priority: 0.2,        // 20%
      distanceFit: 0.1,     // 10%
      experience: 0.1       // 10%
    }

    // 1. Needs Match (40%)
    const donationItems = donation.items.map(i => i.name)
    const matchCount = donationItems.filter(item => 
      beneficiary.needs.some(need => 
        need.toLowerCase().includes(item.toLowerCase()) || 
        item.toLowerCase().includes(need.toLowerCase())
      )
    ).length
    const needsMatchScore = (matchCount / donationItems.length) * 100
    score += needsMatchScore * weights.needsMatch

    // 2. Capacity (20%)
    const availableCapacity = beneficiary.capacity - beneficiary.current_recipients
    const capacityRatio = (availableCapacity / beneficiary.capacity) * 100
    score += capacityRatio * weights.capacity

    // 3. Priority Level (20%)
    const priorityScores = {
      'Crítica': 100,
      'Alta': 80,
      'Media': 60,
      'Baja': 40
    }
    const priorityScore = priorityScores[donation.priority_level] || 50
    score += priorityScore * weights.priority

    // 4. Distance Fit (10%)
    const distanceScores = {
      'Villa Crespo, CABA': 95,
      'Belgrano, CABA': 90,
      'La Boca, CABA': 75,
      'Flores, CABA': 80
    }
    const distanceScore = distanceScores[beneficiary.location] || 70
    score += distanceScore * weights.distanceFit

    // 5. Experience/Reputation (10%)
    const experienceScore = (beneficiary.people_served / 150) * 100
    score += Math.min(experienceScore, 100) * weights.experience

    return Math.round(score)
  }

  const pendingDonationsToAssign = donations.filter(d => d.status === 'Pendiente')

  // Calculate scored beneficiaries when a donation is selected
  const scoredBeneficiaries = useMemo(() => {
    if (!selectedDonation) return []
    
    return beneficiaries
      .map(ben => ({
        ...ben,
        compatibilityScore: calculateCompatibilityScore(selectedDonation, ben)
      }))
      .sort((a, b) => b.compatibilityScore - a.compatibilityScore)
  }, [selectedDonation])

  const handleAssignBeneficiary = () => {
    if (!selectedDonation || !selectedBeneficiary) {
      alert('Selecciona tanto una donación como una entidad beneficiaria')
      return
    }

    setDonations(donations.map(d => 
      d.id === selectedDonation.id 
        ? { ...d, status: 'Asignada', assigned_to: selectedBeneficiary.id, assigned_date: new Date().toISOString().split('T')[0] }
        : d
    ))

    alert(`✅ Donación asignada a ${selectedBeneficiary.name}`)
    setSelectedDonation(null)
    setSelectedBeneficiary(null)
  }

  const getScoreColor = (score) => {
    if (score >= 80) return '#4caf50'
    if (score >= 60) return '#ff9800'
    return '#f44336'
  }

  return (
    <div className="assign-beneficiary-view">
      <div className="section-header">
        <h2>Asignar Entidad Beneficiaria</h2>
        <p>Sistema inteligente de selección basado en algoritmo de compatibilidad</p>
      </div>

      <div className="assign-layout">
        {/* Pending Donations */}
        <div className="donations-section">
          <div className="section-title">
            <h3>Donaciones Pendientes</h3>
            <span className="count-badge">{pendingDonationsToAssign.length}</span>
          </div>

          <div className="donations-list">
            {pendingDonationsToAssign.length > 0 ? (
              pendingDonationsToAssign.map(donation => (
                <div
                  key={donation.id}
                  className={`donation-card ${selectedDonation?.id === donation.id ? 'selected' : ''}`}
                  onClick={() => setSelectedDonation(donation)}
                >
                  <div className="card-header">
                    <h4>{donation.title}</h4>
                    <span className="priority-badge">{donation.priority_level}</span>
                  </div>
                  <p className="donor-info">{donation.donor_name}</p>
                  <div className="items-count">{donation.items.length} tipos de items</div>
                  <div className="items-preview">
                    {donation.items.map((item, idx) => (
                      <span key={idx} className="item-tag">{item.name}</span>
                    ))}
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <Heart size={32} />
                <p>¡No hay donaciones pendientes!</p>
              </div>
            )}
          </div>
        </div>

        {/* Beneficiaries Section */}
        <div className="beneficiaries-section">
          <div className="section-title">
            <h3>Resultados del Algoritmo</h3>
            {selectedDonation && <span className="count-badge">{scoredBeneficiaries.length}</span>}
          </div>

          {selectedDonation ? (
            <div className="beneficiaries-list">
              {scoredBeneficiaries.map((beneficiary, idx) => (
                <div
                  key={beneficiary.id}
                  className={`beneficiary-card ${selectedBeneficiary?.id === beneficiary.id ? 'selected' : ''} ${idx === 0 ? 'recommended' : ''}`}
                  onClick={() => setSelectedBeneficiary(beneficiary)}
                >
                  {idx === 0 && (
                    <div className="recommended-badge">
                      <CheckCircle size={16} />
                      Recomendada
                    </div>
                  )}
                  
                  <div className="card-header">
                    <h4>{beneficiary.name}</h4>
                    {selectedBeneficiary?.id === beneficiary.id && <CheckCircle size={20} />}
                  </div>

                  <p className="location">📍 {beneficiary.location}</p>

                  {/* Compatibility Score */}
                  <div className="compatibility-score">
                    <div className="score-label">Compatibilidad</div>
                    <div className="score-visual">
                      <div className="score-bar">
                        <div 
                          className="score-fill" 
                          style={{ 
                            width: `${beneficiary.compatibilityScore}%`,
                            backgroundColor: getScoreColor(beneficiary.compatibilityScore)
                          }}
                        />
                      </div>
                      <span className="score-number">{beneficiary.compatibilityScore}%</span>
                    </div>
                  </div>

                  {/* Needs Match */}
                  <div className="score-detail">
                    <span className="label">Necesidades coinciden:</span>
                    <div className="needs-tags">
                      {selectedDonation.items.map((item, i) => (
                        <span 
                          key={i} 
                          className={`need-tag ${beneficiary.needs.some(n => n.toLowerCase().includes(item.name.toLowerCase()) || item.name.toLowerCase().includes(n.toLowerCase())) ? 'match' : 'no-match'}`}
                        >
                          {item.name}
                        </span>
                      ))}
                    </div>
                  </div>

                  {/* Capacity Info */}
                  <div className="score-detail">
                    <span className="label">Capacidad: {beneficiary.capacity - beneficiary.current_recipients} espacios libres</span>
                  </div>

                  {/* People Served */}
                  <div className="people-served">
                    Atiende a <strong>{beneficiary.people_served}</strong> personas
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <Heart size={32} />
              <p>Selecciona una donación para ver las opciones de asignación</p>
            </div>
          )}
        </div>
      </div>

      {/* Assignment Panel */}
      {selectedDonation && selectedBeneficiary && (
        <div className="assignment-panel">
          <div className="panel-content">
            <h3>Confirmar Asignación</h3>
            <div className="assignment-details">
              <div className="detail-row">
                <span className="label">Donación:</span>
                <span className="value">{selectedDonation.title}</span>
              </div>
              <div className="detail-row">
                <span className="label">Donante:</span>
                <span className="value">{selectedDonation.donor_name}</span>
              </div>
              <div className="detail-row">
                <span className="label">Entidad Beneficiaria:</span>
                <span className="value">{selectedBeneficiary.name}</span>
              </div>
              <div className="detail-row">
                <span className="label">Compatibilidad:</span>
                <span className="value" style={{ color: getScoreColor(selectedBeneficiary.compatibilityScore) }}>
                  {selectedBeneficiary.compatibilityScore}% de match
                </span>
              </div>
              <div className="detail-row">
                <span className="label">Impacto Estimado:</span>
                <span className="value">{selectedBeneficiary.people_served} personas beneficiadas</span>
              </div>
            </div>
          </div>

          <button className="btn-assign" onClick={handleAssignBeneficiary}>
            <CheckCircle size={20} />
            Confirmar Asignación
          </button>
        </div>
      )}
    </div>
  )
}

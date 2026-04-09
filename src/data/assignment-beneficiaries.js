export const beneficiariesForAssignment = [
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

export const compatibilityWeights = {
  needsMatch: 0.4,
  capacity: 0.2,
  priority: 0.2,
  distanceFit: 0.1,
  experience: 0.1
}

export const priorityScores = {
  'Crítica': 100,
  Alta: 80,
  Media: 60,
  Baja: 40
}

export const distanceScores = {
  'Villa Crespo, CABA': 95,
  'Belgrano, CABA': 90,
  'La Boca, CABA': 75,
  'Flores, CABA': 80
}

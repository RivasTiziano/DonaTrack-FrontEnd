export const missions = [
  {
    id: 'mission-1',
    title: 'Tu Primera Donación',
    description: 'Realiza tu primera donación en DonaTrack',
    icon: '🎁',
    requirement: 'Completar 1 donación',
    reward: 50,
    completed: true,
    completedDate: '2025-06-20'
  },
  {
    id: 'mission-2',
    title: 'Donante Generoso',
    description: 'Realiza 5 donaciones en el mes',
    icon: '💝',
    requirement: 'Completar 5 donaciones en un mes',
    reward: 100,
    completed: true,
    completedDate: '2025-07-15'
  },
  {
    id: 'mission-3',
    title: 'Ayuda Consistente',
    description: 'Mantén donaciones durante 3 meses consecutivos',
    icon: '📅',
    requirement: 'Donar al menos 1 vez por mes durante 3 meses',
    reward: 150,
    completed: true,
    completedDate: '2025-09-10'
  },
  {
    id: 'mission-4',
    title: 'Variedad de Corazón',
    description: 'Dona en al menos 4 categorías diferentes',
    icon: '🌈',
    requirement: 'Donar en 4 categorías diferentes',
    reward: 120,
    completed: false,
    progress: 3
  },
  {
    id: 'mission-5',
    title: 'Impacto Social',
    description: 'Tus donaciones llegan a 100 beneficiarios',
    icon: '🤝',
    requirement: 'Que tus donaciones beneficien a 100 personas',
    reward: 200,
    completed: false,
    progress: 75
  },
  {
    id: 'mission-6',
    title: 'Donante Premium',
    description: 'Alcanza la categoría Platino',
    icon: '👑',
    requirement: 'Acumular 500 puntos en DonaTrack',
    reward: 250,
    completed: false,
    progress: 420
  }
]

export const badges = [
  {
    id: 'badge-1',
    name: 'Primer Paso',
    description: 'Realizaste tu primera donación',
    icon: '🌱',
    earnedDate: '2025-06-20'
  },
  {
    id: 'badge-2',
    name: 'Solidario',
    description: 'Completaste 5 donaciones',
    icon: '❤️',
    earnedDate: '2025-07-15'
  },
  {
    id: 'badge-3',
    name: 'Comprometido',
    description: 'Donaste durante 3 meses seguidos',
    icon: '🎯',
    earnedDate: '2025-09-10'
  },
  {
    id: 'badge-4',
    name: 'Corazón Generoso',
    description: 'Acumulaste 500 puntos de impacto',
    icon: '✨',
    earnedDate: '2025-10-05'
  }
]

export const donorCategories = [
  { level: 1, name: 'Sembrador', minPoints: 0, color: '#90EE90' },
  { level: 2, name: 'Aliado', minPoints: 100, color: '#87CEEB' },
  { level: 3, name: 'Guardián', minPoints: 250, color: '#FFD700' },
  { level: 4, name: 'Oro', minPoints: 400, color: '#FFA500' },
  { level: 5, name: 'Platino', minPoints: 500, color: '#C0C0C0' }
]

export const generateUserNotifications = (userId) => [
  {
    id: 'notif-1',
    type: 'donation_assigned',
    title: 'Donación Asignada',
    message: 'Tu donación de alimentos ha sido asignada a Fundación Despierta',
    timestamp: '2025-10-15T14:30:00',
    read: false,
    donationId: 'donation-1',
    icon: '📦'
  },
  {
    id: 'notif-2',
    type: 'mission_completed',
    title: 'Misión Completada',
    message: 'Felicidades, completaste la misión "Donante Generoso"',
    timestamp: '2025-10-14T10:15:00',
    read: false,
    missionId: 'mission-2',
    icon: '🎉'
  },
  {
    id: 'notif-3',
    type: 'category_upgraded',
    title: 'Cambio de Categoría',
    message: 'Subiste a la categoría Platino',
    timestamp: '2025-10-13T16:45:00',
    read: true,
    category: 'Platino',
    icon: '👑'
  },
  {
    id: 'notif-4',
    type: 'donation_received',
    title: 'Donación Recibida',
    message: 'Red Solidaria Norte confirmó la recepción de tu donación de abrigo',
    timestamp: '2025-10-12T09:20:00',
    read: true,
    donationId: 'donation-2',
    icon: '✅'
  },
  {
    id: 'notif-5',
    type: 'delivery_update',
    title: 'Actualización de Entrega',
    message: 'El camión de tu donación está a 2.5 km de distancia',
    timestamp: '2025-10-15T15:00:00',
    read: false,
    donationId: 'donation-3',
    icon: '🚚'
  }
]

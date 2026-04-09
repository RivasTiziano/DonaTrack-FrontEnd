export const generateBeneficiaryNotifications = (entityId) => [
  {
    id: 'notif-1',
    type: 'donation_assigned',
    title: 'Nueva Donación Asignada',
    message: 'Juan Pérez te ha asignado una donación de alimentos (50 kg arroz, 35 kg lentejas)',
    timestamp: '2025-10-15T14:30:00',
    read: false,
    donationId: 'assigned-1',
    icon: '📦',
    actionUrl: '/entidad/donaciones/assigned-1'
  },
  {
    id: 'notif-2',
    type: 'delivery_in_transit',
    title: 'Entrega en Camino',
    message: 'El camión BB 002 CC está en camino con tu donación de abrigo. Llegará en 2 horas',
    timestamp: '2025-10-15T10:15:00',
    read: false,
    donationId: 'assigned-2',
    icon: '🚚',
    actionUrl: '/entidad/entregas/assigned-2'
  },
  {
    id: 'notif-3',
    type: 'delivery_arrived',
    title: 'Entrega Lista para Recibir',
    message: 'El camión CC 003 DD ha llegado con medicamentos. Por favor, ve al portón de entrada',
    timestamp: '2025-10-14T16:45:00',
    read: true,
    donationId: 'assigned-4',
    icon: '✅'
  },
  {
    id: 'notif-4',
    type: 'delivery_confirmed',
    title: 'Entrega Confirmada',
    message: 'Gracias por confirmar la recepción de útiles escolares. Impacto: 120 niños beneficiados',
    timestamp: '2025-10-12T09:20:00',
    read: true,
    donationId: 'assigned-3',
    icon: '🎉'
  },
  {
    id: 'notif-5',
    type: 'need_matched',
    title: 'Necesidad Cubierta',
    message: 'Tu necesidad de "Productos de higiene" ha sido parcialmente cubierta por donantes',
    timestamp: '2025-10-13T15:00:00',
    read: true,
    needId: 'need-5',
    icon: '💚'
  }
]

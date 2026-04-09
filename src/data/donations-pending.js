export const pendingDonations = [
  {
    id: 'pending-1',
    donation_id: 'donation-101',
    donor_id: 'donor-1',
    donor_name: 'Juan Pérez',
    title: 'Alimentos no perecederos',
    items: [
      { name: 'Arroz integral', quantity: 50, unit: 'kg' },
      { name: 'Lentejas', quantity: 35, unit: 'kg' },
      { name: 'Leche en polvo', quantity: 40, unit: 'cajas' }
    ],
    received_date: '2025-10-14',
    expiration_date: '2025-11-15',
    status: 'Pendiente',
    condition: 'Óptima',
    storage_location: 'Estantería A-12',
    priority_level: 'Alta'
  },
  {
    id: 'pending-2',
    donation_id: 'donation-102',
    donor_id: 'donor-2',
    donor_name: 'María García',
    title: 'Ropa de abrigo',
    items: [
      { name: 'Mantas térmicas', quantity: 28, unit: 'unidades' },
      { name: 'Buzos de lana', quantity: 42, unit: 'unidades' },
      { name: 'Medias térmicas', quantity: 60, unit: 'pares' }
    ],
    received_date: '2025-10-12',
    expiration_date: null,
    status: 'Pendiente',
    condition: 'Óptima',
    storage_location: 'Pasillo B',
    priority_level: 'Media'
  },
  {
    id: 'pending-3',
    donation_id: 'donation-103',
    donor_id: 'donor-4',
    donor_name: 'ONG Solidaria',
    title: 'Útiles escolares',
    items: [
      { name: 'Cuadernos', quantity: 200, unit: 'unidades' },
      { name: 'Lápices', quantity: 500, unit: 'unidades' },
      { name: 'Mochilas', quantity: 100, unit: 'unidades' }
    ],
    received_date: '2025-10-10',
    expiration_date: null,
    status: 'Pendiente',
    condition: 'Óptima',
    storage_location: 'Almacén 1',
    priority_level: 'Baja'
  },
  {
    id: 'pending-4',
    donation_id: 'donation-104',
    donor_id: 'donor-3',
    donor_name: 'Carlos Mendez',
    title: 'Medicamentos y vitaminas',
    items: [
      { name: 'Vitamina C', quantity: 100, unit: 'cajas' },
      { name: 'Paracetamol', quantity: 50, unit: 'cajas' },
      { name: 'Analgésicos', quantity: 75, unit: 'cajas' }
    ],
    received_date: '2025-09-25',
    expiration_date: '2026-03-25',
    status: 'Revisión',
    condition: 'Requiere verificación',
    storage_location: 'Área médica',
    priority_level: 'Crítica'
  }
]

export const donationStatuses = ['Pendiente', 'Asignada', 'Revisión', 'Vencida']
export const donationConditions = ['Óptima', 'Buena', 'Aceptable', 'Requiere verificación', 'No apta']
export const priorityLevels = ['Baja', 'Media', 'Alta', 'Crítica']

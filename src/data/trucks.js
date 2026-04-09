export const availableTrucks = [
  {
    id: 'truck-1',
    plate: 'AA 001 BB',
    driver_name: 'Carlos Rodríguez',
    driver_phone: '+34 911 234567',
    capacity: '5 toneladas',
    current_load: '2 toneladas',
    status: 'Disponible',
    fuel: 75,
    last_maintenance: '2025-08-15',
    next_maintenance: '2025-11-15',
    insurance_expiry: '2026-06-30'
  },
  {
    id: 'truck-2',
    plate: 'BB 002 CC',
    driver_name: 'María Gómez',
    driver_phone: '+34 912 345678',
    capacity: '3.5 toneladas',
    current_load: '3.2 toneladas',
    status: 'En ruta',
    fuel: 45,
    last_maintenance: '2025-09-10',
    next_maintenance: '2025-12-10',
    insurance_expiry: '2026-03-15'
  },
  {
    id: 'truck-3',
    plate: 'CC 003 DD',
    driver_name: 'Juan López',
    driver_phone: '+34 913 456789',
    capacity: '2 toneladas',
    current_load: '0 toneladas',
    status: 'Mantenimiento',
    fuel: 10,
    last_maintenance: '2025-10-05',
    next_maintenance: '2026-01-05',
    insurance_expiry: '2025-12-30'
  },
  {
    id: 'truck-4',
    plate: 'DD 004 EE',
    driver_name: 'Pedro Sánchez',
    driver_phone: '+34 914 567890',
    capacity: '4 toneladas',
    current_load: '1.5 toneladas',
    status: 'Disponible',
    fuel: 60,
    last_maintenance: '2025-07-20',
    next_maintenance: '2025-10-20',
    insurance_expiry: '2026-05-20'
  }
]

export const truckStatuses = ['Disponible', 'En ruta', 'Mantenimiento', 'Fuera de servicio']

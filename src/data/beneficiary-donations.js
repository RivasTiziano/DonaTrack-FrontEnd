export const beneficiaryAssignedDonations = [
  {
    id: 'assigned-1',
    donation_id: 'donation-1',
    entity_id: 'entity-1',
    donor_name: 'Juan Pérez',
    donor_category: 'Platino',
    title: 'Donación de Alimentos',
    items: [
      { name: 'Arroz', quantity: '50 kg' },
      { name: 'Lentejas', quantity: '35 kg' },
      { name: 'Leche en polvo', quantity: '40 unidades' }
    ],
    status: 'Asignada',
    assigned_date: '2025-10-14',
    expected_delivery: '2025-10-18',
    delivery_truck: {
      plate: 'AA 001 BB',
      driver: 'Carlos Rodríguez',
      phone: '+34 911 234567',
      location: {
        lat: -34.5900,
        lng: -58.4200,
        address: 'Ruta 7, km 12.5'
      }
    },
    location: {
      lat: -34.5989,
      lng: -58.4395,
      address: 'Av. Corrientes 4500, Villa Crespo, CABA'
    },
    impact: 'Beneficiará a 150 personas'
  },
  {
    id: 'assigned-2',
    donation_id: 'donation-2',
    entity_id: 'entity-1',
    donor_name: 'María García',
    donor_category: 'Oro',
    title: 'Donación de Abrigo',
    items: [
      { name: 'Mantas', quantity: '28 unidades' },
      { name: 'Buzos', quantity: '42 unidades' },
      { name: 'Medias térmicas', quantity: '60 pares' }
    ],
    status: 'En tránsito',
    assigned_date: '2025-10-12',
    expected_delivery: '2025-10-17',
    delivery_truck: {
      plate: 'BB 002 CC',
      driver: 'María Gómez',
      phone: '+34 912 345678',
      location: {
        lat: -34.5650,
        lng: -58.4380,
        address: 'Av. Santa Fe 3500, Palermo'
      }
    },
    location: {
      lat: -34.5789,
      lng: -58.4450,
      address: 'Calle Honduras, Belgrano'
    },
    impact: 'Beneficiará a 95 personas'
  },
  {
    id: 'assigned-3',
    donation_id: 'donation-3',
    entity_id: 'entity-1',
    donor_name: 'ONG Solidaria',
    donor_category: 'Organización',
    title: 'Útiles Escolares',
    items: [
      { name: 'Cuadernos', quantity: '200 unidades' },
      { name: 'Lápices', quantity: '500 unidades' },
      { name: 'Mochilas', quantity: '100 unidades' }
    ],
    status: 'Entregada',
    assigned_date: '2025-10-10',
    delivery_date: '2025-10-15',
    received_by: 'Directora María López',
    photos: [
      'https://images.unsplash.com/photo-1427504494785-66a17e28da40?w=400&h=300&fit=crop',
      'https://images.unsplash.com/photo-1427504494785-66a17e28da40?w=400&h=300&fit=crop'
    ],
    impact: 'Beneficiará a 120 niños'
  },
  {
    id: 'assigned-4',
    donation_id: 'donation-4',
    entity_id: 'entity-1',
    donor_name: 'Carlos Mendez',
    donor_category: 'Sembrador',
    title: 'Medicamentos y Vitaminas',
    items: [
      { name: 'Vitaminas C', quantity: '100 unidades' },
      { name: 'Analgésicos', quantity: '50 cajas' }
    ],
    status: 'Confirmada',
    assigned_date: '2025-10-15',
    expected_delivery: '2025-10-19',
    delivery_truck: {
      plate: 'CC 003 DD',
      driver: 'Juan López',
      phone: '+34 913 456789',
      location: {
        lat: -34.5750,
        lng: -58.4250,
        address: 'Diagonal Norte, Centro'
      }
    },
    location: {
      lat: -34.5900,
      lng: -58.4300,
      address: 'Av. Acoyte, Caballito'
    },
    impact: 'Beneficiará a 80 personas'
  }
]

export const donationStatuses = [
  { status: 'Asignada', color: '#3498db', description: 'Donación asignada a la entidad' },
  { status: 'Confirmada', color: '#2ecc71', description: 'Confirmada y en preparación' },
  { status: 'En tránsito', color: '#f39c12', description: 'En camino a la entidad' },
  { status: 'Entregada', color: '#27ae60', description: 'Recibida y confirmada' },
  { status: 'Rechazada', color: '#e74c3c', description: 'No se pudo completar' }
]

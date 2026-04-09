export const activeDeliveries = [
  {
    id: 'delivery-1',
    donationId: 'donation-1',
    donationTitle: 'Alimentos - Fundación Despierta',
    status: 'En tránsito',
    truck: {
      id: 'truck-1',
      plate: 'AA 001 BB',
      driver: 'Carlos Rodríguez',
      phone: '+34 911 234567',
      location: {
        lat: -34.6037,
        lng: -58.3816,
        address: 'Av. 9 de Julio, CABA'
      }
    },
    destination: {
      lat: -34.5989,
      lng: -58.4395,
      address: 'Av. Corrientes 4500, Villa Crespo, CABA'
    },
    estimatedTime: '15 minutos',
    lastUpdate: '2025-10-15T15:05:00',
    progress: 65
  },
  {
    id: 'delivery-2',
    donationId: 'donation-2',
    donationTitle: 'Ropa - Red Solidaria Norte',
    status: 'En tránsito',
    truck: {
      id: 'truck-2',
      plate: 'BB 002 CC',
      driver: 'María Gómez',
      phone: '+34 912 345678',
      location: {
        lat: -34.5700,
        lng: -58.4650,
        address: 'Calle Honduras, Belgrano'
      }
    },
    destination: {
      lat: -34.5622,
      lng: -58.4561,
      address: 'Av. Cabildo 2100, Belgrano, CABA'
    },
    estimatedTime: '25 minutos',
    lastUpdate: '2025-10-15T14:50:00',
    progress: 40
  },
  {
    id: 'delivery-3',
    donationId: 'donation-3',
    donationTitle: 'Útiles Escolares - Escuela Puentes',
    status: 'Confirmada',
    truck: {
      id: 'truck-3',
      plate: 'CC 003 DD',
      driver: 'Juan López',
      phone: '+34 913 456789',
      location: {
        lat: -34.9200,
        lng: -57.9500,
        address: 'La Plata, Buenos Aires'
      }
    },
    destination: {
      lat: -34.9205,
      lng: -57.9534,
      address: 'Calle 1 y 45, La Plata, Buenos Aires'
    },
    estimatedTime: 'Mañana 10:00 AM',
    lastUpdate: '2025-10-15T14:00:00',
    progress: 0
  }
]

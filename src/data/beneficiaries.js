export const beneficiaryEntities = [
  {
    id: 'entity-1',
    name: 'Fundación Despierta',
    category: 'Alimentación',
    description: 'Comedor comunitario que asiste a familias en situación de vulnerabilidad',
    location: {
      address: 'Av. Corrientes 4500, Villa Crespo, CABA',
      lat: -34.5989,
      lng: -58.4395
    },
    contacts: 60,
    image: '/donation-images/fundacion-despierta.svg',
    verified: true,
    rating: 4.8
  },
  {
    id: 'entity-2',
    name: 'Red Solidaria Norte',
    category: 'Ropa y Abrigo',
    description: 'Organización que distribuye prendas de abrigo a personas en situación de calle',
    location: {
      address: 'Av. Cabildo 2100, Belgrano, CABA',
      lat: -34.5622,
      lng: -58.4561
    },
    contacts: 45,
    image: 'https://images.unsplash.com/photo-1509099836639-18ba1795216d?w=900&h=650&fit=crop',
    verified: true,
    rating: 4.6
  },
  {
    id: 'entity-3',
    name: 'Escuela Puentes',
    category: 'Educación',
    description: 'Institución educativa que brinda apoyo escolar a niños de sectores vulnerables',
    location: {
      address: 'Calle 1 y 45, La Plata, Buenos Aires',
      lat: -34.9205,
      lng: -57.9534
    },
    contacts: 120,
    image: 'https://images.unsplash.com/photo-1427504494785-66a17e28da40?w=900&h=650&fit=crop',
    verified: true,
    rating: 4.9
  },
  {
    id: 'entity-4',
    name: 'Casa Hogar Esperanza',
    category: 'Asistencia Social',
    description: 'Albergue para personas adultas mayores sin hogar',
    location: {
      address: 'San Justo, Buenos Aires',
      lat: -34.7649,
      lng: -58.5608
    },
    contacts: 30,
    image: 'https://images.unsplash.com/photo-1559027615-cd2628902d4a?w=900&h=650&fit=crop',
    verified: true,
    rating: 4.7
  },
  {
    id: 'entity-5',
    name: 'Clínica Comunitaria San Martín',
    category: 'Salud',
    description: 'Centro de atención médica para poblaciones de bajos recursos',
    location: {
      address: 'Av. Rivadavia 3200, Caballito, CABA',
      lat: -34.6234,
      lng: -58.4414
    },
    contacts: 80,
    image: 'https://images.unsplash.com/photo-1576091160550-112173f31c74?w=900&h=650&fit=crop',
    verified: true,
    rating: 4.8
  }
]

export const donationCategories = [
  {
    id: 'cat-1',
    name: 'Alimentos',
    subcategories: ['Alimentos secos', 'Alimentos frescos', 'Bebidas', 'Lácteos'],
    icon: '🍎'
  },
  {
    id: 'cat-2',
    name: 'Ropa y Accesorios',
    subcategories: ['Ropa infantil', 'Ropa adultos', 'Mantas y cobijas', 'Calzado'],
    icon: '👕'
  },
  {
    id: 'cat-3',
    name: 'Educación',
    subcategories: ['Libros', 'Útiles escolares', 'Material didáctico', 'Computadoras'],
    icon: '📚'
  },
  {
    id: 'cat-4',
    name: 'Salud y Higiene',
    subcategories: ['Medicamentos', 'Equipos médicos', 'Productos de higiene', 'Vitaminas'],
    icon: '💊'
  },
  {
    id: 'cat-5',
    name: 'Muebles y Enseres',
    subcategories: ['Camas', 'Sillas y mesas', 'Colchones', 'Otros'],
    icon: '🛏️'
  }
]

export const donationStatuses = [
  'Pendiente',
  'Confirmada',
  'En tránsito',
  'Entregada',
  'Rechazada'
]

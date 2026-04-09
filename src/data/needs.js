export const materialNeeds = [
  {
    id: 'need-1',
    entity_id: 'entity-1',
    title: 'Alimentos no perecederos',
    description: 'Necesitamos arroz, lentejas, aceite y leche en polvo para el comedor comunitario',
    category: 'Alimentos',
    subcategory: 'Alimentos secos',
    quantity: 100,
    unit: 'kg',
    priority: 'Alta',
    status: 'Activa',
    created_at: '2025-10-01',
    deadline: '2025-11-15',
    image_url: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 'need-2',
    entity_id: 'entity-1',
    title: 'Mantas y frazadas',
    description: 'Mantas térmicas de buena calidad para el invierno',
    category: 'Ropa y Abrigo',
    subcategory: 'Mantas y cobijas',
    quantity: 50,
    unit: 'unidades',
    priority: 'Media',
    status: 'Activa',
    created_at: '2025-10-05',
    deadline: '2025-10-31',
    image_url: 'https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?w=400&h=300&fit=crop'
  },
  {
    id: 'need-3',
    entity_id: 'entity-1',
    title: 'Equipos médicos',
    description: 'Tensiómetros, glucómetros y termómetros digitales',
    category: 'Salud y Higiene',
    subcategory: 'Equipos médicos',
    quantity: 20,
    unit: 'unidades',
    priority: 'Alta',
    status: 'Parcialmente cubierta',
    created_at: '2025-09-20',
    deadline: '2025-12-31',
    image_url: 'https://images.unsplash.com/photo-1576091160550-112173f31c74?w=400&h=300&fit=crop'
  },
  {
    id: 'need-4',
    entity_id: 'entity-1',
    title: 'Útiles escolares',
    description: 'Cuadernos, lápices, lapiceras y mochilas para niños',
    category: 'Educación',
    subcategory: 'Útiles escolares',
    quantity: 200,
    unit: 'unidades',
    priority: 'Media',
    status: 'Cubierta',
    created_at: '2025-08-15',
    deadline: '2025-09-30',
    image_url: 'https://images.unsplash.com/photo-1488190211105-8342361f853f?w=400&h=300&fit=crop'
  },
  {
    id: 'need-5',
    entity_id: 'entity-1',
    title: 'Productos de higiene',
    description: 'Jabón, champú, desodorante y elementos de higiene personal',
    category: 'Salud y Higiene',
    subcategory: 'Productos de higiene',
    quantity: 150,
    unit: 'unidades',
    priority: 'Alta',
    status: 'Activa',
    created_at: '2025-10-07',
    deadline: '2025-11-30',
    image_url: 'https://images.unsplash.com/photo-1585441371265-9733b3ef3bda?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=400&q=80'
  }
]

export const beneficiaryCategories = [
  { id: 'cat-1', name: 'Alimentos', subcategories: ['Alimentos secos', 'Alimentos frescos', 'Bebidas', 'Lácteos'] },
  { id: 'cat-2', name: 'Ropa y Abrigo', subcategories: ['Ropa infantil', 'Ropa adultos', 'Mantas y cobijas', 'Calzado'] },
  { id: 'cat-3', name: 'Educación', subcategories: ['Libros', 'Útiles escolares', 'Material didáctico', 'Computadoras'] },
  { id: 'cat-4', name: 'Salud y Higiene', subcategories: ['Medicamentos', 'Equipos médicos', 'Productos de higiene', 'Vitaminas'] },
  { id: 'cat-5', name: 'Muebles y Enseres', subcategories: ['Camas', 'Sillas y mesas', 'Colchones', 'Otros'] }
]

export const priorityLevels = ['Baja', 'Media', 'Alta', 'Crítica']
export const needStatuses = ['Activa', 'Parcialmente cubierta', 'Cubierta', 'Cancelada']

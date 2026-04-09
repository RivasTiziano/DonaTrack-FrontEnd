import { Heart, Users, Building2 } from 'lucide-react'

export const registrationOptions = [
  {
    id: 'donor-human',
    title: 'Soy una persona donante',
    description: 'Registro como persona humana para realizar donaciones',
    icon: Heart,
    path: '/registro/donante-humano'
  },
  {
    id: 'donor-organization',
    title: 'Soy una organización donante',
    description: 'Registro como empresa, ONG u otra institución',
    icon: Building2,
    path: '/registro/donante-organizacion'
  },
  {
    id: 'beneficiary',
    title: 'Soy una entidad beneficiaria',
    description: 'Registro como institución para recibir donaciones',
    icon: Users,
    path: '/registro/entidad-beneficiaria'
  }
]

export const donorOrganizationTypes = [
  { value: 'gubernamental', label: 'Gubernamental' },
  { value: 'ong', label: 'ONG' },
  { value: 'empresa', label: 'Empresa' },
  { value: 'institucion', label: 'Institución' }
]

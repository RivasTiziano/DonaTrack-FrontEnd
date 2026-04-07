# DonaTrack

DonaTrack es una interfaz web para visualizar y seguir donaciones con foco en transparencia e impacto social. La aplicación combina una landing informativa, un mapa interactivo y una vista de detalle para explorar entregas, beneficiarios y trazabilidad de cada donación.

## Badges

El proyecto no tiene CI o publicación automática configurada todavía, así que no hay badges de build o versión enlazados en este momento. Si más adelante se agrega GitHub Actions o un flujo de despliegue, esta sección puede incluir badges de estado.

## Demo y capturas

No hay una versión pública desplegada ni capturas versionadas en el repositorio por ahora. Para probar la app localmente, ejecútala con Vite y abre la ruta principal en el navegador.

Rutas principales:

- `/` para la landing page.
- `/explorar-donaciones` para el mapa interactivo.
- `/explorar-donaciones/:donationId` para el detalle de una donación.

## Tecnologías

- React 19
- React Router DOM 7
- React Leaflet 5
- Leaflet
- Lucide React
- Vite

## Requisitos Previos

- Node.js 18 o superior
- npm 9 o superior
- Git para clonar el repositorio

## Instalación

1. Clona el repositorio:

   git clone <URL_DEL_REPOSITORIO>
   cd frontDonaTrack

2. Instala las dependencias:

   npm install

3. Inicia el servidor de desarrollo:

   npm run dev

4. Abre la URL que aparece en la terminal, normalmente http://localhost:5173.

## Uso

### Ejecutar en desarrollo

Inicia la app con:

	npm run dev

Luego navega por estas vistas:

- La landing muestra la propuesta de valor, estadísticas y donaciones destacadas.
- El mapa permite explorar donaciones geolocalizadas y abrir el detalle de cada una.
- La vista de detalle muestra entidad beneficiaria, ubicación, fecha, artículos y resumen de impacto.

### Verificar calidad

Ejecuta lint antes de enviar cambios:

	npm run lint

### Generar build de producción

Compila la aplicación con:

	npm run build

### Previsualizar el build

Sirve la versión compilada localmente con:

	npm run preview

## Estructura general

- src/App.jsx: definición de rutas.
- src/pages/LandingPage.jsx: página de inicio.
- src/pages/MapView.jsx: mapa interactivo de donaciones.
- src/pages/DonationDetailPage.jsx: detalle individual de una donación.
- src/components/: componentes reutilizables de navegación y mapa.
- src/data/: datos de contenido y donaciones de ejemplo.
- src/styles/: estilos base, layout, componentes y páginas.

## Notas

- El mapa depende de los estilos de Leaflet para renderizar correctamente.
- El proyecto ya pasa npm run lint de forma local.

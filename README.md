# DonaTrack — Frontend SSR / Cliente Liviano (DDSI · UTN FRBA)

**DonaTrack** es una plataforma integral de trazabilidad, matchmaking logístico, gamificación y transparencia para la distribución comunitaria de donaciones hacia comedores, escuelas y hogares.

Este repositorio implementa el **Cliente Liviano web** bajo arquitectura **Server-Side Rendering (SSR)** con **Spring Boot 3.2.4**, **Java 21/17**, **Thymeleaf**, Vanilla CSS/JS y **Leaflet.js**, conectado en tiempo real con los **5 microservicios del backend** (Autenticación, Donaciones, Logística, Incentivos y Notificaciones).

> **Documentación técnica en `docs/`:**
> - [Integración MVC con el backend (Entrega 5)](docs/ENTREGA5-MVC.md) · [Controladores MVC](docs/CONTROLLERS-MVC.md)
> - [Rate Limiting y propagación de IP real (`X-Client-IP` / `X-Front-Auth`)](docs/rate-limiting.md)
> - [Observabilidad distribuida (OpenTelemetry + Jaeger)](docs/observabilidad.md)
> - [Monitoreo y probes (`/livez`, `/readyz`, `/actuator/prometheus`)](docs/monitoreo.md)

---

## 1. Producción y URLs en la Nube (AWS EC2 + Caddy HTTPS)

| Componente | Dirección en Producción |
| :--- | :--- |
| **Web Pública / Frontend SSR** | `https://15-229-30-7.sslip.io` (y `http://15.229.30.7`) |
| **API Gateway (REST / GraphQL / Auth)** | `https://15-229-30-7.sslip.io/api/...`, `/graphql`, `/auth/login` |
| **Webhook Planificador de Rutas** | `https://15-229-30-7.sslip.io/webhooks/routing` |
| **Observabilidad y Monitoreo** | Solo `127.0.0.1` en la EC2 (`Jaeger :16686`, `Grafana :3000`, `Prometheus :9091`) vía Túnel SSH |

---

## 2. Cuentas y Datos Precargados para Probar Todo

El inicio de sesión es **100% real** contra `autenticacion-service` (`JWT HS256`), manteniendo los tokens en la sesión HTTP del servidor y entregando al navegador únicamente una cookie `HttpOnly`.

### Cuentas listas para usar en Producción (`https://15-229-30-7.sslip.io/login`)

| Rol | Email | Contraseña | Panel / Uso |
| :--- | :--- | :--- | :--- |
| **Persona Administradora** | `admin@donatrack.org` | *(ver `docs/CREDENCIALES.md` del backend)* | `/admin/dashboard` (CSV, depósito, matchmaking, rutas, rankings) |
| **Persona Donante (Demo)** | `donante.demo@donatrack.org` | `DonaTrackDemo2026!` | `/donante/dashboard` (donaciones, mapa GPS, incentivos, notifs) |
| **Persona Donante (Líder)** | `donante.lider@donatrack.org` | `DonaTrackDemo2026!` | `/donante/dashboard` (métricas acumuladas e historial) |
| **Entidad Beneficiaria 1** | `esperanza.demo@donatrack.org` | `DonaTrackDemo2026!` | `/entidad/dashboard` (necesidades, mapa GPS, confirmar con foto) |
| **Entidad Beneficiaria 2** | `hogarsol.demo@donatrack.org` | `DonaTrackDemo2026!` | `/entidad/dashboard` (matchmaking multi-entidad y Error 3 `NO_RECIBIDA`) |

### Datos de prueba ya cargados en AWS
- **20.000+ donantes** en el padrón con **paginación server-side** (`/admin/dashboard/donantes`) y **buscador asíncrono JSON** (`/admin/dashboard/donantes/buscar`) en el alta de donaciones.
- **13 entregas públicas** en la landing (`/`) con fotos `.webp` y ubicaciones en el mapa Leaflet (CABA + GBA).
- **3 donaciones de respaldo `EN_DEPOSITO`** (`Fideos secos tallarines`, `Sillas escolares usadas`, `Leche larga vida entera`) listas para ejecutar matchmaking en `/admin/dashboard/sugerencias`.
- **Necesidades activas** (`EXTRAORDINARIA` y `RECURRENTE` `P7D`) en *Comedor Esperanza* y *Hogar del Sol* (incluyendo `6/30` parcial y `100/100` satisfecha).
- **3 camiones** (`DTK802` `EN_RUTA`, `DTK501` `DISPONIBLE`, `E6T217` `DISPONIBLE`) y **1 entrega en camino (`EN_TRASLADO`)** con coordenadas GPS activas para ver el mapa en tiempo real en `/admin/dashboard/seguimiento`, `/donante/dashboard/entregas` y `/entidad/dashboard/entregas`, y confirmar recepción con fotografía en `/entidad/dashboard/confirmar`.
- **Centro de notificaciones:** muestra las notificaciones reales enviadas por Gmail SMTP (`COMPLETADA`) y registradas en plataforma.

---

## 3. Mapa de Rutas Reales de la Aplicación

### Rutas Públicas (sin sesión)
- `/` — Landing page con métricas, galería de entregas confirmadas con foto y mapa público Leaflet (`OpenStreetMap`)
- `/legal`, `/privacidad` — Información legal
- `/login` — Inicio de sesión unificado
- `/registro`, `/registro/donante-humano`, `/registro/donante-organizacion`, `/registro/entidad-beneficiaria` — Registro público conectado a backend (con compensación si falla el alta de cuenta)
- `/primer-acceso` — Activación de contraseña mediante token temporal recibido tras alta por Admin/CSV

### Panel Persona Donante (`ROLE_DONANTE_HUMANO` / `ROLE_DONANTE_JURIDICO`)
- `/donante/dashboard` — Resumen de impacto (`totalDonacionesHistoricas`, `impactoAcumulado`, `totalOrganizacionesAyudadas`) e insignias
- `/donante/dashboard/donaciones` — Historial de donaciones con filtros por estado/categoría
- `/donante/dashboard/entidades` — Explorador de entidades beneficiarias
- `/donante/dashboard/entregas` — Mapa interactivo Leaflet con seguimiento GPS y auto-actualización cada 60 s
- `/donante/dashboard/incentivos` — Stepper de categorías, misiones, insignias y auditoría de ascensos
- `/donante/dashboard/notificaciones` — Centro de notificaciones propias (`marcar leída` / `marcar todas`)

### Panel Entidad Beneficiaria (`ROLE_BENEFICIARIA`)
- `/entidad/dashboard` — Resumen operativo y cobertura
- `/entidad/dashboard/necesidades` — Alta y gestión de necesidades `EXTRAORDINARIA` y `RECURRENTE` (`P1W`, `P1M`, `P3M`)
- `/entidad/dashboard/donaciones` — Donaciones asignadas
- `/entidad/dashboard/entregas` — Mapa interactivo de entregas entrantes con seguimiento GPS
- `/entidad/dashboard/confirmar` — Confirmación de recepción (`ENTREGADA` con fotografía) o reporte de fallo (`NO_RECIBIDA` con motivo)
- `/entidad/dashboard/notificaciones` — Centro de notificaciones de la entidad

### Panel Persona Administradora (`ROLE_ADMIN`)
- `/admin/dashboard` — Tablero general (consultas paralelas en `PanelAdminService`)
- `/admin/dashboard/donantes`, `/admin/dashboard/donantes/nuevo/{humanos|juridicos}`, `/admin/dashboard/importar` — Padrón paginado, alta y migración masiva CSV (>20k)
- `/admin/dashboard/beneficiarios`, `/admin/dashboard/necesidades`, `/admin/dashboard/catalogo` — Entidades, necesidades y catálogo
- `/admin/dashboard/donaciones`, `/admin/dashboard/donaciones/nueva`, `/admin/dashboard/bienes` — Depósito central y recepción multi-bien con buscador asíncrono
- `/admin/dashboard/sugerencias`, `/admin/dashboard/asignaciones` — Ejecución manual del evaluador de matchmaking (`Semantico`/`Subatendidos`) y confirmaciones
- `/admin/dashboard/camiones`, `/admin/dashboard/entregas`, `/admin/dashboard/rutas`, `/admin/dashboard/paradas`, `/admin/dashboard/seguimiento` — Flota, planificación de rutas y mapa GPS
- `/admin/dashboard/incentivos`, `/admin/dashboard/incentivos/donantes`, `/admin/dashboard/rankings`, `/admin/dashboard/incentivos/historial` — Categorías, misiones y rankings mensuales
- `/admin/dashboard/notificaciones` — Diagnóstico de conexión

---

## 4. Cumplimiento de Usabilidad y Optimizaciones Clave

- **Arquitectura de información $\le 3$ niveles**, breadcrumbs semánticos, design tokens unificados (`clean-ui.css`), activos **Leaflet** y **Lucide** auto-hospedados en `/vendor/`, contraste WCAG AA y manejo accesible de errores (`403`, `429` en `error/429.html`).
- **Rendimiento con 20.000+ donantes:** paginación server-side (`?q=&tipo=&page=&size=`) y selector asíncrono JSON (`/admin/dashboard/donantes/buscar`) construido con nodos DOM seguros (sin `innerHTML`), bajando `/admin/dashboard/donantes` de `37.5 s / 41.4 MB` a **`~1.2–1.6 s / <60 KB`**.
- **Validaciones en doble capa:** restricciones HTML5 (`min` en fechas de vencimiento perecederas y cantidades mínimas) sincronizadas con las validaciones del backend.
- **Propagación de IP real y Trazas:** `IpDelNavegadorInterceptor` reenvía `X-Client-IP` + `X-Front-Auth` hacia los microservicios para que **Bucket4j** aplique cuotas por usuario final detrás del proxy de Render; el agente Java OpenTelemetry propaga `traceparent` hacia **Jaeger**.

---

## 5. Ejecución Local y Pruebas

### Requisitos
* **JDK 21** (o 17) y **Maven 3.8+**.
* **Backend DonaTrack** corriendo localmente (`http://localhost:8080`..`8084`) o apuntado vía `.env`.

### Pasos de Ejecución
```bash
cp .env.example .env
mvn spring-boot:run
# Abrir en http://localhost:8090
```

Con Docker Compose (junto a la red del backend):
```bash
docker compose up -d --build
# Con observabilidad y monitoreo:
docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml -f docker-compose.monitoreo.yml up -d --build
```

### Tests Automáticos
```bash
mvn test
```

---

## 6. Estructura del Proyecto

```text
DonaTrack-FrontEnd/
├── pom.xml                                  # Spring Boot 3.2.4, Thymeleaf, Actuator/Prometheus
├── Dockerfile                               # Imagen multi-stage Java 21 + agente OTel
├── docker-compose*.yml                      # Perfiles local, observabilidad, monitoreo y AWS
├── docs/                                    # Documentación MVC, Rate Limiting, Monitoreo y OTel
└── src/
    ├── main/
    │   ├── java/ar/edu/utn/donatrack/
    │   │   ├── config/                      # AccesoInterceptor, WebClient/RestTemplate
    │   │   ├── controllers/                 # Web, Autenticacion, Donaciones, Logistica, Incentivos, Notificaciones, Acceso, PrimerAcceso, Admin
    │   │   ├── services/                    # Lógica de paneles, API clients e IpDelNavegadorInterceptor
    │   │   ├── mappers/ & validators/       # Formateo seguro de vistas y validaciones de formularios
    │   │   └── dto/ & forms/                # Contratos tipados sin acoplamiento JPA
    │   └── resources/
    │       ├── application.properties       # Puerto 8090, URLs APIs, AUTH_FRONT_KEY y probes Actuator
    │       ├── static/{css,js,img,vendor}/  # Design system, paginación/buscador JS, mapa público y Leaflet/Lucide
    │       └── templates/                   # Vistas Thymeleaf SSR y fragments por rol + error/429.html
    └── test/java/ar/edu/utn/donatrack/      # Suite MockMvc y tests unitarios (283 tests)
```

---

## 👥 Equipo de Desarrollo — Grupo 18 (2026)
* **Materia:** Diseño de Sistemas de Información — UTN FRBA
* **Entrega:** Entrega Final / Entrega 6 (Integración Cloud, Observabilidad, Outbox, GraphQL y Rate Limiting)

# Entrega 5 — cliente liviano MVC con autenticación

## Ejecución

Usar JDK 17 o superior. Desde este proyecto: `mvn spring-boot:run`.
Abrir http://localhost:8090/login. Con Docker: `docker compose up -d --build` (requiere el backend levantado en la red `2026-tpa-ma-ma-grupo-18_default`).

Variables opcionales:

| Variable | Valor predeterminado |
| --- | --- |
| FRONTEND_PORT | 8090 |
| DONACIONES_API_URL | http://localhost:8080 |
| LOGISTICA_API_URL | http://localhost:8083 |
| INCENTIVOS_API_URL | http://localhost:8082 |
| NOTIFICACIONES_API_URL | http://localhost:8081 |
| AUTENTICACION_API_URL | http://localhost:8084 |
| NOTIFICACIONES_ENVIO_MANUAL_HABILITADO | false |

Notificaciones tiene comprobación de conexión y envío manual local en
`/admin/dashboard/notificaciones`. El envío manual permanece deshabilitado
por defecto; habilitarlo solo para pruebas locales controladas.

## Autenticación y sesión

El login llama a `POST /auth/login` del servicio de autenticación. Los tokens quedan en la
sesión HTTP (solo por cookie) y `WebApiCallerService` agrega `Authorization: Bearer` a cada
llamada, refrescando el access token un minuto antes de vencer. `/admin/**`, `/donante/**` y
`/entidad/**` exigen el rol correspondiente (`AccesoInterceptor`); sin sesión redirigen al login.
Los paneles personales toman siempre al dueño del `referenciaId` de la sesión.

El primer admin lo crea el backend con `AUTH_ADMIN_EMAIL` / `AUTH_ADMIN_PASSWORD`. Las cuentas de
donantes y entidades se crean desde "Crear acceso" en el listado del admin (`POST /auth/usuarios`),
con el email del perfil; la contraseña inicial se comunica por fuera del sistema hasta que exista
el flujo de primer acceso del backend.

## Arquitectura

Navegador → Controller MVC → Service del frontend → ApiService → WebApiCallerService → REST.
Los validators comprueban la entrada; los mappers transforman formularios y respuestas.
La organización se detalla en [CONTROLLERS-MVC.md](CONTROLLERS-MVC.md).
La respuesta JSON se deserializa a DTO y Thymeleaf genera HTML en el servidor.
Los formularios hacen POST al frontend; este traduce a POST, PUT, PATCH o DELETE
del contrato REST. No hay JPA, acceso a bases ni reglas de negocio en el cliente.

Las llamadas están agrupadas por microservicio: `DonacionesApiService`
(incluye donantes, CSV, bienes, catálogo, beneficiarios, necesidades y matchmaking),
`LogisticaApiService`, `IncentivosApiService` y `NotificacionesApiService`.
Todas reutilizan `WebApiCallerService`. Ya no hay un cliente separado para
consultar donantes. Ahora existe una capa de coordinación del frontend por encima
de cada servicio de API; no duplica los clientes HTTP.

La ampliación de operaciones, sus pantallas y los endpoints reservados para
integraciones se documentan en [COBERTURA-API.md](COBERTURA-API.md).

Se conserva el layout administrativo y el sistema de estilos de la entrega 4.
Las operaciones conectadas usan la plantilla compartida `admin-api` y el fragmento
de campos de bienes. Las plantillas antiguas siguen disponibles como referencia
de maquetado; no son prueba de integración funcional.

## Circuitos conectados

| Pantalla | Operaciones reales |
| --- | --- |
| Donantes | Listar, crear y eliminar humanos/jurídicos; consultar y editar datos básicos conservando contactos y representantes |
| Donaciones | Listar, crear con varios bienes, detalle/asignaciones/contactos, cambiar estado de cobertura, eliminar |
| Bienes | Listar, detalle, crear, actualizar, eliminar |
| Catálogo | Listar categorías/subcategorías, crear categoría y subcategoría |
| Beneficiarios | Listar, registrar, consultar/editar datos básicos, eliminar; asignaciones por entidad y estado |
| Necesidades | Listar, detalle, crear extraordinarias/recurrentes, actualizar, eliminar |
| Matchmaking | Activar/desactivar algoritmos, generar/listar/consultar sugerencias y tops; evaluador manual; confirmar asignación |
| Camiones | Listar, detalle, crear, cambiar estado, eliminar; consultar ubicaciones GPS y avance |
| Entregas | Listar y detalle con asignación, estado y referencias de fotografías |
| Rutas | Listar, detalle/paradas, planificar por fecha, iniciar con chofer, finalizar, cancelar |
| Incentivos | Gestión de categorías y misiones; consulta administrativa de métricas, progreso, misiones e insignias por donante |
| Rankings | Ranking mensual e historial de rankings guardados |
| CSV | Subir `archivo` como multipart, mostrar cantidades y errores por fila, acceso al listado resultante |

Las asignaciones requieren Logística, MongoDB y sus demás dependencias disponibles.
Los eventos requieren RabbitMQ y consumidores para comprobar efectos asincrónicos.
CSV: límite frontend de 20 MB por archivo y 21 MB por solicitud. El backend puede
aplicar sus propios límites. No se envían credenciales ni correos ficticios.
Los errores de comunicación no se convierten en listados vacíos exitosos y las
escrituras no se reintentan automáticamente. En caso de timeout puede haberse
procesado una operación: consultar su resultado antes de repetirla.

Los DTO de bienes recibidos no incluyen el ID de subcategoría: en edición se pide
seleccionarla explícitamente, sin deducir IDs por nombres ni inventarlos.

## Pendientes que dependen del backend

Detalle en [PLAN-DESMAQUETADO-E5.md](PLAN-DESMAQUETADO-E5.md) (sección 10) y [RETOMAR-E5.md](RETOMAR-E5.md).

1. **C1 — Donaciones del donante.** `GET /donaciones` es solo admin; "Mis donaciones" y el seguimiento del donante quedan vacíos (requisito 5).
2. **X1/C3 — entregaId para la entidad.** La confirmación de recepción pide el número de entrega a mano (requisito 12).
3. **C3/C4 — Seguimiento por dueño.** No se muestra la ubicación de los camiones a donante ni entidad (requisitos 9 y 14).
4. **D1 — Endpoints públicos.** Landing, mapa y detalle público muestran estados vacíos (requisitos 1 y 2).
5. **E1/E2 — Registro público y primer acceso.** El registro en línea muestra un aviso; las cuentas las crea el admin (requisitos 3 y 20).
6. **F1 — Fotos.** La recepción envía URLs de fotos; falta la subida de archivos.

## Verificación

`mvn test` ejecuta renderizado Thymeleaf mediante MockMvc y contratos HTTP con un
servidor local de prueba, incluyendo multipart, PATCH, DELETE, campos de creación,
errores, listas vacías, montos de asignación y ranking real.
No necesita bases ni envía correos o eventos reales. La prueba E2E contra los
microservicios levantados y la revisión visual en navegador deben hacerse aparte.

Prueba manual: crear donante → crear categoría/subcategoría → registrar donación
→ registrar necesidad → generar sugerencia → asignar una cantidad parcial →
ver disponible y estado de la asignación en el detalle → importar CSV → revisar donantes.

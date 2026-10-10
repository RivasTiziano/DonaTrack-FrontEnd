# Entrega 5 — avance MVC sin autenticación

## Ejecución

Usar JDK 17 o superior. Desde este proyecto: `mvn spring-boot:run`.
Abrir http://localhost:8090/admin/dashboard.

Variables opcionales:

| Variable | Valor predeterminado |
| --- | --- |
| FRONTEND_PORT | 8090 |
| DONACIONES_API_URL | http://localhost:8080 |
| LOGISTICA_API_URL | http://localhost:8083 |
| INCENTIVOS_API_URL | http://localhost:8082 |
| NOTIFICACIONES_API_URL | http://localhost:8081 |
| NOTIFICACIONES_ENVIO_MANUAL_HABILITADO | false |

Notificaciones tiene comprobación de conexión y envío manual local en
`/admin/dashboard/notificaciones`. El historial personal y las marcas de lectura
requieren nuevos endpoints e identidad autorizada en el backend: ver
[NOTIFICACIONES.md](NOTIFICACIONES.md). El envío manual permanece deshabilitado
por defecto; habilitarlo solo para pruebas locales controladas.

No exponer este frontend a Internet: aún no hay autenticación ni autorización.
La selección de rol del login original es una demostración, no una sesión segura.

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

## Pasos distintos que quedan para trabajar juntos

1. **Identidad y alcance de los paneles personales.** No se conecta “mis donaciones”,
   “mis necesidades” ni “mis incentivos” usando IDs fijos o el primer usuario encontrado.
   Definir identidad real al abordar autenticación; hasta entonces esos paneles son maquetado.
2. **Edición de colecciones de perfiles.** La creación existente de donantes admite
   contactos y representantes adicionales. La nueva edición básica conserva esas
   colecciones, pero no incluye un editor para reemplazarlas ni gestionar sus IDs.
3. **Donaciones con varios bienes.** Ya se registran varios bienes por envío;
   el backend sigue siendo responsable de su segmentación.
4. **Recepción con fotos.** La planificación administrativa por fecha y consulta de
   GPS ya están conectadas. Falta la identidad y autorización de quien confirma
   recepción y carga evidencias. No se modifican estados de asignación desde un
   formulario que suplante a Logística.
5. **Historial de notificaciones.** El backend actual solo expone enviar notificación
   y health, no listar/marcar leído. Necesita definir un contrato antes de conectar las vistas.
6. **Mapa y transparencia pública.** Definir datos autorizados para publicación y
   su composición entre Donaciones y Logística. Landing, mapa y detalles públicos
   originales continúan mostrando maquetado, no resultados reales de este avance.
7. **Autenticación.** No se crean cuentas, tokens, JWT ni se publica CuentaCreada.
   El login actual debe reemplazarse antes de considerar terminada la entrega 5.

La entrega 5 NO está completa. Esta implementación resuelve la base y los circuitos
repetibles descritos arriba, no los pasos pendientes ni la totalidad de los CRUD.

## Verificación

`mvn test` ejecuta renderizado Thymeleaf mediante MockMvc y contratos HTTP con un
servidor local de prueba, incluyendo multipart, PATCH, DELETE, campos de creación,
errores, listas vacías, montos de asignación y ranking real.
No necesita bases ni envía correos o eventos reales. La prueba E2E contra los
microservicios levantados y la revisión visual en navegador deben hacerse aparte.

Prueba manual: crear donante → crear categoría/subcategoría → registrar donación
→ registrar necesidad → generar sugerencia → asignar una cantidad parcial →
ver disponible y estado de la asignación en el detalle → importar CSV → revisar donantes.

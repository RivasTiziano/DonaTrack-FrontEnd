# Plan de desmaquetado — Entrega 5 (frontend)

Plan de ejecución para un agente implementador. Contrato del backend verificado el 2026-10-12 contra `../2026-tpa-ma-ma-grupo-18` (rama `ENTREGA-5`). La estética queda fuera de alcance: solo se cambian datos, conexiones y la estructura mínima de las plantillas.

---

## 0. Reglas para quien implemente

1. **No inventar datos.** Cuando la API no da un dato, va `null` o una lista vacía, y la plantilla lo oculta o muestra "—". Prohibido: fotos de Unsplash, coordenadas fijas, nombres ficticios ("Chofer Asignado", "Sede Central"), `"CUIT-" + id`, puntajes, ratings y ETAs inventados.
2. **Conservar las claves del modelo** que ya usan las plantillas (`userDonations`, `needs`, `assignedDonations`, etc., listadas en cada fase). Así las vistas casi no cambian. Si una clave deja de tener datos reales, se pasa `null` y en la plantilla se agrega `th:if` para no romper el render.
3. **No cambiar estilos, CSS ni la disposición.** Solo:
   - `th:if` para nulos;
   - reemplazar `onclick` simulados por `<form method="post">`;
   - el botón "Salir" (logout).
4. Respetar la arquitectura existente: Controller → Service → ApiService → `WebApiCallerService`. Validators y mappers como hoy. Sin lógica de negocio en el front.
5. Correr `mvn test` al terminar cada fase. Ninguna fase se da por terminada con tests rojos. Si un test valida comportamiento de la maqueta que se elimina, se actualiza o se borra, y se explica en el resumen final.
6. No hacer commits salvo que el usuario lo pida.
7. Toda vista personal filtra **siempre** por el `referenciaId` de la sesión. Nunca se toma un id de dueño desde un parámetro de URL o de formulario. Hoy `GET /donaciones` y `GET /necesidades` devuelven todo a cualquier usuario autenticado.
8. Los filtrados "del lado del front" son **provisorios**. Se marcan con el comentario `// PROVISORIO: reemplazar por <endpoint> (pedido backend <código>)`, usando los códigos de la sección 10.

---

## 1. Contrato del backend (fuente de verdad)

Puertos: donaciones 8080, notificaciones 8081, incentivos 8082, logística 8083, **autenticación 8084**. Todos exigen `Authorization: Bearer <accessToken>`, salvo `/health`.

### Autenticación (8084)
- `POST /auth/login` con cuerpo `{email, password}` responde:
  `{accessToken, refreshToken, expiraEnSegundos, rol, permisos[], referenciaId}`. Ojo: el campo se llama **`expiraEnSegundos`**, no `expiresIn`.
  - Si las credenciales son incorrectas devuelve 401, con un cuerpo ProblemDetail `{title, status, detail}`.
- `POST /auth/refresh` con cuerpo `{refreshToken}` devuelve el mismo TokenResponse. Si falla, 401.
- `POST /auth/usuarios` con cuerpo `{email, password(mín. 12), rol, referenciaId}` responde 201 con `{id, email, rol, referenciaId}`.
  - Requiere `GESTIONAR_USUARIOS` (solo ADMIN). Si el email ya existe, 409.
- Duración de los tokens: access 15 min, refresh 7 días. **No existe** logout en el backend ni autoregistro.
- Roles: `ADMIN`, `DONANTE_HUMANO`, `DONANTE_JURIDICO`, `BENEFICIARIA`.
- `referenciaId` es el id del donante (humano o jurídico) o de la entidad en donaciones-service. Es el mismo valor que `donanteId` en incentivos.

### Permisos por rol
| Rol | Permisos |
|---|---|
| ADMIN | GESTIONAR_DONANTES, REGISTRAR_DONACIONES, MARCAR_DONACION_VENCIDA, ASIGNAR_DONACIONES, GESTIONAR_CAMIONES, SEGUIR_ENTREGAS, VER_RANKINGS, IMPORTAR_DONANTES_CSV, REVISAR_ENTREGAS_FALLIDAS, GESTIONAR_MISIONES, GESTIONAR_USUARIOS, VER_ENTIDADES_BENEFICIARIAS |
| DONANTE_HUMANO / DONANTE_JURIDICO | VER_DONACIONES_PROPIAS, VER_ENTIDADES_BENEFICIARIAS, VER_INCENTIVOS_PROPIOS, SEGUIR_ENTREGAS, VER_NOTIFICACIONES_PROPIAS |
| BENEFICIARIA | GESTIONAR_NECESIDADES, VER_DONACIONES_ASIGNADAS, CONFIRMAR_RECEPCION, SEGUIR_ENTREGAS, VER_NOTIFICACIONES_PROPIAS |

### Endpoints y DTO relevantes para los paneles personales
- **Donaciones (8080).** No tiene `@PreAuthorize`: cualquier token válido pasa.
  - `GET /donaciones` devuelve `[{id, donanteId, descripcionGeneral, bien, estado, fechaRecepcion}]`.
    - No acepta filtros.
    - `estado` puede ser `EN_DEPOSITO`, `ASIGNACION_REALIZADA` o `VENCIDA`.
  - `GET /donaciones/{id}/asignaciones` devuelve `[AsignacionDetalle]`.
  - `GET /beneficiarios/{id}/asignaciones?estado=` devuelve `[AsignacionDetalle]`.
  - `GET /asignaciones?entidadBeneficiariaId=&estado=` devuelve `[AsignacionDetalle]`. Ojo: `entidadBeneficiariaId` es obligatorio.
  - `AsignacionDetalle` tiene estos campos:
    `{id, donacionId, descripcionGeneralDonacion, bien, necesidadId, entidadBeneficiariaId, cantidadAsignada, fechaAsignacion, estado, camionId, fechaEntrega, justificacionFallo}`.
    - `estado` puede ser `PENDIENTE_RUTA`, `LISTA_PARA_ENTREGAR`, `EN_TRASLADO`, `ENTREGADA` o `ENTREGA_FALLIDA`.
  - `GET /beneficiarios` y `GET /beneficiarios/{id}` devuelven:
    `{id, razonSocial, tipoEntidad, rubro, direccion{calle,ciudad,provincia,numero,latitud,longitud}, medioDeContactoPredeterminado{tipoMedioContacto,formaContacto}, representantes[{id,nombre,telefono,correos[]}], activo}`.
  - `GET /necesidades` devuelve todas las necesidades, sin filtro:
    `[{entidadBeneficiariaId, necesidadId, bien, descripcion, tipoNecesidad, periodo, cantidadRecibida, cantidadRestante, estado}]`.
    - `estado` puede ser `PENDIENTE`, `EN_PROGRESO` o `SATISFECHA`.
  - `GET /donantes/humanos/{id}` y `GET /donantes/juridicos/{id}`: los DTO ya están en `DonacionesDetalles.Humano` y `DonacionesDetalles.Juridico`.
- **Incentivos (8082).** El `{id}` tiene que ser el `referenciaId` del token, salvo para ADMIN.
  - `GET /incentivos/donantes/{id}/metricas` devuelve `{totalDonacionesHistoricas, impactoAcumulado, totalOrganizacionesAyudadas, categoriaActual, desgloseMensual[{periodo,totalDonaciones,impacto,entidadesAyudadas}]}`.
  - `GET /incentivos/donantes/{id}/progreso-mision-actual` devuelve `{idMision, nombreMision, descripcionMision, valorActual, valorObjetivo, porcentajeProgreso, completada, orden}`.
    - Si el donante ya completó todas las misiones, devuelve `{mensaje}`.
  - `GET /incentivos/donantes/{id}/misiones` devuelve las completadas: `[{idMision, nombreMision, descripcionMision, fechaCompletada}]`.
  - `GET /incentivos/donantes/{id}/insignias` devuelve `[{nombre, descripcion, imagenUrl}]`. La lista viene vacía si el donante las tiene ocultas.
  - `PATCH /incentivos/donantes/{id}/insignias/visibilidad` con cuerpo `{insigniasVisibles}`.
    - Solo puede hacerlo **el propio donante**. **ADMIN recibe 403.**
  - `GET /incentivos/donantes/{id}/historial-categorias`.
  - `GET /incentivos/categorias` (con sus misiones), con `isAuthenticated()`.
  - `GET /incentivos/rankingMensual` y `GET /incentivos/rankings/historial` requieren `VER_RANKINGS` (solo ADMIN).
- **Logística (8083).**
  - `GET /entregas` y `GET /entregas/{id}` requieren `GESTIONAR_CAMIONES`: **la entidad no puede listarlas**.
  - `PATCH /entregas/{id}/estado` acepta `{estado:"ENTREGADA", fotos:[url,...]}` (al menos una foto) o `{estado:"NO_RECIBIDA", justificacion}`.
    - Permisos: `CONFIRMAR_RECEPCION` (la entidad dueña), `REVISAR_ENTREGAS_FALLIDAS` o `GESTIONAR_CAMIONES`.
    - Solo se aceptan entregas que están `EN_TRASLADO`.
  - `GET /camiones/ubicaciones` y `GET /camiones/{patente}/ubicacion` requieren `SEGUIR_ENTREGAS`. Devuelven **todos** los camiones, sin filtrar por dueño.
- **Notificaciones (8081).** La identidad sale **solo del token**: ya no se usan `?email` ni `?telefono`.
  - `GET /notificaciones?soloNoLeidas=&limite=`
  - `GET /notificaciones/no-leidas/cantidad`
  - `PATCH /notificaciones/{id}/leida`
  - `PATCH /notificaciones/leidas`
  - Todos requieren `VER_NOTIFICACIONES_PROPIAS`. Las respuestas vienen envueltas en `{retorno, datos, mensajeError}`, como hoy.

---

## Fase 1 — Sesión, login real y token en cada llamada

### 1.1 Configuración
`src/main/resources/application.properties`:
- Agregar `apis.autenticacion.url=${AUTENTICACION_API_URL:http://localhost:8084}`.
- Borrar la propiedad `app.notificaciones.historial-por-contacto-habilitado` y su comentario (lo completa la fase 2).

### 1.2 DTO de autenticación
Nuevo archivo `src/main/java/ar/edu/utn/donatrack/dto/AuthDtos.java`:
```java
public final class AuthDtos {
    private AuthDtos() {}
    public record Login(String email, String password) {}
    public record Refresh(String refreshToken) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Token(String accessToken, String refreshToken, long expiraEnSegundos,
                        String rol, List<String> permisos, Long referenciaId) {}
    public record CrearUsuario(String email, String password, String rol, Long referenciaId) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usuario(Long id, String email, String rol, Long referenciaId) {}
}
```

### 1.3 Modelo de sesión
Nuevo archivo `models/SesionUsuario.java`. Es una clase final, `Serializable`, inmutable salvo los tokens:
- Campos:
  - `String email`
  - `String rol`
  - `List<String> permisos`
  - `Long referenciaId`
  - `String nombreVisible`
  - `volatile String accessToken`
  - `volatile String refreshToken`
  - `volatile Instant expiraEn`
- Métodos:
  - `boolean tiene(String permiso)`
  - `boolean esAdmin()`
  - `boolean esDonante()` (DONANTE_HUMANO o DONANTE_JURIDICO)
  - `boolean esBeneficiaria()`
  - `String tipoDonante()`: devuelve `"humanos"` si el rol es DONANTE_HUMANO, `"juridicos"` si es DONANTE_JURIDICO, y `null` en otro caso.
  - `String panel()`: devuelve `/admin/dashboard`, `/donante/dashboard` o `/entidad/dashboard` según el rol.
  - `boolean vencePronto()`: `Instant.now().isAfter(expiraEn.minusSeconds(60))`.
  - `void renovar(AuthDtos.Token t)`: actualiza los dos tokens y `expiraEn = now + expiraEnSegundos`.
  - `static SesionUsuario desde(String email, AuthDtos.Token t, String nombreVisible)`.
- `toString()` **no** debe incluir los tokens.

### 1.4 Acceso a la sesión actual
Nuevo archivo `services/internal/SesionActual.java` (`@Component`):
```java
public static final String ATRIBUTO = "SESION_USUARIO";
public Optional<SesionUsuario> obtener()        // RequestContextHolder → HttpSession(false) → atributo
public void guardar(HttpServletRequest req, SesionUsuario s)  // req.changeSessionId() si ya existía, luego setAttribute
public void cerrar(HttpServletRequest req)      // session.invalidate()
public Long referenciaId()                       // obtener().map(...).orElseThrow(() -> new SesionRequeridaException())
```
- Si no hay request activo (por ejemplo en tests unitarios de ApiService), `obtener()` devuelve `Optional.empty()` y no lanza.
- Nueva excepción `exceptions/SesionRequeridaException extends RuntimeException`.

### 1.5 Cliente de autenticación
Nuevo archivo `services/AutenticacionApiService.java`.
- Usa **su propio** `RestClient`, creado con `RestClient.builder().build()`, **sin** el interceptor de token. Así se evita la recursión al refrescar.
- Métodos:
  - `Token login(String email, String password)`
  - `Token refrescar(String refreshToken)`
  - `Usuario crearUsuario(CrearUsuario req)`: este sí necesita el Bearer del admin. Usar `WebApiCallerService` (que ya va a tener el interceptor) contra `url + "/auth/usuarios"`.

### 1.6 Token en `WebApiCallerService`
- Agregar un `ClientHttpRequestInterceptor` interno en `WebApiCallerService`, construido con un `ObjectProvider<SesionActual>` y un `ObjectProvider<AutenticacionApiService>` opcionales. Se usan providers para evitar ciclos y para que los tests que hacen `new WebApiCallerService(RestClient.builder())` sigan compilando.
- **Mantener** el constructor actual `WebApiCallerService(RestClient.Builder)`, sin sesión, para los tests. Agregar un segundo constructor anotado con `@Autowired` que reciba los providers.
- Lógica del interceptor:
  1. Si no hay sesión, se envía sin header.
  2. Si `sesion.vencePronto()`, dentro de `synchronized (sesion)`, se llama a `auth.refrescar(refreshToken)` y luego a `sesion.renovar(...)`.
     - Si el refresh falla con 401, se lanza `SesionExpiradaException` (nueva, `extends RuntimeException`).
  3. Se agrega `Authorization: Bearer <accessToken>`.
- **No** reintentar escrituras: el comentario actual sobre escrituras sigue vigente.

### 1.7 Errores 401 y 403
- `ApiErrorMessages.describe`:
  - Leer también `detail` y `title` (formato ProblemDetail), además de `message` y `mensaje`.
  - Si el código es 401, devolver "Tu sesión venció o no es válida. Volvé a iniciar sesión."
  - Si el código es 403, devolver "Tu usuario no tiene permiso para esta operación."
- Nuevo `exceptions/SesionExceptionHandler` (`@ControllerAdvice` global, con `@Order(Ordered.HIGHEST_PRECEDENCE)`). Maneja `SesionExpiradaException`, `SesionRequeridaException` y `HttpClientErrorException.Unauthorized` que no hayan sido capturadas: invalida la sesión y redirige a `/login?expirada`.

### 1.8 Login y logout
`AutenticacionController` y `AutenticacionService`:
- **Borrar** `destinoDemostracion` y los comentarios de "maqueta".
- `GET /login`: si ya hay sesión, `redirect:` a `sesion.panel()`. Si no, mostrar la vista `login`.
- `POST /login` (`email`, `password`):
  - Si alguno está vacío, volver a mostrar `login` con `error = "Ingresá tu email y contraseña."`.
  - Llamar a `authApi.login(...)`.
    - Si responde 401: `error = "Email o contraseña incorrectos."` y se conserva el `email` en el modelo (nunca la contraseña).
    - Si no hay conexión (`ResourceAccessException`): `error = "No se pudo conectar con el servicio de autenticación."`.
  - Con el token, resolver `nombreVisible` usando `AutenticacionService.nombreVisible(token)`:
    - DONANTE_HUMANO: `GET /donantes/humanos/{ref}` y usar `nombre + " " + apellido`.
    - DONANTE_JURIDICO: `GET /donantes/juridicos/{ref}` y usar `razonSocial`.
    - BENEFICIARIA: `GET /beneficiarios/{ref}` y usar `razonSocial`.
    - ADMIN: usar el email.
    - Estas llamadas necesitan el token recién obtenido: primero guardar la sesión con `nombreVisible = email`, después pedir el perfil y actualizar el nombre. Si el perfil falla, queda el email.
  - Guardar la sesión con `SesionActual.guardar` (cambia el id de sesión) y hacer `redirect:` a `sesion.panel()`.
- `POST /logout`: `sesionActual.cerrar(request)` y `redirect:/`.
- `login.html`:
  - Borrar el bloque de accesos rápidos de demostración (los 3 botones `fillDemoLogin` y su función JS).
  - Borrar `value="demo1234"` y el placeholder `donante@donatrack.org`; poner placeholder `tu@email.com`.
  - Mostrar `${error}` en un `role="alert"`.
  - Si viene `${param.expirada}`, mostrar "Tu sesión venció. Iniciá sesión nuevamente."
  - Conservar el `th:value="${email}"`.
- `AutenticacionMapper`: conservar solo lo que use el registro.

### 1.9 Control de acceso por rol
Nuevo `config/WebConfig implements WebMvcConfigurer` con un `AccesoInterceptor implements HandlerInterceptor`:
- `/admin/**` requiere `esAdmin()`.
- `/donante/**` requiere `esDonante()`.
- `/entidad/**` requiere `esBeneficiaria()`.
- Sin sesión: `response.sendRedirect(contextPath + "/login")` y devolver `false`.
- Con sesión pero rol incorrecto: `response.sendError(403)`. Agregar `templates/error/403.html` mínimo con el mensaje "No tenés acceso a esta sección" y un link a `sesion.panel()`. Reutilizar las clases CSS de `api-error.html`.
- Excluir del control: `/`, `/legal`, `/privacidad`, `/login`, `/logout`, `/registro/**`, `/explorar-donaciones/**`, `/css/**`, `/js/**`, `/vendor/**`, `/favicon.svg`, `/donation-images/**` y `/error`.

### 1.10 Datos de sesión en todas las vistas
Nuevo `controllers/support/SesionModelAdvice` (`@ControllerAdvice` global) con `@ModelAttribute("sesion")`, que agrega la `SesionUsuario` o `null`.
- Para donante y entidad agrega además `@ModelAttribute("notificacionesNoLeidas")`: el resultado de `NotificacionesApiService.contarNoLeidas()` dentro de try/catch, o `null` si falla.
  - Solo se calcula cuando el path empieza con `/donante/` o `/entidad/`, para no llamar en cada página pública.
- **Reemplazar** los `user` hardcodeados ("Administración (sin autenticación)", "Administrador", "Juan Pérez", "Fundación Vida") en:
  - `AdminController.index`
  - `ApiViewSupport.defaults`
  - `IncentivosController.rankings`
  - `DonacionesController.listar`
  - los 3 layouts (`th:text="${user != null ? user.name : '...'}"`): pasar a `${sesion != null ? sesion.nombreVisible : ''}`.
  - En los layouts de donante y entidad, la categoría ("Categoría: Sostenedor") se reemplaza por `${user?.category}` solo si no es nula, y "Entidad Verificada" se borra.
- En los layouts, el badge `<span class="notification-count-badge">2</span>` (y el `3`) pasa a `th:if="${notificacionesNoLeidas != null and notificacionesNoLeidas > 0}" th:text="${notificacionesNoLeidas}"`.
- **Logout en los 3 layouts.** Hay 2 lugares por layout: el header y el footer del drawer. Reemplazar `<a th:href="@{/}">…Salir…</a>` por un formulario:
  ```html
  <form th:action="@{/logout}" method="post" style="display:inline"><button type="submit" class="btn btn-outline btn-sm">…mismo contenido…</button></form>
  ```
  Conservar las clases e íconos existentes.

**Criterios de aceptación de la Fase 1:**
- Sin sesión, `/admin/dashboard` redirige a `/login`.
- Login correcto como admin: redirige a `/admin/dashboard` y las llamadas llevan el Bearer.
- Login correcto como donante: redirige a `/donante/dashboard`.
- Un donante en `/admin/dashboard` recibe 403.
- Logout invalida la sesión.
- `mvn test` en verde: actualizar los tests MVC existentes para que usen una `MockHttpSession` con una `SesionUsuario` de rol ADMIN. Crear un helper `TestSesiones.admin()` / `donante(id)` / `entidad(id)` en `src/test/.../TestSesiones.java`.
- Tests nuevos:
  - `AutenticacionMvcTest`: login OK por cada rol con `@MockBean AutenticacionApiService`, login con 401, acceso denegado y redirect sin sesión.
  - Un test de `WebApiCallerService` con `HttpServer` local que verifique que se envía el header `Authorization`.

---

## Fase 2 — Notificaciones por token

- `NotificacionesApiService`:
  - Firmas nuevas: `listar(boolean soloNoLeidas, int limite)`, `contarNoLeidas()`, `marcarLeida(Long id)` y `marcarTodasLeidas()`.
  - Borrar los parámetros `email`/`telefono` y simplificar `uri(...)` (solo `soloNoLeidas` y `limite`).
- `NotificacionesService`: `historial()` sin argumentos (`limite` 50). Borrar `exigirContacto`.
- `NotificacionesController`:
  - Borrar el flag `historialPorContacto`, los `@RequestParam email/telefono` y el `ResponseStatusException` del flag.
  - `donante()` y `entidad()` cargan siempre `historial()`. Los POST de leída redirigen sin query params.
- `templates/notificaciones.html`:
  - Borrar el formulario de búsqueda por email o teléfono y los textos que lo explican ("Ver notificaciones", "Historial todavía no disponible", etc.).
  - Si la audiencia no es admin, mostrar directamente la lista o el estado vacío "No tenés notificaciones."
  - Los formularios de "marcar leída" ya no mandan email ni teléfono.
- Tests:
  - Reescribir `NotificacionesHistorialMvcTest` sin la propiedad, usando la sesión del donante o de la entidad.
  - Borrar `NotificacionesDisabledTest` si solo prueba el flag eliminado.
  - Ajustar `NotificacionesApiTest` a las firmas nuevas.
- `docs/ENTREGA5-MVC.md`: borrar la fila `NOTIFICACIONES_HISTORIAL_POR_CONTACTO_HABILITADO` y el párrafo del historial por contacto.

---

## Fase 3 — Alinear DTO

En `dto/ApiDtos.java`:
- `Asignacion`: agregar `String descripcionGeneralDonacion`, `Bien bien`, `LocalDateTime fechaAsignacion`, `LocalDateTime fechaEntrega` y `String justificacionFallo`. Conservar los campos actuales y su orden para lo que ya lo use, agregando los nuevos al final.
  - Revisar todos los constructores `new ApiDtos.Asignacion(...)` en los tests y completarlos con `null`.
- `Entrega`: agregar `Long entidadBeneficiariaId`.
- `Beneficiario`: agregar `DonanteRequests.Direccion direccion`, `MedioDeContactoDto medioDeContactoPredeterminado` y `List<DonacionesDetalles.Representante> representantes`. Actualizar los constructores en los tests.

Nuevo record en `LogisticaDetalles`: `public record ActualizarEntrega(String estado, String justificacion, List<String> fotos) {}`.

`LogisticaApiService`: agregar `void actualizarEstadoEntrega(Long id, LogisticaDetalles.ActualizarEntrega body)`, que hace `PATCH url/entregas/{id}/estado`.

---

## Fase 4 — Panel del donante (reemplaza `DonanteDashboardMapper`)

Crear `services/PanelDonanteService` y `mappers/PanelDonanteMapper`.
- El mapper es puro: sin HTTP. Recibe los DTO y devuelve `Map<String,Object>` con las **mismas claves** de hoy.
- El servicio toma `ref = sesionActual.referenciaId()` y `tipo = sesion.tipoDonante()`.
- Cada carga va dentro de `ConsultaOpcional.lista(...)` o en un try/catch. Si falla, se agrega `apiError` al modelo y la sección queda vacía.

### 4.1 Perfil (`user`), en todas las vistas del donante
| Clave | Valor |
|---|---|
| `name` | Humano: `nombre + " " + apellido`. Jurídico: `razonSocial`. |
| `email` | `medioDeContactoPredeterminado.formaContacto` o `null`. |
| `type` | `"Humana"` o `"Jurídica"`. |
| `document` | `numeroDocumento`. |
| `category` | `incentivos.metricas(ref).categoriaActual`. Si falla, `null`. |

### 4.2 `userDonations` (en `/donante/dashboard/donaciones` y en el Resumen)
> **Actualización 2026-10-12 (backend commit 035387d):** `GET /donaciones` pasó a ser solo para admin, y el donante recibe 403.
> - Mientras no exista C1 (ahora bloqueante), `userDonations` y `activeDonorDeliveries` van vacías, con un estado vacío explicativo.
> - Lo que sigue de esta sección aplica cuando C1 exista.
> - Los combos de filtro salen de `GET /catalogo/categorias`.
> - La entidad **no** puede llamar a `GET /beneficiarios` (el listado); solo puede leer su propio perfil.

- Origen: `donaciones.donaciones()` filtrado por `ref.equals(d.donanteId())`.
  `// PROVISORIO: reemplazar por GET /donaciones?donanteId (pedido backend C1)`
- Para cada donación: `donaciones.asignaciones(d.id())` y `beneficiarios()`. Esta última se llama **una sola vez** y se arma un `Map<Long,String>` de id a `razonSocial`.
- Claves:
  | Clave | Valor |
  |---|---|
  | `id` | id de la donación |
  | `title` | `bien.descripcion` o `descripcionGeneral` |
  | `category` | nombre de categoría |
  | `subcategory` | nombre de subcategoría |
  | `item_type` | `bien.tipoBien` |
  | `entity_name` | Nombre de la primera asignación. Si no hay asignación, `null`; la plantilla muestra "Sin asignar". |
  | `entity_id` | id de esa entidad o `null` |
  | `status` | Si hay asignaciones, el estado de la más reciente (`EN_TRASLADO`, `ENTREGADA`, etc.). Si no, `d.estado()`. |
  | `delivery_date` | `fechaEntrega` de esa asignación o `fechaRecepcion.toLocalDate()` |
  | `total_units` | `bien.cantidad` |
  | `unit` | `bien.unidadMedida` en minúsculas |
  | `image_url` | `bien.foto` o `null` |
  | `evidence_photos` | `List.of()`: el donante no puede leer las fotos de la entrega (pedido D1/F1). |
- Plantilla `dashboard-donor-donaciones.html`:
  - Si `image_url` es nulo, mostrar un ícono `<i data-lucide="package">` en lugar del `<img>`.
  - Si no hay evidencia, ocultar la sección.
  - Revisar que los filtros JS por estado, categoría y subcategoría funcionen con los valores reales de los enums. Si hay opciones hardcodeadas con estados que no existen, reemplazarlas por los estados de §1.

### 4.3 `beneficiaryEntities` (`/donante/dashboard/entidades`)
- Origen: `beneficiarios()` (ya extendido en la fase 3) y `necesidades()` (una vez).
- Claves:
  | Clave | Valor |
  |---|---|
  | `id`, `name` | `razonSocial` |
  | `type` | `tipoEntidad` |
  | `category`, `rubro` | `rubro` |
  | `verified` | `activo` |
  | `contactsCount` | `representantes.size()` |
  | `location` | `{address: "calle numero, ciudad, provincia", calle, numero, ciudad, provincia, lat, lng}`. `lat`/`lng` son `null` si no están. |
  | `representatives` | `[{nombre, telefono}]` |
  | `contactChannels` | `[{type: medio.tipoMedioContacto, value: medio.formaContacto, isDefault: true}]` si hay medio predeterminado |
  | `activeNeeds` | Necesidades con `entidadBeneficiariaId == id` y `estado != SATISFECHA`, mapeadas a `{title: descripcion, subcategory, cantidadRestante, unit}` |
  | `recentDonations` | `List.of()` |
- Se **borran** las claves `cuit`, `description`, `rating` e `image`; en la plantilla se ocultan con `th:if`.
- En el mapa Leaflet de esa vista, crear marcadores solo para las entidades con `lat` y `lng` no nulos.

### 4.4 Incentivos (`/donante/dashboard/incentivos`)
Origen: `metricas(ref)`, `progreso(ref)`, `misionesCompletadas(ref)`, `insignias(ref)`, `historialCategorias(ref)` y `categorias()`.
- **`donorMetrics`:**
  - `totalDonacionesHistoricas`, `impactoAcumulado`, `organizacionesAyudadas = totalOrganizacionesAyudadas`, `desgloseMensual`.
  - Borrar `rachaMeses` y `puestoRankingMes`. Ocultar la tarjeta "Ranking Mensual" y la de racha (pedido G1).
- **`incentiveCategories`:** `categorias()` en el orden que devuelve la API. Para cada una:
  - `id`, `nombre`, `nivel = índice + 1`, `misionesTotal = misiones.size()`.
  - `misionesCumplidas`: cantidad de `misionesCompletadas` cuyo `idMision` está entre las misiones de esa categoría.
  - `status`:
    - la categoría cuyo `nombre` es igual a `categoriaActual` lleva `"Actual"`;
    - las anteriores, `"Completada"`;
    - la siguiente, `"Siguiente Nivel"`;
    - el resto, `"Bloqueada"`.
  - `descripcion`: `null`.
- **`missions`:**
  - Primero, la misión actual. Solo si `progreso.mensaje() == null`. Se busca la misión del catálogo con `id == idMision` para obtener `tipo` e `insigniaPremio`. Claves:
    - `id`, `orden`, `categoriaNombre = categoriaActual`, `tipo`, `nombre`, `descripcion`, `valorObjetivo`, `valorActual`;
    - `progresoPorcentaje = round(porcentajeProgreso)`, `completada`;
    - `insigniaPremio = {nombre, descripcion, imagenUrl}`.
  - Después, una entrada por cada misión completada, con `completada=true`, `progresoPorcentaje=100` y `fechaCompletada` formateada.
  - `tipoIcon` / `tipoLabel` / `tipoBadgeClass`: derivarlos de `tipo` con un `switch` en el mapper (racha → `flame`, completitud → `layers`, cantidad de bienes → `package`, entregas exitosas → `check-circle`, por defecto `award`). Mirar los valores reales de `tipo` en `IncentivosDtos.Mision` y en el validator de incentivos del admin.
- **`badges`:** cada insignia se mapea a `{name, description, imageUrl, icon: "award", status: "Obtenida"}`. Si la lista viene vacía, mostrar "Todavía no tenés insignias visibles."
- **`categoryAudits`:** `{fecha: fechaCambio.toLocalDate(), categoriaAnterior, categoriaNueva}`.
- **Visibilidad de insignias.**
  - Nueva ruta `POST /donante/dashboard/incentivos/visibilidad` (`@RequestParam boolean visible`), que llama a `incentivos.visibilidad(ref, visible)`, agrega un mensaje flash y redirige.
  - En la plantilla, el toggle que hoy dispara `showToast(...)` en JS pasa a ser un `<form method="post">` con dos botones (Mostrar / Ocultar). El backend no expone el estado actual, así que no se preselecciona ninguno.
  - **Borrar** `POST /admin/dashboard/incentivos/donantes/{id}/visibilidad` y su formulario en `fragments/consultas-incentivos.html`: el backend responde 403 a ADMIN.

### 4.5 Resumen (`/donante/dashboard`)
- `user`, `userDonations` (las 5 más recientes por fecha) y `badges` (insignias reales).
- Mantener `WebController.donorDashboard`, pero que delegue en `PanelDonanteService.resumen()`.

### 4.6 Entregas (`/donante/dashboard/entregas`)
`activeDonorDeliveries`: por cada donación propia, sus asignaciones en estado `LISTA_PARA_ENTREGAR` o `EN_TRASLADO`.
| Clave | Valor |
|---|---|
| `id` | id de la asignación |
| `donationId` | id de la donación |
| `donationTitle` | `descripcionGeneralDonacion` o `bien.descripcion` |
| `category` | categoría del bien |
| `destinationEntity` | nombre de la entidad |
| `destinationAddress` | dirección de la entidad |
| `destinationCoords` | `{lat, lng}` o `null` |
| `status` | estado de la asignación |
| `truck` | `null` |

- Se borran las claves `eta`, `totalDistanceKm`, `progressPercent` y `stops`.
- En la plantilla:
  - Si `truck` es nulo, mostrar el aviso "La ubicación del camión todavía no está disponible para tu cuenta." y dibujar en el mapa solo el destino, si tiene coordenadas.
  - **No** mostrar camiones de `GET /camiones/ubicaciones`, porque no se puede saber cuáles son los suyos.
  - `// PROVISORIO: pedido backend C3/C4 (GET /entregas/seguimiento)`

### 4.7 Limpieza
- Borrar `DonanteDashboardMapper`, `DonacionesService.datosDemostracionDonante`, `donacionesDonante`, `entidadesDonante`, `IncentivosService.incentivosDonante`, `LogisticaService.entregasDonante` y `WebService.donorDashboard`.
- Las rutas del donante en los controllers llaman a `PanelDonanteService`.
- Tests: crear `PanelDonanteMvcTest` con `@MockBean` de los ApiService. Debe verificar:
  - que se filtran las donaciones de otro donante;
  - que no aparece "Unsplash", "Chofer Asignado" ni "CUIT-";
  - que la visibilidad de insignias llama a la API con el `ref` de la sesión.

---

## Fase 5 — Panel de la entidad (reemplaza `BeneficiarioDashboardMapper`)

Crear `services/PanelEntidadService` y `mappers/PanelEntidadMapper`, con las mismas reglas que la fase 4. `ref = sesionActual.referenciaId()`.

### 5.1 Perfil (`user`)
`beneficiario(ref)` se mapea así:
| Clave | Valor |
|---|---|
| `name` | `razonSocial` |
| `address` | dirección formateada |
| `representative` | nombre del primer representante o `null` |
| `email` | `medioDeContactoPredeterminado.formaContacto` |

Se borra la clave `cuit`.

### 5.2 `needs` (`/entidad/dashboard/necesidades` y el Resumen)
- Origen: `necesidades()` filtrado por `entidadBeneficiariaId == ref`.
  `// PROVISORIO: pedido backend C2`
- Claves:
  | Clave | Valor |
  |---|---|
  | `id` | `necesidadId` |
  | `title`, `description` | `descripcion` |
  | `type` | `tipoNecesidad` |
  | `category`, `subcategory` | del bien |
  | `status` | `estado` |
  | `quantity` | `bien.cantidad` (cantidad objetivo) |
  | `unit` | unidad del bien |
  | `deadline` | `periodo` o `null` |
  | `progress` | `round(cantidadRecibida / bien.cantidad * 100)`, con tope 100; si falta un dato, 0 |
  | `remaining` | `cantidadRestante` |
- Se borran `priority` e `image_url`.
- **Alta de necesidad por la entidad.** Nueva ruta `POST /entidad/dashboard/necesidades`, que reutiliza `DonacionesService.crearNecesidad(ref, descripcionNecesidad, tipoNecesidad, periodo, BienForm)`.
  - El `entidadBeneficiariaId` sale **siempre de la sesión**, nunca del formulario.
  - El formulario de la plantilla tiene que mandar los mismos nombres que usa el admin: incluir el fragmento `fragments/api-bien-form.html` y el selector de categoría y subcategoría. Las categorías se cargan con `donaciones.categorias()`.
  - Mostrar un mensaje flash de éxito o error.
- **Baja de necesidad.** Nueva ruta `POST /entidad/dashboard/necesidades/{id}/eliminar`.
  - Antes de borrar, hace `GET /necesidades/{id}` y verifica que `entidadBeneficiariaId == ref`. Si no coincide, responde 403.
  - `// PROVISORIO: el backend todavía no valida al dueño (pedido B1)`
- Borrar del template los botones o acciones simulados (los `showToast` sin request).

### 5.3 `assignedDonations` (`/entidad/dashboard/donaciones` y el Resumen)
- Origen: `asignacionesEntidad(ref, "")`.
- Claves:
  | Clave | Valor |
  |---|---|
  | `id` | id de la asignación |
  | `donationId` | id de la donación |
  | `title` | `descripcionGeneralDonacion` o `bien.descripcion` |
  | `status` | estado de la asignación |
  | `items_count` | `cantidadAsignada` |
  | `unit` | unidad del bien |
  | `assigned_date` | `fechaAsignacion` |
  | `expected_delivery` | `fechaEntrega` o `null` |
  | `failure_reason` | `justificacionFallo` |
- Se borran `donor_name`, `driver` y `truck_plate`.

### 5.4 `incomingDeliveries` (`/entidad/dashboard/entregas`)
- Las asignaciones propias en `LISTA_PARA_ENTREGAR` o `EN_TRASLADO`.
- Claves: `id`, `donationId`, `title`, `status`, `goods = [{name: bien.descripcion, quantity: cantidadAsignada + " " + unidad}]` y `truck = null`.
- Se borran `donor`, `eta`, `etaMinutes`, `urgency`, `isImminent`, `handlingNotes` e `isPerishable`.
  - Excepción: `isPerishable` se puede calcular como `bien.fechaDeVencimiento != null`.
- El aviso de camión es igual que en 4.6. `// PROVISORIO: pedido backend C3/C4`

### 5.5 Confirmar recepción (`/entidad/dashboard/confirmar`)
- GET: mostrar las asignaciones propias en `EN_TRASLADO`, como `assignedDonations` filtradas.
- Nueva ruta `POST /entidad/dashboard/confirmar` con los campos `entregaId` (Long), `resultado` (`RECIBIDA`|`NO_RECIBIDA`), `fotos` (textarea, una URL por línea) y `justificacion`.
  - Nuevo `validators/RecepcionValidator`:
    - `entregaId` es obligatorio.
    - Si el resultado es `RECIBIDA`, debe haber al menos una URL `http(s)://` válida. Usar `URI.create` y verificar el esquema.
    - Si es `NO_RECIBIDA`, la justificación no puede estar vacía.
  - Nuevo `LogisticaService.confirmarRecepcion(Long entregaId, String resultado, List<String> fotos, String justificacion)`, que llama a `actualizarEstadoEntrega(entregaId, new ActualizarEntrega(resultado.equals("RECIBIDA") ? "ENTREGADA" : "NO_RECIBIDA", justificacion, fotos))`.
  - Las fotos van detrás de una interfaz `services/FotosRecepcion` con un método `List<String> resolver(String textoUrls)`. La implementación inicial, `FotosPorUrl`, parte el texto por líneas. Así después se puede cambiar a subida de archivos sin tocar el controller (pedido F1).
  - Plantilla:
    - Reemplazar la simulación actual (input file con JS y `showToast`) por este formulario real.
    - Label del campo: "Número de entrega".
    - Ayuda visible: "Por ahora el número de entrega lo informa la administración; próximamente se completará automáticamente."
    - `// PROVISORIO: la entidad no puede obtener el entregaId (hallazgo backend X1 → pedido C3)`
    - Mostrar un mensaje flash con el resultado o el error de la API. El backend rechaza entregas que no están `EN_TRASLADO`: mostrar su mensaje tal cual.

### 5.6 Limpieza
- Borrar `BeneficiarioDashboardMapper`, `datosDemostracionBeneficiario`, `necesidadesBeneficiario`, `donacionesBeneficiario`, `LogisticaService.entregasBeneficiario` / `confirmarBeneficiario` y `WebService.beneficiaryDashboard`.
- Borrar `WebDatos` si queda sin uso.
- `dashboard-beneficiary-notificaciones.html` y `dashboard-donor-notificaciones.html` no las usa ningún controller (se usa `notificaciones.html`). Confirmarlo con grep y borrarlas.
- Tests: crear `PanelEntidadMvcTest`. Debe verificar:
  - que el alta de necesidad usa el `ref` de la sesión aunque el formulario mande otro id;
  - que no se puede borrar la necesidad de otra entidad;
  - que la confirmación arma el PATCH correcto;
  - que el validator rechaza confirmaciones sin fotos.

---

## Fase 6 — Páginas públicas sin datos falsos

El backend **no** tiene endpoints públicos (pedido D1). Un visitante anónimo no puede ver datos reales.

- `WebMapper.landing`:
  - `featuredDonations = List.of()` y `recentDeliveries = List.of()`.
  - Borrar las URLs de Unsplash, incluida la `heroImage`. Usar `/donation-images/fundacion-despierta.svg` o quitar la imagen.
  - `heroStats`: borrar `"100% trazabilidad verificada"` y `"REST microservicios conectados"` (no son métricas reales). Dejar la lista vacía y ocultar el bloque.
  - Los textos institucionales (hero, features, footer) se conservan.
- `landing.html`: si las listas están vacías, mostrar el estado vacío "Pronto vas a poder ver acá las donaciones entregadas del último mes." en ambas secciones.
- `WebMapper.map`:
  - Borrar las coordenadas fijas y `beneficiaries_count = 50`.
  - El mapa recibe `donations = List.of()` y los contadores en 0. `map.html` muestra un estado vacío.
- `WebMapper.donationDetail`: si no hay datos, el modelo lleva `donation = null` y la plantilla muestra "Esta donación no está disponible públicamente." Borrar el fallback inventado.
- Crear `services/PublicoService` (o adaptar `WebService`) con los métodos `donacionesDestacadas()`, `entregasEnMapa()` y `detallePublico(id)`.
  - Por ahora devuelven vacío, con el comentario `// PROVISORIO: conectar a GET /publico/entregas (pedido backend D1)`.
  - No llamar a `GET /donaciones` desde páginas públicas: sin token responde 401, y aunque respondiera, mostraría donaciones no entregadas.

---

## Fase 7 — Registro público honesto

El backend no tiene autoregistro (pedido E1).
- Los tres `POST /registro/*` dejan de redirigir a los dashboards: vuelven a mostrar el mismo formulario con los valores cargados.
  - Mensaje `role="status"`: "El registro en línea todavía no está habilitado. Acercate al depósito: una persona administradora registra tus datos y crea tu acceso."
  - No llamar a ninguna API.
- `GET /registro*` queda igual.

---

## Fase 8 — Administración

### 8.1 Crear acceso para donantes y entidades
Es imprescindible: hoy es la **única** forma de que exista un usuario no admin.
- Rutas nuevas:
  - `GET /admin/dashboard/donantes/{tipo}/{id}/acceso`
  - `POST /admin/dashboard/donantes/{tipo}/{id}/acceso`
  - `GET /admin/dashboard/beneficiarios/{id}/acceso`
  - `POST /admin/dashboard/beneficiarios/{id}/acceso`
- El formulario tiene:
  - **email**, precargado con el `medioDeContactoPredeterminado` si es un email;
  - **contraseña inicial**, mínimo 12 caracteres;
  - **confirmación** de la contraseña.
- El rol se deduce: `humanos` → `DONANTE_HUMANO`, `juridicos` → `DONANTE_JURIDICO`, `beneficiarios` → `BENEFICIARIA`. `referenciaId = id`. **Nunca** permitir elegir `ADMIN` desde este formulario.
- **El backend de autenticación no verifica que `referenciaId` exista ni que coincida con el rol.** Por eso:
  - Solo se llega a este formulario desde la fila de un donante o una entidad de los listados. No hay un input libre de id.
  - Antes de mostrar el formulario y antes del POST, el controller vuelve a leer el perfil con `GET /donantes/{tipo}/{id}` o `GET /beneficiarios/{id}`. Si el perfil no existe, responde 404.
- **El email tiene que ser el del perfil.** El historial de notificaciones lo usa como respaldo para identificar al destinatario.
  - Si el medio de contacto predeterminado es un EMAIL, el campo se muestra precargado y **de solo lectura**, y en el POST se toma del perfil, no del formulario.
  - Si el perfil no tiene un email, se bloquea la creación con el mensaje "El perfil no tiene un email de contacto; cargalo antes de crear el acceso."
- `AutenticacionApiService.crearUsuario`:
  - Si responde 409: "Ya existe un usuario con ese email."
  - Si responde 201, mensaje flash: "Acceso creado. Comunicale la contraseña inicial a la persona por un canal seguro."
- Nunca guardar ni mostrar la contraseña después del POST.
- Agregar un botón "Crear acceso" en las filas de `dashboard-admin-donantes.html` y en el listado de beneficiarios de `admin-api.html`.
- Plantilla nueva mínima: `admin-acceso.html`, con el layout admin y las clases de `admin-perfil.html`.
- `// PROVISORIO: reemplazar por flujo de primer acceso con CuentaCreada (pedido backend E2)`

### 8.2 Ranking con nombres
- En `IncentivosController.rankings` y en el historial, resolver `idDonante` a un nombre usando `listarHumanos()` y `listarJuridicos()`, con un mapa armado una sola vez.
- Pasar a la vista filas `{posicion, idDonante, nombre, misiones}`. Si un id no aparece, mostrar "Donante #id".

### 8.3 Limpieza
Borrar en el admin las leyendas "sin autenticación" ("Integración REST real. Este entorno de desarrollo todavía no implementa autenticación ni control de acceso.", "Entorno local sin autenticación ni permisos…"). En los dos casos, reemplazar por nada.

---

## Fase 9 — Documentación y verificación final

- `docs/ENTREGA5-MVC.md`:
  - Agregar `AUTENTICACION_API_URL` a la tabla de variables.
  - Explicar el login real y que el primer admin sale de `AUTH_ADMIN_EMAIL` / `AUTH_ADMIN_PASSWORD` del backend.
  - Actualizar la lista de "Pasos pendientes" con los pedidos de la sección 10 que siguen abiertos.
  - Borrar las frases "No exponer este frontend a Internet: aún no hay autenticación" y "La selección de rol del login original es una demostración".
  - Corregir el enlace roto a `COBERTURA-API.md` (borrarlo).
- `README.md`: corregir el puerto (8090) y sacar la tabla de "Credenciales de demostración".
- `docs/CONTROLLERS-MVC.md`: reflejar `PanelDonanteService`, `PanelEntidadService`, `SesionActual` y el interceptor de acceso.
- Verificación manual con el backend levantado (`docker compose up` en el repo del backend):
  1. Login como admin.
  2. Crear un donante humano y crearle el acceso.
  3. Registrar una donación para ese donante.
  4. Logout y login como el donante.
  5. Ver la donación en "Mis donaciones", las métricas en Incentivos y las notificaciones.
  6. Crear una entidad, crearle el acceso y entrar como entidad.
  7. Crear una necesidad.
  8. Volver como admin: sugerir y asignar.
  9. Volver como entidad: ver la asignación.
- `mvn test` en verde. En el resumen final, listar los tests borrados o reescritos y el motivo.

---

## 10. Pedidos al backend que este plan deja marcados como PROVISORIO

| Código | Pedido | Afecta |
|---|---|---|
| B1 | `@PreAuthorize` y control de dueño en donaciones-service | todo el front (hoy la seguridad depende de ocultar opciones) |
| C1 | `GET /donaciones?donanteId&estado&categoriaId&subcategoriaId` | 4.2 |
| C2 | `GET /necesidades?entidadBeneficiariaId&estado` | 5.2 |
| C3 / X1 | `GET /entregas?asignacionId\|entidadBeneficiariaId\|donanteId` abierto al dueño | 5.5 (entregaId), 4.6, 5.4 |
| C4 | `GET /entregas/seguimiento` con la ubicación del camión del dueño | 4.6, 5.4 |
| D1 | `/publico/**`: entregas del último mes, mapa y detalle | Fase 6 |
| E1 | Registro público orquestado | Fase 7 |
| E2 / X2 / X3 | CuentaCreada + primer acceso (alta por admin y CSV) | 8.1 y requisito 20 |
| F1 | Subida de fotos (GridFS en logística) | 5.5 |
| G1 | Puesto del donante en el ranking | 4.4 |

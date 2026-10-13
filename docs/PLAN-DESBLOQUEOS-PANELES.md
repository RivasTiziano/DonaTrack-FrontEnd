# Plan — Desbloqueos de los paneles de donante y entidad

Estado: **aprobado (2026-10-13)**, se aplica con Haiku sin commit.
Base del front: rama `entrega5-desmaquetado-front` @ a315c7d.
Contratos del back verificados en ENTREGA-5 @ f594f86.

Fuera de alcance: **Req 9 y 14** (entregas activas en el mapa), hasta que exista C4. Los PROVISORIO de C4, D1, E2 y F1 no se tocan.

Enunciado de referencia: `../2026-tpa-ma-ma-grupo-18/docs/local/enunciado-tp-anual-2026.md`.

---

## 0. Reglas
- El dueño se toma **siempre** del `referenciaId` de la sesión. Nunca de la URL ni de un formulario.
- Arquitectura: Controller → Service → ApiService. Mappers puros y validators.
- Sin N+1: como máximo una llamada por recurso y por pantalla.
- No tocar los archivos del refactor del admin (eb16201) ni los del registro (a315c7d), salvo los que nombra este plan.
- Al terminar cada ítem, `mvn -q test` en verde. Sin commit ni push. No levantar Docker.

## 1. Contratos que se usan (verificados en el código del back)

| Endpoint | Permiso para quien no es admin | Respuesta |
|---|---|---|
| `GET /donaciones?donanteId&estado&categoriaId&subcategoriaId` | `VER_DONACIONES_PROPIAS`, con `donanteId` obligatorio e igual al propio. Si falta o es ajeno: 403. | `[{id, donanteId, descripcionGeneral, bien, estado, fechaRecepcion}]` |
| `GET /asignaciones?entidadBeneficiariaId&donanteId&estado` | Entidad: `VER_DONACIONES_ASIGNADAS` con el id propio. Donante: `VER_DONACIONES_PROPIAS` con el id propio. | `[AsignacionDetalle]`, que ya está mapeado en `ApiDtos.Asignacion` |
| `GET /necesidades?entidadBeneficiariaId&estado` | `isAuthenticated()` | `[Necesidad]` |
| `GET /entregas?asignacionId&entidadBeneficiariaId&donanteId&estado` | Entidad: `CONFIRMAR_RECEPCION` con el id propio. Donante: `VER_DONACIONES_PROPIAS` con el id propio. | `[{id, asignacionId, entidadBeneficiariaId, donanteId, direccion, bienes, estado, justificacion, fotos, camionId, camionPatente}]` |
| `PATCH /entregas/{id}/estado` | `CONFIRMAR_RECEPCION` (la entidad dueña) | Ya lo usa `LogisticaApiService.actualizarEstadoEntrega` |

Valores de los enums:
- `EstadoDonacion`: `EN_DEPOSITO`, `ASIGNACION_REALIZADA`, `VENCIDA`.
- `EstadoEntrega`: `PENDIENTE`, `PLANIFICADA`, `EN_TRASLADO`, `ENTREGADA`, `NO_RECIBIDA`.
- `EstadoNecesidad`: `PENDIENTE`, `EN_PROGRESO`, `SATISFECHA`.

---

## Ítem 1 — Req 12: confirmar recepción sin el campo manual
**Respaldo:**
- UI/UX #12: "confirmar la recepción de una donación, cargando fotos de la misma".
- Entrega 3, "Servicio de Logística - Trazabilidad de las entregas": confirmación → "Entregada" con fotos; "No recibida" queda para revisión del admin.

**Interpretación:** las fotos siguen como URLs, sin subir archivos (F1 pendiente). El enunciado dice "cargando fotos" y no exige subirlas.

- **`LogisticaApiService`:** agregar `List<ApiDtos.Entrega> entregas(Long entidadBeneficiariaId, Long donanteId, Long asignacionId, String estado)`.
  - Arma la query con `UriComponentsBuilder` y omite los parámetros nulos.
  - Se conserva `entregas()` sin filtros, que usa el admin.
- **`PanelEntidadService.confirmar()`:**
  1. Carga las asignaciones propias en `EN_TRASLADO`: `GET /asignaciones?entidadBeneficiariaId={ref}&estado=EN_TRASLADO` (`asignacionesEntidad`, que ya existe).
  2. **Una sola** llamada `GET /entregas?entidadBeneficiariaId={ref}&estado=EN_TRASLADO`, que se convierte en un `Map<asignacionId, entregaId>`.
  3. Cada asignación lleva su `entregaId`. Si no se encuentra, la fila se muestra con el aviso "La entrega todavía no está disponible para confirmar" y sin botones.
- **Controller (`POST /entidad/dashboard/confirmar`):**
  - Recibe `asignacionId` (no `entregaId`), `resultado` (RECIBIDA | NO_RECIBIDA), `fotos` y `justificacion`.
  - El service resuelve el `entregaId` del lado del servidor, con la misma consulta filtrada por `asignacionId` y `entidadBeneficiariaId={ref}`.
  - **Nunca** se acepta un `entregaId` que venga del cliente. Si no hay una entrega `EN_TRASLADO` propia para esa asignación (ya se confirmó, cambió de estado o es ajena), **no hace el PATCH** y muestra "Esta entrega ya no está pendiente de confirmación".
- **`RecepcionValidator`:** se saca la validación de `entregaId` y se agrega la de `asignacionId` obligatorio. Las reglas de fotos y justificación no cambian.
- **Contrato del PATCH, confirmado por el back @ f594f86:**
  - El botón dice "Recibida", pero lo que se envía es `estado: ENTREGADA`. Lo traduce `LogisticaService.confirmarRecepcion`, que ya lo hace.
  - `ENTREGADA` exige al menos una URL de foto no vacía. `NO_RECIBIDA` exige justificación. El cliente valida las dos cosas antes de llamar.
  - Respuestas posibles:
    - 400 "Transición de estado inválida" (la entrega no está `EN_TRASLADO`): se muestra el mensaje "Esta entrega ya no está en traslado; actualizá la página".
    - 403 (entrega ajena): se muestra "No podés confirmar esta entrega".
    - Cualquier otro 400: se muestra el `message` del back.
- **Plantilla `dashboard-beneficiary-confirmar.html`:**
  - Se borra el campo "Número de entrega" y su ayuda.
  - Cada asignación muestra dos acciones: **Recibida** (un `form-panel` con fotos, una URL por línea) y **No recibida** (un `form-panel` con la justificación).
  - Cada `form` lleva un `asignacionId` oculto.
- Borrar los PROVISORIO de X1 en `LogisticaController`:180, `LogisticaService`:86 y la plantilla:68.
- **Tests (`PanelEntidadMvcTest`):**
  - el GET hace una sola llamada a `/entregas` con `entidadBeneficiariaId=ref` y `estado=EN_TRASLADO`;
  - el POST resuelve el `entregaId` del servidor y hace PATCH con ENTREGADA y las fotos, o con NO_RECIBIDA y la justificación;
  - una asignación ajena o sin entrega en curso no hace PATCH y muestra "Esta entrega ya no está pendiente de confirmación";
  - un `entregaId` que llegue en el form se ignora.

## Ítem 2 — Req 5: "Mis donaciones" del donante
**Respaldo:** UI/UX #5: "filtrar sus donaciones realizadas por estado y categoría o subcategoría/s de bienes entregados".

**Interpretación:**
- La columna "entidad destino" de cada donación no está pedida de forma literal en el #5. Se apoya en la trazabilidad (Contexto, "Nuestro sistema"; Entrega 2, "Estados de las donaciones").
- El filtro por estado usa `EstadoDonacion` (estado de cobertura). El estado de la entrega se muestra aparte, como un chip de la asignación.

- **`DonacionesApiService`:**
  - Agregar `List<ApiDtos.Donacion> donaciones(Long donanteId, String estado, Long categoriaId, Long subcategoriaId)`, que omite los parámetros nulos o vacíos.
  - Agregar `List<ApiDtos.Asignacion> asignacionesDonante(Long donanteId)`, que llama a `GET /asignaciones?donanteId=`.
  - Se conserva `donaciones()` para el admin.
- **`PanelDonanteService.donaciones(filtro)`:**
  - Recibe un record `FiltroDonaciones(String estado, Long categoriaId, Long subcategoriaId)` armado por el controller desde los query params, con estado validado contra el enum y ids numéricos. Si un valor es inválido, se ignora con un aviso.
  - Hace tres llamadas en total:
    1. `GET /donaciones?donanteId={ref}&…filtros`;
    2. `GET /asignaciones?donanteId={ref}`, una vez, agrupada por `donacionId`;
    3. `GET /beneficiarios`, una vez, para tener el mapa id → razón social (permitido al donante con `VER_ENTIDADES_BENEFICIARIAS`).
  - Se borra `donacionesPropias()` y el PROVISORIO C1 de la línea 154.
  - Cada fila conserva las claves actuales del mapper (`PanelDonanteMapper`): `entity_name` sale de la asignación más reciente y `status` es el estado de la asignación si existe; si no, el de la donación.
  - `categoriasFiltro` y `subcategoriasFiltro` salen de `GET /catalogo/categorias`, como hoy.
- **Controller:** `GET /donante/dashboard/donaciones?estado=&categoriaId=&subcategoriaId=`.
- **Plantilla `dashboard-donor-donaciones.html`:**
  - Los filtros pasan a ser un `<form method="get">` con selects de estado, categoría y subcategoría, más los botones "Filtrar" y "Limpiar". Se conservan los valores elegidos.
  - El filtro JS que exista se reemplaza por el form GET, o se deja solo como mejora progresiva.
  - Se borra el estado vacío "Todavía no podemos mostrar tu historial…" y el PROVISORIO de la línea 134.
  - Nuevo estado vacío: "No tenés donaciones con esos filtros."
- **Resumen `dashboard-donor.html`:** las 5 donaciones más recientes, con `GET /donaciones?donanteId={ref}` (una llamada). Se borra el PROVISORIO de la línea 77.
- **No se toca** `dashboard-donor-entregas.html` ni `PanelDonanteService.entregas()`: es el Req 9, bloqueado por C4.
- **Tests (`PanelDonanteMvcTest`):**
  - se llama a `/donaciones` con `donanteId=ref` y los filtros del request;
  - se ignora un `donanteId` que venga en la URL;
  - hay exactamente una llamada a `/asignaciones?donanteId` y una a `/beneficiarios`;
  - un estado inválido se ignora;
  - el resumen se limita a 5 donaciones.

## Ítem 3 — Req 10: necesidades filtradas por el back
**Respaldo:** UI/UX #10 y Entrega 1, "Entidades beneficiarias y necesidades".

- **`DonacionesApiService`:** agregar `List<ApiDtos.Necesidad> necesidades(Long entidadBeneficiariaId, String estado)`.
- **`PanelEntidadService`:**
  - `necesidades(m, ref)` pasa a usar `GET /necesidades?entidadBeneficiariaId={ref}`.
  - Se borra el filtro en memoria y el PROVISORIO C2 de la línea 121.
- **Desvío respecto del pedido (`PanelDonanteService`:77, directorio de entidades del donante):**
  - Esa pantalla muestra las necesidades activas de **todas** las entidades. Filtrar por entidad sería una llamada por entidad (N+1).
  - Se mantiene **una sola** llamada `GET /necesidades`, agrupada por entidad en memoria.
  - Solo se cambia el comentario: deja de ser PROVISORIO y queda la justificación "una llamada para todas las entidades; evita N+1".
- **Tests:**
  - la entidad llama a `/necesidades` con `entidadBeneficiariaId=ref`;
  - el directorio del donante hace una sola llamada a `/necesidades`.

## Ítem 4 — Limpieza y DTO
**Respaldo:** mantenimiento. Entrega 5, alcance "Servicio de Autenticación" (B1 ya resuelto).

- `PanelEntidadService`:74: borrar el comentario "PROVISORIO … pedido B1". El chequeo de dueño del front **se mantiene** como defensa adicional; el comentario pasa a explicar eso.
- PROVISORIO de C1, C2 y X1: se borran en los ítems 1 a 3.
- **No se tocan** los PROVISORIO de C3/C4 (`dashboard-beneficiary-entregas.html`, `dashboard-donor-entregas.html`), D1 (`WebMapper`, `dashboard-donor-entidades.html`) ni E2 (`AccesoService`).
- `ApiDtos.Entrega`: agregar al final `Long donanteId`, `LogisticaDetalles.Direccion direccion` y `List<BienEntrega> bienes`.
  - `BienEntrega` es un record nuevo y mínimo con `descripcion`, `cantidad` y `unidadMedida`, con `@JsonIgnoreProperties`.
  - Completar con `null` los constructores que se usen en los tests.
- Verificación: `grep -rn PROVISORIO src/main` solo debe mostrar los de C3/C4, D1, E2 y F1.

---

## Orden
1. Ítem 4 (DTO): lo necesita el ítem 1.
2. Ítem 1 (Req 12).
3. Ítem 2 (Req 5).
4. Ítem 3 (Req 10).
5. Ítem 4 (resto de la limpieza).

## Verificación manual
La hace Tomás, con los contenedores que levanta el coordinador y los datos de `scripts/prueba-visual` recargados.
- Como entidad:
  - confirmar Recibida sin escribir el número de entrega;
  - confirmar No recibida con una justificación.
- Como donante:
  - ver sus donaciones;
  - filtrar por estado, categoría y subcategoría;
  - comprobar que aparece la entidad destino de las donaciones ya asignadas.
- Como entidad: ver solo sus necesidades.

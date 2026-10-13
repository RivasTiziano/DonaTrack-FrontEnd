# Plan — Seguimiento de entregas y páginas públicas

Estado: **aprobado (2026-10-13)**; se aplica con Haiku sin commit. El refresco cada 60 s está respaldado por la Entrega 3 ("seguimiento en tiempo real de las entregas por todos los interesados").
- Front: `entrega5-desmaquetado-front` @ bc986e1.
- Back: ENTREGA-5 @ 725a6b8. Los contratos están verificados en el código.

Enunciado: `../2026-tpa-ma-ma-grupo-18/docs/local/enunciado-tp-anual-2026.md`.

## 0. Reglas
- Arquitectura: Controller → Service → ApiService. El dueño sale siempre del token: las rutas del back no reciben id.
- Sin N+1. Las páginas públicas usan un cliente **sin Bearer**, siguiendo el patrón de `PublicoService`.
- No tocar los PROVISORIO de E2 ni de F1.
- `mvn -q test` en verde al terminar cada ítem. Sin commit ni push. No levantar Docker.

## 1. Contratos (verificados en el back @ 725a6b8)

**Seguimiento (logística)**
- `GET /entregas/seguimiento`
  - Permiso: `SEGUIR_ENTREGAS`. No recibe parámetros: el dueño sale del token.
  - Respuesta: `[{entregaId, asignacionId, estado, destino{ciudad, provincia, latitud, longitud}, camion{patente, latitud, longitud, ultimaActualizacion} | null}]`.

**Páginas públicas (donaciones, sin token)**
- `GET /publico/entregas?desde=yyyy-MM-dd` (destacadas)
  - Respuesta: `[{id, descripcion, fechaEntrega, cantidad, unidad, imagen, categoria, subcategoria, entidad{nombre, localidad}, fotos[]}]`.
- `GET /publico/entregas/{id}`: devuelve el mismo DTO.
- `GET /publico/entregas/mapa?desde=` (todas las ENTREGADAS cuya entidad tiene coordenadas)
  - Respuesta: `[{id, fechaEntrega, categoria, subcategoria, cantidad, unidad, entidad{nombre, localidad, provincia, latitud, longitud}}]`.
- `GET /publico/entidades`
  - Respuesta: `[{id, nombre, tipo, localidad, provincia, latitud, longitud}]`. No se usa en este plan; queda disponible.

**Publicación (donaciones, solo admin)**
- `PATCH /asignaciones/{id}/publicacion`
  - Body: `{publicable: boolean (obligatorio), descripcionPublica?: string(≤500)}`.
  - Respuesta 200: `{asignacionId, publicable, descripcionPublica, fotos[]}`.
  - Se aplica sobre la **asignación** (no sobre la entrega) y solo si está ENTREGADA.

---

## Ítem 1 — Req 9 y 14: mapa de entregas activas con el camión
**Respaldo:** UI/UX #9 (donante) y #14 (entidad): "seguir en un mapa las entregas activas … visualizando el/los camiones asociados con ubicación y última actualización". También Entrega 3, "Monitoreo de camiones en tiempo real".

- `LogisticaApiService.seguimiento()` → `GET /entregas/seguimiento`, con el record nuevo `LogisticaDetalles.SeguimientoEntrega(entregaId, asignacionId, estado, Destino destino, CamionPosicion camion)`.
- `PanelDonanteService.entregas()` y `PanelEntidadService.entregas()`:
  - **una** llamada a `seguimiento()`;
  - más un enriquecimiento con el título de la donación o del bien:
    - donante: con `GET /asignaciones?donanteId=ref` (una llamada);
    - entidad: con `GET /asignaciones?entidadBeneficiariaId=ref` (una llamada).
  - Se borra la versión vacía actual y el comentario de C4.
- Las claves del modelo siguen siendo las mismas: `activeDonorDeliveries` / `incomingDeliveries`.
  - `truck` pasa de `null` a `{plate, lat, lng, lastUpdate}`.
  - `destinationCoords` sale de `destino`.
- **Plantillas** (`dashboard-donor-entregas.html`, `dashboard-beneficiary-entregas.html`):
  - mapa Leaflet con un marcador de destino y otro de camión por cada entrega;
  - lista lateral con el estado, la patente y "última actualización hace X min";
  - si `camion == null`, mostrar "Sin posición todavía" (solo el destino);
  - si no hay entregas activas, mostrar un estado vacío.
- **INTERPRETACIÓN:** no hay refresco en tiempo real. Se agrega un botón "Actualizar" y un `meta refresh` opcional cada 60 s (EXTRA). El enunciado dice "en tiempo real", pero no define el mecanismo.
- Borrar los PROVISORIO de C3/C4 (`dashboard-beneficiary-entregas.html`:73, `dashboard-donor-entregas.html`:72, incluida la mención obsoleta a C1) y el comentario de C4 en `PanelDonanteService.entregas()`.
- **Tests:**
  - una sola llamada a `/entregas/seguimiento`;
  - con camión, renderiza la patente y la última actualización;
  - con `camion == null`, renderiza "Sin posición todavía";
  - la lista vacía muestra el estado vacío;
  - el panel no llama a `/camiones/*ubicacion*` (B2).

## Ítem 2 — Req 1 y 2: landing y mapa público
**Respaldo:**
- UI/UX #1: landing con "ejemplos destacados de donaciones realizadas en el último mes" y "visualización de fotos de donaciones entregadas sin login".
- UI/UX #2: "mostrar las donaciones ya entregadas … en un mapa interactivo y permitir ver sus detalles al hacer clic en cada marcador".

- **`PublicoService`** (cliente sin Bearer, que ya existe):
  - `donacionesDestacadas()` → `GET /publico/entregas?desde={hoy−30 días}`;
  - `entregasEnMapa()` → `GET /publico/entregas/mapa`. Se carga todo el histórico de entregadas, porque el #2 dice "ya entregadas" sin límite de fecha;
  - `detalleEntrega(id)`, que ya existe.
  - Actualizar `EntregaPublicaDto` con `categoria`, `subcategoria`, `entidad{nombre, localidad}` y `fotos[]`.
  - Nuevo `EntregaMapaDto`.
- **Landing (`WebMapper.landing` / `landing.html`):**
  - "Donaciones destacadas del último mes": tarjetas con la foto (`fotos[0]`; si falta, `imagen`), la descripción, la categoría, la entidad y la localidad, y la fecha.
  - **Galería:** se mantiene como la dejó eb16201. Hoy toma sus fotos de `/publico/entregas`, así que con esto se completa sola. No se agregan imágenes de stock.
- **Mapa (`WebMapper.map` / `map.html`):**
  - marcadores con las coordenadas de `entidad`;
  - popup con la categoría y subcategoría, la cantidad, la entidad, la fecha y el enlace "Ver detalle" → `/explorar-donaciones/{id}`;
  - contadores reales (entregas, entidades distintas y unidades).
  - Se borra el `List.of()` actual de `entregasEnMapa`.
- **Detalle (`donation-detail.html`):** la galería con `fotos[]`, además de los datos del DTO.
- Borrar los PROVISORIO de D1 (`WebMapper`:130 y `dashboard-donor-entidades.html`:252).
  - **Excepción:** el de la línea 252 trata de las constancias fotográficas para el **donante** dentro de su panel, que no forman parte de D1-público. Si sigue sin un endpoint que lo cubra, se mantiene y se reetiqueta como F1.
- **Tests:**
  - la landing llama a `/publico/entregas` con `desde` = hoy−30, sin el header `Authorization`;
  - el mapa usa `/publico/entregas/mapa` y renderiza los marcadores;
  - el detalle muestra las fotos;
  - con la API caída, se ven los estados vacíos y no hay error.

## Ítem 3 — Admin: publicar o despublicar entregas destacadas
**Respaldo:** UI/UX #1 ("ejemplos destacados"). **INTERPRETACIÓN:** el enunciado no dice quién elige los destacados. El back resolvió que lo elija el admin para revisar las fotos antes de publicarlas.

- `DonacionesApiService.publicacion(asignacionId, publicable, descripcionPublica)` → `PATCH /asignaciones/{id}/publicacion`.
- **Dónde:** en la pantalla admin **Entregas** (`/admin/dashboard/entregas`, fragmento `admin-logistica`). Para las filas con `estado == ENTREGADA`, una acción "Publicar en la landing" abre un diálogo con:
  - la descripción pública opcional (≤500);
  - el aviso "Revisá que las fotos no muestren datos personales";
  - y la acción "Despublicar".
  - Las dos usan el `asignacionId` de la fila.
- **Brecha (pedido al back X13):** ninguna respuesta de lectura expone hoy si una asignación ya está publicada. El estado se ve solo en la respuesta del PATCH, que se muestra en el mensaje flash con las fotos que quedarían visibles. La fila no puede marcar "Publicada" hasta que `AsignacionDetalle` (o la entrega) incluya `publicable`.
- Ruta MVC: `POST /admin/dashboard/asignaciones/{id}/publicacion`, que redirige a Entregas con un flash.
- **Tests:**
  - el PATCH lleva el body exacto;
  - la acción solo aparece en las filas ENTREGADA;
  - una descripción de más de 500 caracteres se rechaza en el cliente.

---

## Orden
1 → 2 → 3. Cada ítem trae sus tests.

## Verificación manual (Tomás)
- Donante y entidad con una ruta iniciada ven el camión y su "última actualización".
- El admin publica una entrega ENTREGADA → aparece en la landing.
- El mapa público muestra los marcadores con detalle.

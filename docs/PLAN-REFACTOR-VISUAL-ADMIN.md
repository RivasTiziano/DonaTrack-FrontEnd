# Plan — Refactor visual del panel de Administración (y arreglos de la landing)

Estado: **plan, sin aplicar**. Se ejecuta después del desmaquetado (rama `entrega5-desmaquetado-front`).

Objetivo: que el panel de admin sea legible y coherente **sin perder ninguna funcionalidad**, y cumpla las
"Consideraciones para el maquetado" de la Entrega 4:
- navegación de 3 niveles como máximo;
- feedback no intrusivo;
- responsive;
- WCAG AA;
- hit area de 48 px;
- tokens de diseño unificados.

Los dashboards de donante y entidad se revisan **después**, en un plan aparte. También tienen formularios y secciones rotas.

---

## 0. Reglas para quien implemente

1. **No se pierde ninguna operación.** Antes de empezar, generá el inventario de rutas con:
   ```
   grep -rhoE '@(Get|Post)Mapping\([^)]*\)' src/main/java | sort
   ```
   Al terminar, toda ruta GET y POST de `/admin/**` tiene que seguir accesible desde la UI: por la barra lateral, por un botón o por un enlace de fila. Se verifica con el checklist de la §8.
2. **No cambiar contratos.** Los nombres de los campos de formulario, las URLs, los métodos HTTP y las claves de modelo se mantienen. Solo cambian la presentación y la ubicación.
3. **No se inventan datos.** Los KPI y las listas salen de las APIs. Si una consulta falla, la tarjeta muestra "—" y un aviso, y la página no se rompe.
4. **Estilos.** Los estilos nuevos van en un solo archivo, `static/css/styles/pages/admin.css`, importado desde `index.css`. Se usan los tokens de `base.css` (`--color-*`, `--radius-*`, espaciados). No usar estilos inline nuevos; ir migrando los existentes a clases.
5. **Verificación.**
   - `mvn -q test` en verde después de cada fase.
   - Revisión visual en el navegador, con el contenedor levantado, en 375, 768 y 1280 px.
6. Sin commits salvo que se pidan. **Nunca** push. Los commits no llevan `Co-Authored-By`.

---

## 1. Problemas detectados (para que no se repitan)

| Problema | Causa | Arreglo |
|---|---|---|
| Fila de 20 enlaces arriba de **todas** las pantallas de admin | `<nav class="api-links">` en `admin-api.html` y `admin-complementos.html` | Borrarla. La navegación vive solo en la barra lateral (§3). |
| Panel General sin contenido | La sección `inicio` es una tarjeta con texto | Tablero operativo (§4). |
| Botón "Crear" gigante en Flota de Camiones (y en todos los formularios de alta) | El `<button>` es un hijo más de la grilla `.api-fields` y se estira para ocupar la celda | Separar los campos y las acciones: `.form-grid` para los campos y `.form-actions` (flex, alineado a la derecha) para los botones (§2). |
| Listado de Donaciones ilegible | La celda "Acciones" contiene formularios completos (select de estado, justificación y botones) en cada fila | Tabla compacta con acciones mínimas. Los formularios de edición se mueven al detalle o a un diálogo (§2, §5.2). |
| Títulos y migas duplicados, tablas sin chips de estado, enums crudos (`EN_DEPOSITO`) | Plantilla genérica `admin-api` | Encabezado de página estándar, chips de estado y etiquetas legibles (§2). |

---

## 2. Componentes base (Fase A)

Todos van en `admin.css`. Donde convenga, se arman como fragmentos Thymeleaf en `templates/fragments/admin-ui.html`.

| Componente | Uso |
|---|---|
| `page-header` | Migas + título + descripción corta + acción principal a la derecha (por ejemplo "+ Nuevo camión"). Reemplaza el `h1` y las migas sueltas. |
| `toolbar` | Búsqueda o filtros a la izquierda y acciones secundarias a la derecha, encima de las tablas. |
| `data-table` | Tabla compacta: cabecera sticky, filas de 44–48 px, números alineados a la derecha, `white-space: nowrap` en ids y fechas. En móvil se ve como tarjetas apiladas (con `data-label` en cada `td`). |
| `row-actions` | Como máximo 2 acciones visibles por fila (íconos de 44 px con `aria-label` y tooltip), más un menú "⋯" (`<details>`) con el resto. Las acciones destructivas tienen confirmación. **Nunca** formularios completos dentro de la celda. |
| `status-chip` | Muestra los enums con texto legible y color: `EN_DEPOSITO` → "En depósito" en gris, `ASIGNACION_REALIZADA` → "Asignada" en azul, `VENCIDA` → "Vencida" en rojo, `DISPONIBLE`, `EN_RUTA`, `EN_TRASLADO`, `NO_RECIBIDA`, etc. El mapeo de enum a etiqueta y color va en un fragmento único. |
| `form-panel` | Los formularios de alta van en un `<details class="form-panel">` colapsable (cerrado por defecto) con el título "Nuevo …", o en un `dt-modal` existente. Adentro usan `.form-grid` (campos) y `.form-actions` (botones de tamaño normal y alineados). |
| `kpi-card` | Valor grande + etiqueta + variación o detalle + enlace. Se reutiliza el `.stat-card` de `admin-dashboard.css` en lugar de crear otro. |
| `empty-state` | Ícono + texto + acción ("Todavía no hay camiones. + Nuevo camión"). |
| Mensajes | `apiError` y `success` se muestran con el `showToast` existente (vía un script que lee atributos `data-*`). Además se deja el banner `role="alert"` para lectores de pantalla. |

---

## 3. Barra lateral agrupada (Fase A)

Se reemplazan los tres bloques duplicados del layout admin (sidebar, drawer móvil y barra inferior) por **un único fragmento** `fragments/admin-nav.html`, incluido en los tres lugares.

```
Resumen                      /admin/dashboard
DEPÓSITO
  Donantes                   /admin/dashboard/donantes
  Donaciones                 /admin/dashboard/donaciones
  Importar CSV               /admin/dashboard/importar
ENTIDADES
  Beneficiarios              /admin/dashboard/beneficiarios
  Necesidades                /admin/dashboard/necesidades
ASIGNACIÓN
  Matchmaking                /admin/dashboard/asignar        (pestañas: Sugerir · Sugerencias guardadas · Por entidad)
LOGÍSTICA
  Camiones                   /admin/dashboard/camiones
  Rutas                      /admin/dashboard/rutas          (paradas dentro del detalle de la ruta)
  Entregas                   /admin/dashboard/entregas
  Seguimiento GPS            /admin/dashboard/seguimiento
INCENTIVOS
  Rankings                   /admin/dashboard/rankings       (pestañas: Mes actual · Historial)
  Categorías y misiones      /admin/dashboard/incentivos
  Consulta por donante       /admin/dashboard/incentivos/donantes
CATÁLOGO
  Categorías                 /admin/dashboard/catalogo
  Bienes                     /admin/dashboard/bienes
SISTEMA
  Notificaciones             /admin/dashboard/notificaciones
──────
Mapa público                 /explorar-donaciones
```

- `activeTab` sigue marcando el ítem activo. Revisar que todas las rutas, incluidas las de `admin-complementos`, pasen un `activeTab` correcto. Hoy `admin-complementos` siempre marca "inicio".
- Barra inferior en móvil: Resumen · Donaciones · Matchmaking · Logística (va a Camiones) · Menú (abre el drawer).
- Los ítems se muestran según los permisos del token (`sesion.tiene('GESTIONAR_CAMIONES')`, etc.), con los mismos nombres de permiso que el backend.
- Las pestañas (Matchmaking, Rankings) son enlaces a las rutas existentes con estilo `tabs`, no JS.

---

## 4. Panel General (Fase B)

### 4.1 Datos
Nuevo `services/PanelAdminService` (y `mappers/PanelAdminMapper`). Cada carga es independiente: si una falla, solo esa tarjeta queda en "—".

| Bloque | Fuente (endpoints ya usados por el front) | Cálculo |
|---|---|---|
| KPI Donaciones en depósito | `GET /donaciones` | cantidad con `estado == EN_DEPOSITO` |
| KPI Por vencer | ídem | `EN_DEPOSITO` y `bien.fechaDeVencimiento` dentro de los próximos 7 días, más las ya vencidas sin marcar |
| KPI Necesidades activas | `GET /necesidades` | `estado != SATISFECHA`; detalle: cuántas son `EXTRAORDINARIA` |
| KPI Flota | `GET /camiones` | cantidad por estado (`DISPONIBLE`, `EN_RUTA`, `FUERA_DE_SERVICIO`) |
| KPI Entregas en traslado | `GET /entregas` | `estado == EN_TRASLADO` |
| KPI No recibidas | `GET /entregas` | `estado == NO_RECIBIDA` (en rojo si es > 0) |
| Requiere atención: no recibidas | `GET /entregas` | Las 5 más recientes `NO_RECIBIDA`, con su justificación y un enlace al detalle de la entrega |
| Requiere atención: por vencer | `GET /donaciones` | Las 5 más próximas a vencer, con el formulario mínimo "Marcar vencida" (POST existente `/admin/dashboard/donaciones/{id}/estado`, estado `VENCIDA` y justificación obligatoria en un diálogo) |
| Requiere atención: sin sugerencia | `GET /donaciones` + `GET /sugerencias` | Las `EN_DEPOSITO` cuyo id no está en las sugerencias guardadas, con el botón "Generar sugerencia" (POST existente `/admin/dashboard/asignar/sugerir`) |
| Rutas de hoy | `GET /rutas` | `fecha == hoy`: chofer, camión, estado (chip) y enlace al detalle |
| Mapa de camiones | `GET /camiones/ubicaciones` | Leaflet: un marcador por camión, con un popup con la patente, la última actualización y el avance de la ruta. Si no hay ubicaciones, se muestra un estado vacío. |
| Podio mensual | `GET /incentivos/rankingMensual` + nombres (`IncentivosService.rankingConNombres`) | Top 3 con el nombre del donante |
| Accesos rápidos | — | Botones a: Nuevo donante humano, Nuevo donante jurídico, Registrar donación (abre el `form-panel` de Donaciones), Ejecutar matchmaking (POST `/admin/dashboard/evaluador/ejecutar` con confirmación), Planificar rutas (Rutas), Importar CSV |

### 4.2 Vista
- Nueva plantilla `admin-panel.html`, que deja de usar la sección `inicio` de `admin-api`.
- `AdminController.index` delega en `PanelAdminService`.
- Disposición:
  - **Escritorio:** fila de 6 KPI, después "Requiere atención" (2/3 del ancho) + "Rutas de hoy" (1/3), y abajo el mapa (2/3) + el podio (1/3).
  - **Tablet:** 2 columnas.
  - **Móvil:** 1 columna, con las listas antes que el mapa.
- Saludo: "Hola, {nombreVisible}" + la fecha de hoy. Accesos rápidos en el `page-header`.

### 4.3 Tests
- `PanelAdminMvcTest`, con `@MockBean` de los ApiService. Tiene que verificar:
  - los conteos;
  - que una API caída deja "—" y no rompe la página;
  - que "por vencer" no incluye donaciones fuera de los 7 días;
  - que no aparece la fila `api-links`.

---

## 5. Pantallas (Fases C–F)

Cada pantalla se arma con: `page-header` con su acción principal, `toolbar`, `data-table` con `row-actions`, `form-panel` y `empty-state`.

### 5.1 Depósito
- **Donantes** (`dashboard-admin-donantes.html`): mantener los filtros por tipo. Acciones de fila: Ver/editar · Crear acceso · menú "⋯" con Eliminar. Arriba, los botones "Nuevo humano" y "Nuevo jurídico".
- **Donaciones:**
  - Tabla compacta con ID, Donante (nombre si se puede resolver; si no, "#id"), Descripción, Bien, Disponible, Estado (chip) y Fecha.
  - Acciones: **Detalle** · menú "⋯" con **Cambiar estado** (diálogo con el select y la justificación; reutiliza el POST existente), **Contactos** y **Eliminar**.
  - El alta con varios bienes va en un `form-panel` "Registrar donación". `api-bien-form` se mantiene dentro de una `.form-grid`.
  - **Detalle de donación:** ficha con los datos del bien, la lista de asignaciones (chips) y las acciones de estado.
- **Importar CSV:** zona de carga con la descripción del formato, el spinner existente durante el envío y el resultado (procesadas, creadas, actualizadas, rechazadas) en KPI, más una tabla de errores por fila.

### 5.2 Entidades y Asignación
- **Beneficiarios:** tabla con razón social, tipo, rubro, ciudad y estado activo. Acciones: Ver/editar · Crear acceso · Asignaciones · menú "⋯" con Eliminar.
- **Necesidades:** filtros por entidad, tipo y estado, y la barra de progreso (`cantidadRecibida / cantidad`). El alta va en un `form-panel`.
- **Matchmaking** (`/admin/dashboard/asignar`), con pestañas:
  - "Sugerir": select de donación + botón, y el resultado con las dos tablas de tops o la intersección destacada. "Asignar" abre un diálogo con la cantidad.
  - "Sugerencias guardadas".
  - "Por entidad".
  - Los algoritmos activos van como toggles en una tarjeta lateral.

### 5.3 Logística
- **Camiones:** tabla con patente, capacidades (en una columna "Carga / Volumen / Altura") y estado (chip). Acciones: Detalle · Cambiar estado (menú con los 3 estados, POST existente) · Eliminar. El alta va en un `form-panel` con `.form-actions`: **arregla el botón gigante**.
- **Rutas:** filtro por fecha, tabla (fecha, camión, chofer, estado, distancia y duración), acciones Iniciar (diálogo con el chofer), Finalizar y Cancelar según el estado, y el formulario "Planificar por fecha" en el `page-header`. El detalle de la ruta incluye sus paradas (absorbe `/paradas`; la ruta `/admin/dashboard/paradas` se mantiene, enlazada desde ahí).
- **Entregas:** filtros por estado; `NO_RECIBIDA` resaltada. Acción de revisión: el PATCH de estado que ya permite `REVISAR_ENTREGAS_FALLIDAS`, si hay ruta en el front. Si no la hay, se deja anotado como mejora y no se inventa.
- **Seguimiento GPS:** mapa a pantalla completa con los marcadores de `GET /camiones/ubicaciones` y una lista lateral (patente, avance %, próxima parada y última actualización).

### 5.4 Incentivos, Catálogo y Sistema
- **Rankings:** podio del mes (3 tarjetas) más una tabla con el resto. La pestaña "Historial" lleva un acordeón por mes.
- **Categorías y misiones:** tarjetas por categoría, en orden (categoría → siguiente), con sus misiones en una lista. "Agregar misión" va en un `form-panel` dentro de cada tarjeta.
- **Consulta por donante:** buscador con un select de donantes y una ficha con las métricas (KPI), la misión actual (barra de progreso), las insignias y el historial de categorías.
- **Catálogo:** árbol categoría → subcategorías, con "Nueva categoría" y "Nueva subcategoría" en `form-panel`.
- **Bienes:** tabla compacta con el mismo patrón que Donaciones.
- **Notificaciones:** tarjeta de estado del servicio ("Comprobar conexión") y el envío manual, solo si está habilitado.

---

## 6. Landing y fotos (Fase G)

Contexto: en el desmaquetado se sacaron todas las imágenes de Unsplash, incluida la foto del hero. Pero la foto del hero es **decorativa** (no representa una donación real) y estaba bien.

- **Hero:** volver a poner una fotografía. Guardarla como asset local en `static/img/hero-donaciones.jpg`, con una imagen de licencia libre (por ejemplo Unsplash License) y su crédito en un comentario. **No** usar el logo `favicon.svg` agrandado.
  - Si hay que descargar la imagen, pedir confirmación antes.
  - Agregar `alt` descriptivo y `width`/`height` para evitar saltos del layout.
- **Galería y "Donaciones destacadas":** representan donaciones **reales** entregadas (requisito UI #1). Siguen vacías hasta que el backend exponga los endpoints públicos (pedido D1). Para que no se vea "rota":
  - Rediseñar el estado vacío: una ilustración o ícono grande, un texto explicativo y un enlace a "Explorar el mapa".
  - Opcional: ocultar la sección "Galería" mientras no haya fotos, y mantener solo "Destacadas" con el estado vacío.
  - **No** volver a poner fotos de stock simulando donaciones reales.
- `favicon.svg` como fallback del popup del mapa: reemplazarlo por un ícono pequeño de paquete. Nunca a tamaño grande.

---

## 7. Fases y orden

| Fase | Contenido | Archivos principales |
|---|---|---|
| A | Componentes base (`admin.css`, `admin-ui.html`), fragmento de navegación agrupada, borrar `api-links` | `dashboard-admin-layout.html`, `admin-api.html`, `admin-complementos.html` |
| B | Panel General | `AdminController`, `PanelAdminService`, `PanelAdminMapper`, `admin-panel.html`, test |
| C | Depósito (Donantes, Donaciones y su detalle, CSV) | `admin-api.html` (secciones), `dashboard-admin-donantes.html`, `admin-perfil.html`, `admin-donante-form.html` |
| D | Entidades y Matchmaking | `admin-api.html`, `fragments/consultas-donaciones.html` |
| E | Logística (Camiones, Rutas, Entregas, GPS) | `admin-api.html`, `fragments/consultas-logistica.html` |
| F | Incentivos, Catálogo, Bienes, Notificaciones | `admin-api.html`, `fragments/consultas-incentivos.html`, `notificaciones.html` |
| G | Landing y fotos | `landing.html`, `WebMapper`, `map.html` |
| H | Verificación final (§8) y limpieza del CSS que quedó sin uso (`pages/admin-dashboard.css` y los de las plantillas borradas) | — |

Opcional, si la plantilla `admin-api.html` queda inmanejable: partirla en una plantilla por módulo (`admin-donaciones.html`, `admin-logistica-camiones.html`, …). Cada controller devuelve la suya, y las claves de modelo no cambian.

---

## 8. Checklist de verificación

- [ ] Toda ruta `/admin/**` del inventario (§0.1) es alcanzable desde la UI y funciona: alta, edición, cambio de estado y baja donde corresponda.
- [ ] No queda la fila `api-links` en ninguna pantalla.
- [ ] Ningún botón de formulario ocupa toda una celda de la grilla, y ninguna celda de tabla contiene un formulario completo.
- [ ] Los enums se ven como chips legibles.
- [ ] 375 px: sin scroll horizontal de página (las tablas pasan a tarjetas), el drawer y la barra inferior funcionan, y los targets miden ≥ 48 px.
- [ ] Foco visible, `aria-label` en los botones de ícono, contraste AA y `role="alert"` en los errores.
- [ ] Spinner (`simulateAsyncAction` existente) en los envíos que tardan más de 300 ms.
- [ ] `mvn -q test` en verde, con los tests nuevos del panel.
- [ ] Hero con foto y estados vacíos prolijos en la landing.

---

## 9. Después de este plan

Plan aparte para los dashboards de **donante** y **entidad**, con los mismos componentes: revisar formularios rotos (alta de necesidad, confirmación de recepción), estados vacíos y barra lateral por permisos.

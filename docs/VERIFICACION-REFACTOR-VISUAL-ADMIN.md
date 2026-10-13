# Verificación del refactor visual de Administración

Fecha: 12/10/2026. Rama: `entrega5-desmaquetado-front`.

## Implementado

- Navegación administrativa compartida entre barra lateral, drawer y barra inferior, agrupada por módulo y respetando permisos.
- CSS administrativo centralizado, encabezados, mensajes, chips, tablas móviles, filtros, diálogos y formularios con acciones separadas de los campos.
- Panel operativo con datos de Donaciones, Logística e Incentivos. Las consultas fallan independientemente: un dato no disponible muestra «—», no un cero inventado.
- Conteos de depósito, vencimientos, necesidades, flota y entregas; listas de atención, rutas del día, ubicaciones reales disponibles y podio mensual.
- Refactor de depósito, entidades, matchmaking, logística, incentivos, catálogo y notificaciones. Se mantienen los permisos y condiciones de habilitación del envío manual.
- Plantilla administrativa dividida en fragmentos por módulo, conservando los nombres de vista y claves de modelo.
- Foto decorativa local en el hero, autorizada por el usuario. Crédito y licencia en `src/main/resources/static/img/CREDITOS.md`. No se utilizan fotos de stock como evidencia de donaciones reales.
- Limpieza del CSS administrativo sin retirar los estilos compartidos por los dashboards personales.

## Evidencia

| Verificación | Resultado |
|---|---|
| Tests Maven con Java 21 | 156 tests, 0 fallos y 0 errores |
| Declaraciones GET/POST administrativas | 74 antes y después, sin diferencias de rutas |
| Renderizado visual | 16 pantallas con datos sintéticos de tests, a 375, 768 y 1280 px |
| Desborde horizontal | No observado en las 48 combinaciones revisadas |
| Tablas en móvil | Presentadas como tarjetas con etiquetas de campos |
| Búsqueda en Donaciones | Oculta/restaura las filas sin modificar datos |
| Cambio de estado | Diálogo abierto/cerrado sin enviar la operación |
| Asignación desde sugerencia | Diálogo precarga la donación y la necesidad seleccionadas |
| Alta de camión en móvil | Botón Crear de 48 px de alto, fuera de la grilla de campos |
| Menú móvil | Apertura, cierre, enlaces por módulo y foco inicial en Cerrar menú comprobados |
| Semántica del layout | Un único elemento main en las pantallas complementarias revisadas |

Pantallas: panel, donantes, donaciones, camiones, rutas, necesidades, seguimiento, asignar, bienes, rankings, catálogo, importar, beneficiarios, incentivos, consulta de incentivos por donante y notificaciones.

Los tests adicionales comprueban conteos, límite de siete días de vencimiento, fallo independiente de una API, permisos, ausencia de escrituras durante la carga del panel, estructura de tablas y formularios, orden de categorías y prevención de ciclos, además del renderizado de las vistas.

Para repetir la exportación visual de fixtures:

```powershell
mvn -q test '-Dvisual.qa=true'
```

Los HTML y la captura `panel-escritorio.jpg` se generan/guardan en `target/visual-qa`, que no se versiona. Los datos de estas capturas son exclusivamente de prueba, no datos reales del sistema.

## Límites y pendientes

- No se ejecutó una prueba integral de todas las operaciones contra microservicios reales. Los tests MVC emplean APIs simuladas; los contratos HTTP existentes no se modificaron.
- La galería pública permanece vacía hasta disponer de los endpoints públicos previstos en D1. No se inventaron datos para rellenarla.
- No se agregó una ruta MVC nueva para revisar entregas fallidas: queda pendiente como mejora funcional, conforme al plan.
- Las entregas no recibidas se ordenan por ID descendente porque la respuesta disponible no expone una fecha de fallo. La interfaz explicita este criterio; no se presenta como una fecha real.
- Se mejoró accesibilidad, pero resta una auditoría WCAG AA completa con teclado, lector de pantalla y contraste de todas las pantallas/estados.
- Los dashboards de donante y beneficiario conservan su alcance y requieren su propio plan de revisión.

No se modificó el backend ni la autenticación. No se hicieron commits ni push.

# Limpieza visual integral — verificación y uso local

Implementación del plan del 13/10/2026. Cambios sin commit ni push; se conservaron los cambios previos del frontend.

## Implementado

- Estilo plano compartido en landing, login, registro, administrador, donante y entidad: sin sombras; colores sólidos salvo el mismo fondo de login/registro; botones blancos con borde negro y texto rojo en hover, sin desplazamiento.
- Campos con bordes neutros, foco negro visible para teclado y errores visibles. Sidebars sin scroll independiente; el menú móvil completo participa del desplazamiento de la página.
- Avatar por inicial, menú debajo y cierre mediante POST. Administración tiene nombre e inicial propios; los demás roles usan el nombre o razón social, con etiqueta del rol como alternativa, nunca el email.
- Acciones Material locales con nombres accesibles; mostrar/ocultar contraseña; Subcategoría con búsqueda, teclado, cierre con Escape y error visible. El select conserva el campo original para el POST.
- Nueva pantalla GET /admin/dashboard/donaciones/nueva, con el POST existente y múltiples bienes. Ante errores de conversión, validación o del servicio se conservan los valores introducidos.
- KPI compactos, navegación a Matchmaking sin ejecutar escrituras y podio oculto ante ranking vacío, cero misiones completadas o fallo de consulta.
- Landing y detalle público consumen el DTO público de Donaciones. Destacadas: hasta tres entregas del mes actual, por fecha descendente. Galería: hasta seis entregas recientes con foto. No se completan con listas ficticias.
- Crear acceso solo para donantes pendientes sin cuenta. Se consulta nuevamente al enviar. Ante falta de email se ofrece un formulario específico que conserva el estado pendiente y los contactos; no se usa el PUT completo del perfil, porque ese PUT marca el registro como completo. Guardar el email no crea cuenta ni envía correo.
- Autenticación conserva su control de duplicados. No se habilitó autoregistro, recuperación de contraseña ni carga general de evidencias.

## Cambios necesarios en el backend

En el repositorio backend, rama ENTREGA-5:

1. Donaciones: V8__publicacion_entregas.sql agrega publicación desactivada por defecto y descripción pública. No reemplaza V6 ni V7.
2. GET /publico/entregas y /publico/entregas/{id}: solo asignaciones entregadas y publicables, con DTO separado (id, descripción, fecha de entrega, cantidad, unidad, imagen). No expone personas, documentos, contactos, direcciones ni GPS.
3. PATCH /donantes/{humanos|juridicos}/{id}/contacto-email: operación administrativa para completar únicamente un email faltante en un perfil pendiente. Conserva SMS/WhatsApp, contacto predeterminado y representantes; rechaza emails existentes o de otro donante.
4. Autenticación: GET /auth/usuarios/existe?rol=...&referenciaId=... devuelve exclusivamente existe, restringido al administrador con permiso de gestión de usuarios. No devuelve credenciales ni perfiles.

La publicación de datos reales sigue siendo opt-in: no hay una operación automática que publique todas las entregas.
El mapa público no consulta coordenadas privadas: muestra el estado vacío y enlaza a la galería pública.
Los mapas privados siguen requiriendo la sesión y los permisos existentes.

## Datos locales

Se cargó explícitamente el conjunto Prueba visual DonaTrack 2026:
tres entregas publicadas, tres entregas privadas en traslado, tres camiones y tres rutas con GPS persistido.
Las fotos son ilustrativas y tienen licencia/crédito en static/img/CREDITOS.md; no se presentan como evidencia real.

La carga no está en migraciones ni en el arranque. Se ejecutó dos veces sin duplicar registros ni sobrescribir los existentes.
Las fechas y posiciones se conservan en ejecuciones posteriores; no simula movimiento ni renueva ejemplos todos los meses.

La guía de carga, comprobación y retirada selectiva está en el backend:
scripts/prueba-visual/README.md.
No usar DROP DATABASE, TRUNCATE ni borrar volúmenes para retirar este conjunto.

## Verificación realizada

| Verificación | Resultado |
|---|---|
| Frontend | 168 tests, sin fallos ni errores |
| Donaciones | 115 tests, sin fallos ni errores |
| Autenticación | 2 tests, sin fallos ni errores |
| Seguridad | Lectura pública anónima; entregas privadas/no entregadas excluidas; DTO sin datos personales; consulta de cuentas solo administrativa |
| Persistencia del contacto | Prueba H2 con flush/relectura: email persistido, estado pendiente y SMS predeterminado conservados |
| Carga local | 6 asignaciones/entregas coherentes, 3 publicadas, 3 camiones con GPS; carga idempotente |
| Pantallas administrativas | 16 pantallas revisadas en 375, 768 y 1280 px |
| Pantallas de donante y entidad | 6 por rol revisadas en los tres tamaños |
| Pantallas públicas | 7 pantallas revisadas en los tres tamaños |
| Ventanas bajas | Menú completo móvil y formulario de contacto en ventanas de 600 px de altura; scroll de página, no sidebar |
| Controles | Avatar/logout POST, contraseña visible/oculta, búsqueda/teclado de Subcategoría, bienes adicionales, foco y error visible |

La revisión midió desbordamientos horizontales y estilos calculados; no encontró sombras ni gradientes fuera del fondo de login/registro.
La prueba de Subcategoría vacía bloqueó el envío antes de llegar al backend.

Incidencia de verificación: una comprobación del diálogo nativo de eliminar produjo una baja lógica accidental del donante 1.
Se verificó el cambio en el registro de MySQL y se restituyó exclusivamente activo=true; no se borraron filas ni se alteraron sus demás campos.
Se retiró el archivo temporal de diagnóstico. No se continuaron pruebas destructivas sobre datos existentes.
Las confirmaciones originales, permisos y POST de eliminar se conservaron; la automatización del diálogo nativo no se considera una comprobación visual exitosa.

## Ejecutar después de los cambios

Reiniciar frontend, Donaciones y Autenticación para cargar las clases nuevas; la recarga de CSS no incorpora por sí sola las APIs nuevas.
El frontend normal usa 8090 y las URLs configuradas en application.properties / variables de entorno.

Durante la verificación se usaron servidores temporales separados: frontend 8091, Donaciones 8085, Logística 8086 y Autenticación 8084.
Los listeners de RabbitMQ estaban detenidos en los servidores temporales de Donaciones/Logística para evitar consumir mensajes externos.
Eso es un entorno de revisión de lectura, no una prueba completa de procesamiento de eventos o envío de notificaciones.
Al terminar se cerraron los servidores y el navegador temporales, conservando los procesos originales y los contenedores.

En esta computadora coexistían dos MongoDB: el nativo en 127.0.0.1:27017 y Docker en [::1]:27017.
La carga y la verificación utilizaron solo Docker. Para ver los camiones de prueba, Logística debe apuntar a ese mismo MongoDB mediante LOGISTICA_DB_URI.
Consultar la guía del backend para el formato de conexión, sin subir credenciales al repositorio.

Comandos de pruebas (Java 21):
- Frontend: mvn test.
- Backend, desde su raíz: mvn -pl donaciones-service,autenticacion-service -am verify.
- Coherencia de datos, desde el backend: .\\scripts\\prueba-visual\\Verificar-PruebaVisual.ps1.

Captura de referencia: target/visual-qa/admin-1280.jpg.

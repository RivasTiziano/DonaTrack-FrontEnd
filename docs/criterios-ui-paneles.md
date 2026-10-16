# Criterios acordados para los paneles de DonaTrack

Recopilación de los ajustes trabajados en Administración y aplicados al panel de persona donante.

- Navegación: sidebar fijo, plegable, logo arriba y solo iconos al cerrarlo. Sin header ni círculo de perfil. Cerrar sesión al pie, con el color del menú y hover rojo.
- Espaciado: contenido cerca del título, márgenes uniformes y distribución adaptable a pantallas pequeñas, sin scroll horizontal.
- Jerarquía: evitar cards dentro de otras cards. Resumen y métricas compactos; conservar agrupaciones cuando ayudan a comprender los datos.
- Texto: títulos breves, sin rutas de navegación ni descripciones introductorias largas. No mostrar identificadores internos.
- Acciones: botones blancos, sin borde y con hover rojo. Las filas o elementos que abren detalles se oscurecen al pasar el mouse y se pueden activar con teclado. Las acciones internas mantienen su propio comportamiento.
- Formularios: abrir la creación desde arriba, sin saltar al final de la pantalla. Usar únicamente acciones que el rol y los servicios soportan.
- Estados: texto de color legible, sin fondos de pastilla innecesarios.
- Datos: porcentajes enteros redondeados; fechas legibles; historial de categorías sin hora ni valores nulos. Explicar estados vacíos y diferenciar los errores de conexión.
- Mapas: navegación con mouse, iconos de camión para los vehículos y detalles sin desplazamiento automático al abrir el popup.
- Validación: verificar las pantallas con y sin actividad, los filtros, los detalles, las notificaciones y las operaciones permitidas. Mantener la separación de datos por sesión y rol.

## Aplicación al panel donante

Se revisaron Resumen, Mis donaciones, Entidades beneficiarias, Incentivos, Seguimiento de entregas y Notificaciones. Se conserva la trazabilidad de asignaciones y entregas, los filtros, la consulta de entidades y la configuración de visibilidad de insignias.

El panel donante no incorpora la gestión administrativa de camiones, rutas, catálogo, donantes o necesidades. Tampoco se agregan acciones de edición o eliminación que no correspondan a ese rol.

## Aplicación al panel de entidad beneficiaria

Se unificaron Resumen, Necesidades, Donaciones asignadas, Seguimiento de entregas, Confirmar recepción y Notificaciones. El sidebar mantiene un estado de apertura independiente del donante y del admin.

Las necesidades y donaciones usan listas compactas con detalles por clic o teclado. Se mantienen las acciones de publicación y eliminación, la confirmación de recepción con fotografías y el aviso de entrega no recibida con justificación. No se agregaron funciones administrativas al rol.

Los filtros del seguimiento actualizan el contador, la lista y los marcadores del mapa; cuando no hay coincidencias, se oculta el mapa y se muestra un aviso. Las cantidades usan singular/plural y el progreso se muestra en verde con gradiente.

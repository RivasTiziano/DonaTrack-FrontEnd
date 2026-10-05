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

No exponer este frontend a Internet: aún no hay autenticación ni autorización.
La selección de rol del login original es una demostración, no una sesión segura.

## Arquitectura

Navegador → Controller MVC → servicio de API → WebApiCallerService → REST.
La respuesta JSON se deserializa a DTO y Thymeleaf genera HTML en el servidor.
Los formularios hacen POST al frontend; este traduce a POST, PUT, PATCH o DELETE
del contrato REST. No hay JPA, acceso a bases ni reglas de negocio en el cliente.

Se conserva el layout administrativo y el sistema de estilos de la entrega 4.
Las operaciones conectadas usan la plantilla compartida `admin-api` y el fragmento
de campos de bienes. Las plantillas antiguas siguen disponibles como referencia
de maquetado; no son prueba de integración funcional.

## Circuitos conectados

| Pantalla | Operaciones reales |
| --- | --- |
| Donantes | Listar humanos y jurídicos, crear humano/jurídico, eliminar |
| Donaciones | Listar, crear con un bien, detalle/asignaciones, cambiar estado de cobertura, eliminar |
| Bienes | Listar, crear, actualizar, eliminar |
| Catálogo | Listar categorías/subcategorías, crear categoría y subcategoría |
| Beneficiarios | Listar |
| Necesidades | Listar, crear extraordinarias/recurrentes, actualizar, eliminar |
| Matchmaking | Activar/desactivar algoritmos, generar sugerencias y ver tops/intersección, confirmar asignación |
| Camiones | Listar, crear, cambiar estado, eliminar |
| Entregas | Listar con asignación y estado de entrega |
| Rutas | Listar, iniciar, finalizar, cancelar |
| Rankings | Ranking mensual: ID del donante y misiones resueltas, tal como devuelve la API |
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
2. **Formularios con colecciones dinámicas.** La creación de donante usa email
   predeterminado, teléfono SMS opcional y un representante inicial para jurídicos.
   Debemos completar selección de medio predeterminado, múltiples contactos y
   múltiples representantes. La edición de perfiles y CRUD completo de beneficiarios
   se deja pendiente para no sobrescribir esas colecciones ni perder datos.
3. **Donaciones con varios bienes.** El formulario inicial registra un bien por envío.
   La API ya admite varios; falta acordar y construir el formulario de filas dinámicas.
4. **Recepción con fotos y planificación de rutas.** La captura/subida de evidencias,
   identidad de quien confirma, mapa/GPS y planificación con n8n son circuitos específicos.
   No se modifican estados de asignación desde un formulario que suplante a Logística.
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

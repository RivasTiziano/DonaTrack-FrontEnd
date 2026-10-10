# Organización de los controllers MVC

Los controllers reciben solicitudes del navegador, preparan el modelo y seleccionan una vista o redirección. Delegan los casos de uso en los servicios del frontend; no construyen DTO de negocio ni recorren formularios repetidos.

| Clase | Responsabilidad |
| --- | --- |
| `AdminController` | Portada del panel administrativo. |
| `DonacionesController` | Donantes, perfiles, beneficiarios, bienes, catálogo, necesidades, donaciones, asignaciones, sugerencias y CSV. |
| `LogisticaController` | Camiones, entregas, rutas, paradas y seguimiento. |
| `IncentivosController` | Categorías, misiones, métricas, insignias y rankings. |
| `NotificacionesController` | Funcionalidades de notificaciones disponibles para las vistas. |
| `WebController` | Inicio, legal, privacidad y portadas de dashboards que reúnen varios servicios. |
| `AutenticacionController` | Login y registro demostrativos; pendiente autenticación real. |

`controllers/support/ApiViewSupport` concentra apoyo de presentación. `exceptions/FrontendExceptionHandler` maneja errores de solicitudes MVC.

Se conservaron las URL, los formularios y las plantillas existentes. La agrupación por microservicio no otorga permisos administrativos: esa protección sigue pendiente de autenticación y autorización.

## Capas del cliente liviano

| Capa | Responsabilidad |
| --- | --- |
| Controllers | Binding, errores de formato, modelo, mensajes, selección de vista y redirecciones. |
| `DonacionesService`, `LogisticaService`, `IncentivosService`, `NotificacionesService` | Coordinar las operaciones del frontend y las llamadas a la API. |
| `WebService` | Reunir los datos para páginas públicas y dashboards, sin recibir `Model` ni sesión. |
| `validators/` | Validar entrada del cliente: datos obligatorios, formularios incompletos, contacto predeterminado informado, cantidades y formatos. |
| `mappers/` | Transformar formularios y respuestas en DTO o datos de las plantillas. No hacen HTTP ni validan reglas del dominio. |
| `*ApiService` | URL, verbo HTTP y contrato del microservicio. |
| Backend | Reglas de negocio, identidad y autorización, persistencia, estados y eventos. |

Los servicios vuelven a validar la entrada de las escrituras para que otro consumidor del servicio no pueda saltarse el validator del controller. Esa validación local no reemplaza a la del backend.

El registro de personas jurídicas ya no inventa representantes, documentos, fechas ni géneros. Las filas parciales muestran un error antes de llamar al backend. En errores se conservan también los contactos y representantes adicionales.

Editar datos básicos lee el perfil completo antes del PUT: el mapper conserva los contactos, el predeterminado y los representantes que esa pantalla no edita.

Los resultados HTTP y los resultados funcionales son diferentes: Notificaciones interpreta `FALLIDA`, `PENDIENTE` y `COMPLETADA` en su servicio; el controller decide cómo mostrar el resultado. Logística solo confirma la aceptación de una planificación, no la creación inmediata de rutas.

La sesión, el token antirreenvío y el bloqueo del envío manual permanecen en el controller de Notificaciones porque pertenecen a la petición web, no a la lógica de negocio.

## Alcance de los dashboards originales

Explorar donaciones, sus detalles, las donaciones y entidades del donante y las necesidades y donaciones del beneficiario pertenecen a DonacionesController. Incentivos del donante pertenece a IncentivosController. Entregas de ambos perfiles y la pantalla de confirmación pertenecen a LogisticaController.

Las URL y plantillas no cambian. Cada pantalla carga sus datos mediante el service correspondiente; ya no consulta un dashboard completo de varios microservicios. Las cabeceras personales todavía usan datos demostrativos, no una identidad autenticada.

AutenticacionService y AutenticacionMapper concentran exclusivamente la navegación y las opciones de registro existentes. Sus POST no crean usuarios, verifican contraseñas ni emiten tokens. La confirmación de recepción conserva únicamente la pantalla actual: no se agregó una operación real de confirmación.

La refactorización conserva el maquetado y la navegación de demostración del login. No convierte esas pantallas en sesiones autenticadas ni verifica contraseñas. Los datos ilustrativos de los dashboards personales siguen siendo provisionales y se encuentran en `DonanteDashboardMapper` y `BeneficiarioDashboardMapper`; no deben interpretarse como métricas personales reales. Conectar identidad y reemplazar esos ejemplos sigue pendiente de autenticación.

## Verificación

`mvn clean test` verifica rutas y plantillas existentes, escrituras de los services, transformaciones y validators. Las pruebas utilizan APIs simuladas: no crean cuentas, envían correos ni modifican bases reales.

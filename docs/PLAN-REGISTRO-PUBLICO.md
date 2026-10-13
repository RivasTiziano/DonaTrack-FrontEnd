# Plan — Registro público y arreglos de "Crear acceso"

Estado: **aprobado por Tomás (2026-10-13)** con estas decisiones: sin auto-login, sin selector Leaflet y sin tocar la galería de la landing (queda como en eb16201). Alcance: A1, A2, R1–R5. Sin commits. Base: rama `entrega5-desmaquetado-front` @ eb16201.
Contrato del backend: E1 en ENTREGA-5 @ f594f86. El front se adapta a ese contrato tal cual está.

Cada ítem cita el enunciado (`../2026-tpa-ma-ma-grupo-18/docs/local/enunciado-tp-anual-2026.md`).
Lo que no tiene respaldo va marcado como **EXTRA** o **INTERPRETACIÓN**.

---

## 0. Reglas
- Arquitectura: Controller → Service → ApiService. Validators y mappers como en el resto del front.
- La contraseña nunca vuelve al formulario, nunca se loguea y nunca se guarda en la sesión.
- No usar estilos inline nuevos. Reutilizar `registration.css` y los componentes existentes.
- `mvn -q test` en verde al terminar cada parte. Sin commits salvo que se pidan; **nunca** push; los commits no llevan `Co-Authored-By`.

## 1. Contrato del backend (E1, sin token, donaciones-service :8080)

| Tipo | Ruta |
|---|---|
| Donante humano | `POST /publico/registro/donantes-humanos` |
| Donante jurídico | `POST /publico/registro/donantes-juridicos` |
| Entidad beneficiaria | `POST /publico/registro/entidades-beneficiarias` |

- **Body:** `{ "perfil": <DTO de alta>, "email": "...", "contrasenia": "..." }`.
  - `contrasenia` tiene al menos 12 caracteres.
  - `email` tiene que estar entre los medios de contacto del perfil. En la entidad, tiene que ser el contacto predeterminado o el correo de algún representante.
- **Perfil humano:**
  - Campos: `nombre`, `apellido`, `fechaNacimiento` (yyyy-MM-dd), `numeroDocumento` (siempre DNI), `genero`, `direccion`, `mediosDeContacto`, `medioDeContactoPredeterminado`.
  - `genero` es uno de FEMENINO | MASCULINO | NO_BINARIO | OTRO | PREFIERE_NO_INFORMAR.
  - `direccion` tiene la forma `{calle, numero, ciudad, provincia, latitud?, longitud?}`.
  - `mediosDeContacto` es una lista de `{tipoMedioContacto: EMAIL|SMS|WHATSAPP, formaContacto}`. El teléfono va como SMS.
  - `medioDeContactoPredeterminado` es obligatorio.
- **Perfil jurídico:**
  - Campos: `numeroDocumento` (CUIT), `razonSocial`, `tipoEntidad`, `rubro`, `direccion`, `mediosDeContacto`, `medioDeContactoPredeterminado`, `representantesJuridicos`.
  - `tipoEntidad` es uno de GUBERNAMENTAL | ONG | EMPRESA | INSTITUCION.
  - `representantesJuridicos` es una lista de `{nombre, apellido, numeroDocumento, fechaNacimiento, genero, tipoDocumento?}`.
- **Perfil entidad:**
  - Campos: `razonSocial`, `tipoEntidad`, `rubro`, `direccion` (con latitud y longitud recomendadas), `medioDeContactoPredeterminado`.
  - Opcional: `representantes`, lista de `{nombre, telefono, correos:[MedioDeContacto]}`.
- **Respuesta 201:** `{id, rol, email}`, **sin token**.
- **Errores**, todos con la forma `{message}`:

| Código | Causa |
|---|---|
| 400 | Validación (llega solo el primer campo inválido) o email que no está entre los contactos. También contraseña corta. |
| 409 | `"Ya existe una cuenta con ese email"`. |
| 409 | Unicidad (por ejemplo, documento duplicado). El mensaje es genérico. |
| 503 | No se pudo crear la cuenta. |

- El autoregistro no publica `CuentaCreada`. Es correcto: el enunciado pide el mail de bienvenida solo en el alta que hace el admin en el depósito (Entrega 1, "Registro de personas donantes").

---

## Parte R — Registro propio (UI/UX #3 y #4)

### R1. Cliente y servicio
**Respaldo:** UI/UX #3 ("registro de personas donantes y entidades beneficiarias") y Entrega 5 ("Cliente Liviano desacoplado que consume las APIs REST").

- `dto/RegistroDtos.java`, con records que reflejan el contrato:
  - `RegistroHumano(Humano perfil, String email, String contrasenia)`;
  - `RegistroJuridico(...)`;
  - `RegistroEntidad(...)`;
  - `Registrado(Long id, String rol, String email)`.
  - Reutilizar los records de `DonanteRequests` y `DonacionesDetalles` donde coincidan (Direccion, MedioDeContacto, Representante).
  - `toString` **sin** la contraseña.
- `services/RegistroApiService.java`:
  - cliente público **sin Bearer**, con `RestClient` propio y timeouts de 3 s (conexión) y 10 s (lectura), igual que `PublicoService`;
  - tres métodos POST, uno por ruta.
- `services/RegistroService.java`:
  - valida con `RegistroValidator`;
  - arma los DTO con `RegistroMapper`;
  - llama al ApiService;
  - traduce los errores (ver R4).

### R2. Formularios
**Respaldo:** Entrega 1, "Servicio de Donaciones - Donantes" (para humano y jurídico) y "Entidades beneficiarias y necesidades" (para la entidad).

Se reescriben las tres plantillas y se conservan las rutas actuales:

| Ruta | Plantilla |
|---|---|
| `/registro` | `register.html` |
| `/registro/donante-humano` | `register-donor-human.html` |
| `/registro/donante-organizacion` | `register-donor-org.html` |
| `/registro/entidad-beneficiaria` (alias `/registro/beneficiario`) | `register-beneficiary.html` |

**Humano**
- nombre y apellido;
- fecha de nacimiento (input `date`). El enunciado pide "edad"; el backend pide la fecha y calcula la edad, lo que es aceptable frente a la Entrega 1;
- número de documento (DNI);
- género (select con los 5 valores);
- dirección: calle, número, ciudad y provincia;
- contactos:
  - email (obligatorio);
  - teléfono (opcional, se envía como SMS);
  - WhatsApp (opcional);
  - radio "contacto predeterminado" entre los cargados (Entrega 1: "podrá determinar cuál de ellos será su medio de contacto predeterminado");
- contraseña y confirmación;
- el `email` de la cuenta es el del contacto EMAIL. No hay un campo aparte, así se cumple la regla del backend.

**Jurídico**
- CUIT;
- razón social;
- tipo (GUBERNAMENTAL, ONG, EMPRESA, INSTITUCION);
- rubro;
- dirección;
- contactos y predeterminado, como en el humano;
- **representantes repetibles**, al menos uno (Entrega 1: "personas representantes habilitadas"): nombre, apellido, documento, fecha de nacimiento, género y tipo de documento opcional;
- contraseña y confirmación.

**Entidad**
- razón social;
- tipo;
- rubro;
- dirección completa;
- email de contacto predeterminado;
- **representantes opcionales repetibles**: nombre, teléfono y correos (Entrega 1: "teléfono y correos de las personas representantes designadas");
- contraseña y confirmación;
- el email de la cuenta es el predeterminado.
- Se **saca** el campo CUIT: no lo piden ni el enunciado ni el backend.
- **Sin selector de mapa (decisión de Tomás):** no se envían `latitud` ni `longitud`. La dirección estructurada sí va.

**Elementos comunes a los tres formularios** (Entrega 4, "Consideraciones para el Maquetado"):
- etiquetas con obligatorios marcados;
- placeholders;
- ayudas;
- spinner al enviar;
- los representantes se agregan y quitan con JS progresivo. Sin JS, se muestran 2 bloques fijos.

### R3. Validación del lado del cliente
`validators/RegistroValidator.java`. **Respaldo:** Entrega 4, "Gestión de estados" y "microcopy".

- Obligatorios según la sección 1.
- Formato de email.
- Fecha de nacimiento en el pasado (y razonable).
- Contraseña de al menos 12 caracteres, igual a la confirmación.
- El predeterminado tiene que ser uno de los contactos cargados.
- En el jurídico, al menos un representante completo.
- En la entidad, si se carga un representante, nombre y al menos un correo o teléfono.
- Los errores se muestran junto al campo; se conservan todos los valores **salvo la contraseña**.

### R4. Después del 201 y manejo de errores
**Respaldo:** UI/UX #3 y #4. **Sin auto-login (decisión de Tomás):** el enunciado no lo exige.

- Si el registro devuelve 201, redirige a `/login?registrado`. `login.html` muestra el aviso "Tu cuenta fue creada. Iniciá sesión." No se llama a `/auth/login` ni se crea una sesión.
- Errores (siempre se muestra el `message` en un banner `role="alert"`):

| Respuesta | Qué muestra el front |
|---|---|
| 400 | El `message` del backend tal cual. |
| 409 con "Ya existe una cuenta con ese email" | Ese mensaje, más el enlace "¿Ya tenés cuenta? Iniciá sesión". |
| Otro 409 | "Ya existe un registro con esos datos (email o documento). Si te registraron en el depósito, pedí tu acceso a la administración." Cubre a los donantes cargados por el admin o el CSV hasta E2. |
| 503 o error de conexión | "No se pudo crear la cuenta. Probá de nuevo más tarde." No se reintenta automáticamente. |

- Borrar `registroNoHabilitado` y el mensaje "El registro en línea todavía no está habilitado".
- En `login.html`, sumar el aviso para `param.registrado`.

### R5. Tests
- `RegistroMvcTest`, con `@MockBean RegistroApiService` (y `AutenticacionApiService`, para verificar que no se lo llama):
  - los tres registros exitosos redirigen a `/login?registrado`, sin llamar a `AutenticacionApiService`;
  - el 409 por email muestra el enlace al login;
  - el 409 genérico muestra el mensaje de "pedí tu acceso";
  - el 400 muestra el `message`;
  - el 503 muestra su mensaje;
  - una contraseña corta o una confirmación distinta no llaman a la API;
  - un predeterminado que no está entre los contactos no llama a la API;
  - en el jurídico sin representante no llama a la API;
  - la contraseña nunca aparece en el HTML de respuesta.
- `RegistroMapperTest`: el JSON tiene exactamente la forma `{perfil, email, contrasenia}`; el teléfono va como SMS; el `email` de la cuenta es el del contacto EMAIL o el predeterminado.
- Actualizar `PublicoYRegistroMvcTest` y `DistribucionControllersMvcTest`: hoy esperan que el registro **no** llame a la API.

---

## Parte A — Arreglos de "Crear acceso" (regresiones del commit eb16201)

### A1. Los donantes dados de alta por el admin no pueden recibir acceso
**Respaldo:** Entrega 1, "Registro de personas donantes" (el admin registra y la persona accede) y UI/UX #4.
**Contradicción actual:** `AccesoService.habilitado()` exige `estadoRegistro == PENDIENTE_COMPLETAR`, y ese estado solo lo asigna el CSV.

- `habilitado(tipo, id)` pasa a `!auth.existeUsuario(rol, id)` para **todos** los estados, como ya pasa con las entidades.
- `completarEmail(...)` mantiene su restricción propia: solo `PENDIENTE_COMPLETAR` y sin email, que es lo que acepta el PATCH del backend.
- Test: un donante en estado COMPLETO y sin cuenta puede abrir el formulario y crear el acceso. Un donante con cuenta queda bloqueado.

### A2. Dos llamadas por fila en el listado de donantes
**Respaldo:** UI/UX #20 (CSV de más de 10.000 filas) y Entrega 4 (usabilidad e indicadores asincrónicos).
**Problema:** `DonacionesController.listar` llama a `accesos.habilitado()` por cada fila en `PENDIENTE_COMPLETAR`: lee el perfil y consulta `/auth/usuarios/existe`. Después de un CSV eso son miles de llamadas.

- Sacar el bucle. El botón "Crear acceso" se muestra en **todas** las filas, como un enlace al formulario.
- El formulario (`AccesoController.formulario`) ya verifica con `/existe`. Si la persona ya tiene cuenta, en lugar del `redirect:` silencioso actual, muestra el aviso "Esta persona ya tiene acceso".
- Cuando el backend exponga el batch **X4** (`GET /auth/usuarios?rol=&referenciaIds=`), volver a mostrar el badge "Tiene acceso" con **una** llamada por página.
- Test: el listado no llama a `AutenticacionApiService` ni a `GET /donantes/{tipo}/{id}`.

---

## Orden sugerido
1. **A1** (corrección chica y bloqueante para la verificación manual).
2. **A2**.
3. **R1 → R3 → R2 → R4 → R5.**
4. Verificación manual con Docker:
   - autoregistro de los tres tipos, que debe redirigir al login y permitir entrar con la cuenta nueva;
   - registro con el email de un donante cargado por el admin, que debe dar el 409 con el mensaje de "pedí tu acceso";
   - "Crear acceso" sobre un donante COMPLETO.

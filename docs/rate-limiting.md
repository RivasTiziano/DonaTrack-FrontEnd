# Rate limiting del backend: lo que hace el front (Entrega 6, req. 4)

El límite de solicitudes vive en los servicios del backend (`common-lib`, Bucket4j). El diseño completo
está en el repositorio del backend: `docs/entrega6/rate-limiting.md`. Este documento explica solo la
parte del front.

## Por qué el front tiene que ayudar

Este front es un **cliente liviano** (SSR): el navegador le habla a este servidor y es este servidor el
que llama a los servicios. Sin ayuda, el backend vería a todos los visitantes anónimos con la IP del
front y les daría **un único cupo compartido**. Por ejemplo, 5 registros cada 10 minutos o 60 cargas por
minuto de la landing entre todos.

## Qué hace

`IpDelNavegadorInterceptor` (`services/internal`) se agrega a **todos** los `RestClient` del front:
`WebApiCallerService` (paneles), `AutenticacionApiService` (login, refresh, logout y primer acceso),
`RegistroApiService` (registro público) y `PublicoService` (landing y mapa). Los tres últimos son
justamente los anónimos, que el backend limita por IP. En cada llamada a los servicios manda:

| Header | Valor |
|---|---|
| `X-Front-Auth` | `AUTH_FRONT_KEY`: el backend solo le cree a `X-Client-IP` si trae esta clave |
| `X-Client-IP` | La IP del navegador que este servidor está atendiendo |

- En Render hay un proxy delante del front. La IP del navegador es la **última** de `X-Forwarded-For`, la
  que agrega ese proxy (`RATELIMIT_TRUST_FORWARDED_FOR=true`). Las anteriores las puede inventar el navegador.
- En local, sin proxy, es la IP de la conexión.
- Sin `AUTH_FRONT_KEY` o sin un request de navegador en curso, no se agrega nada.
- Las llamadas en paralelo del panel admin (`PanelAdminService`) copian el request al hilo, así que también
  llevan la IP.

## Cuando el backend responde 429

- **Login y registro:** el formulario muestra "Hiciste demasiados intentos seguidos. Esperá N segundos y
  volvé a intentar." (`ApiErrorMessages`).
- **Cualquier otra página:** `LimiteSolicitudesExceptionHandler` muestra `templates/error/429.html` y le
  devuelve al navegador el mismo 429 con `Retry-After`.
- Los segundos salen del header `Retry-After`; si no viene, del campo `reintentarEnSegundos` del body. Si no
  hay ninguno, se usa un minuto.

## Configuración

| Variable | Local (compose) | Render |
|---|---|---|
| `AUTH_FRONT_KEY` | `local-development-front-key-change-me` | **Copiar** el valor que Render generó en el grupo `donatrack-shared-env` del backend (`sync: false`) |
| `RATELIMIT_TRUST_FORWARDED_FOR` | `false` | `true` |

`AUTH_FRONT_KEY` tiene que ser **igual** en el front y en los cinco servicios. Si no coincide, el sistema
funciona igual, pero los visitantes anónimos vuelven a compartir el cupo de la IP del front.

## Pruebas

- `RateLimitIpDelNavegadorTest`: un servidor HTTP local comprueba los headers enviados (clave e IP, última IP
  de `X-Forwarded-For` detrás del proxy, sin clave, sin request, IP inválida).
- `RateLimitClientesAnonimosTest`: login, logout, registro, landing y mapa también mandan la IP del navegador.
- `RateLimitMvcTest`: el login bloqueado muestra cuánto esperar y una página sin manejo propio responde 429
  con `Retry-After` y la vista `error/429`.

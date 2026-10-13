# Cómo retomar — desmaquetado de la Entrega 5

Estado al 2026-10-12. El plan completo está en [PLAN-DESMAQUETADO-E5.md](PLAN-DESMAQUETADO-E5.md).

## Estado por fase

| Fase | Tema | Estado |
|---|---|---|
| 1 | Sesión JWT, login y logout reales, token en cada llamada, acceso por rol | Hecha y revisada |
| 2 | Notificaciones por token (sin email ni teléfono) | Hecha y revisada |
| 3 | DTO alineados con el backend | Hecha y revisada |
| 4 | Panel del donante con datos reales | Hecha y revisada |
| 5 | Panel de la entidad con datos reales (necesidades, asignaciones, confirmar recepción) | Hecha y revisada |
| 6 | Páginas públicas sin datos inventados | Hecha y revisada |
| 7 | Registro público con un aviso honesto | Hecha y revisada |
| 8 | Admin: "Crear acceso", ranking con nombres, borrar plantillas viejas | Hecha y revisada |
| 9 | Documentación (ENTREGA5-MVC, README, CONTROLLERS-MVC) | Hecha |

## Próximos pasos
1. **Verificación manual** con Docker (sección siguiente): login con cada rol, "Crear acceso", alta de necesidad como entidad, asignación como admin y confirmación de recepción.
2. **Integrar la rama** `entrega5-desmaquetado-front` a `main` (decide Tomás; push solo con su OK).
3. Cuando el backend resuelva C1, X1/C3, C4, D1, E1/E2 y F1: reemplazar los comentarios `// PROVISORIO` (buscar con `grep -rn PROVISORIO src/main`).
4. **Refactor visual** de dashboards y barra lateral por permisos (pendiente, fuera del plan).

## Entorno local con Docker
- **Backend:** `docker compose up -d` en `../2026-tpa-ma-ma-grupo-18`, rama ENTREGA-5.
  - El `.env` local necesita `AUTH_ADMIN_EMAIL` y `AUTH_ADMIN_PASSWORD` (mínimo 12 caracteres), o no se crea el admin inicial.
  - Si `incentivos`, `notificaciones` o `logística` no arrancan por "Access denied" o "featureCompatibilityVersion", sus volúmenes son viejos. Hay que borrarlos, y se pierden esos datos.
- **Front:** `docker compose up -d --build` en este directorio. El compose ya incluye `AUTENTICACION_API_URL=http://autenticacion-service:8084`.
  - El front sirve en http://localhost:8090.
- **Credenciales de prueba:** están en `.credenciales-locales.txt`, en la máquina de Tomás. Ese archivo no se commitea.
  - Hay cuentas admin, donante humano, donante jurídico y entidad.
  - Otra persona tiene que crear su propio admin con el `.env` y crear las demás cuentas desde "Crear acceso" (fase 8).

## Bloqueantes del backend (pedidos, no aplicados)
Ver la sección 10 del plan.

| Pedido | Qué falta | Consecuencia en el front |
|---|---|---|
| **C1** | `GET /donaciones?donanteId` | Desde el commit 035387d, `GET /donaciones` es solo para admin. El donante no ve su historial: requisito 5. |
| **X1/C3** | La entidad no obtiene el `entregaId` | La confirmación de recepción pide el número de entrega a mano: requisito 12. |
| **C3/C4** | Seguimiento por dueño | No se muestra la ubicación de los camiones: requisitos 9 y 14. |
| **D1** | Endpoints públicos | La landing y el mapa quedan vacíos: requisitos 1 y 2. |
| **E1/E2** | Registro público y CuentaCreada con primer acceso | Requisitos 3 y 20. |
| **F1** | Subida de fotos | Hoy las fotos se mandan como URLs. |

## Pendiente fuera del plan
- Refactor visual de los dashboards y de la barra lateral por permisos. Queda **después** del desmaquetado.

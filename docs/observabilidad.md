# Observabilidad del frontend

Para Docker local, levantar primero el backend con su complemento de observabilidad y ejecutar:

```powershell
docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml up -d --build
```

Jaeger está en http://localhost:16686 y muestra el servicio `frontend` después de navegar.
El agente instrumenta Spring MVC y los clientes HTTP compatibles, propagando traceparent
para unir los spans del frontend y el backend. Los logs incluyen trace_id y span_id.

Para Render, los archivos Dockerfile y render.yaml preparan el mismo agente.
Crear primero el colector del backend siguiendo observabilidad/RENDER.md y configurar:

- OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://URL-COLECTOR/v1/traces
- OTEL_EXPORTER_OTLP_HEADERS=authorization=Bearer TOKEN

El frontend ya publicado responde en https://donatrack-frontend-kkfk.onrender.com.
Estos cambios locales necesitan publicarse y redesplegarse para afectar esa aplicación.

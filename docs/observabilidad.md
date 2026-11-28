# Observabilidad del frontend

Para ejecutar con observabilidad (local o en AWS EC2), levantar primero el backend con su complemento de observabilidad y luego ejecutar en el frontend:

```bash
docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml -f docker-compose.monitoreo.yml -f docker-compose.aws.yml up -d --build
```

Jaeger queda escuchando en `127.0.0.1:16686` (accesible localmente o por túnel SSH desde AWS) y muestra el servicio `frontend` después de navegar.
El agente Java OpenTelemetry instrumenta Spring MVC y los clientes HTTP compatibles, propagando el encabezado estándar W3C `traceparent` para unir los spans del frontend y del backend. Los logs incluyen `[trace_id=... span_id=...]`.

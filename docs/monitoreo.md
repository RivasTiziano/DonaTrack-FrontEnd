# Monitoreo del frontend

Se incorpora Actuator y métricas Prometheus.

- `/livez` en el puerto HTTP normal verifica la aplicación viva.
- `/readyz` verifica que terminó de arrancar.
- `/actuator/prometheus` está en el puerto interno de administración `9090`.
- Por defecto administración escucha en loopback (`127.0.0.1`) para no exponerse públicamente.
- En Docker (`docker-compose.monitoreo.yml` + `docker-compose.aws.yml`) el complemento habilita `MANAGEMENT_SERVER_ADDRESS=0.0.0.0` dentro de la red interna de Docker para que Prometheus lo consulte sin publicar el puerto 9090 al host.

Después de levantar el backend con su monitoreo y perfil AWS, ejecutar:

```bash
docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml -f docker-compose.monitoreo.yml -f docker-compose.aws.yml up -d --build
```

El supervisor del backend detecta este contenedor por `donatrack.autorestart=true`.
El healthcheck comprueba `/livez`, espera el arranque y tolera cuatro fallos consecutivos.
El frontend no se reinicia automáticamente por una caída de una API externa.

Grafana corre en `127.0.0.1:3000/d/donatrack-salud` (accesible vía túnel SSH en la EC2).
Los límites, alertas y procedimientos están en `monitoreo/README.md` del backend.
El límite de memoria es configurable mediante `AWS_FRONTEND_JAVA_OPTIONS` / `MONITORING_JAVA_OPTIONS`.

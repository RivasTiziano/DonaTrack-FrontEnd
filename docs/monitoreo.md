# Monitoreo del frontend

Se incorpora Actuator y métricas Prometheus.

- `/livez` en el puerto HTTP normal verifica la aplicación viva.
- `/readyz` verifica que terminó de arrancar.
- `/actuator/prometheus` está en el puerto interno de administración 9090.
- Por defecto administración escucha en loopback para no exponerse en Render.
- En Docker local el complemento configura la escucha interna para Prometheus sin publicar ese puerto al host.

Después de levantar el backend con su monitoreo, ejecutar:

```powershell
docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml -f docker-compose.monitoreo.yml up -d --build
```

El supervisor del backend detecta este contenedor por `donatrack.autorestart=true`.
El healthcheck comprueba `/livez`, espera el arranque y tolera cuatro fallos consecutivos.
El frontend no se reinicia automáticamente por una caída de una API externa.

Grafana está en http://localhost:3000/d/donatrack-salud.
Los límites, alertas y procedimientos están en `monitoreo/README.md` del backend.
El límite de memoria local es configurable mediante `MONITORING_JAVA_OPTIONS`.

Para Render, publicar la imagen nueva y configurar el health check en `/livez`, como indica `render.yaml`.
El reinicio lo administra Render. Este Compose no se aplica al despliegue remoto.

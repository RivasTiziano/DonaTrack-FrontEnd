package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificacionesApiService {
    private final WebApiCallerService http;
    private final String url;
    public NotificacionesApiService(WebApiCallerService http, @Value("${apis.notificaciones.url}") String url) {
        this.http = http; this.url = url.replaceAll("/+$", "");
    }
    public boolean disponible() {
        return "Hola desde el servicio de Notificaciones.".equals(http.get(url + "/health", String.class));
    }
    public Respuesta enviar(Enviar request) {
        Respuesta response = http.post(url + "/notificaciones/enviar", request, Respuesta.class);
        if (response == null || response.retorno() == null || response.datos() == null)
            throw new IllegalStateException("La API devolvió una respuesta de envío incompleta.");
        return response;
    }
}

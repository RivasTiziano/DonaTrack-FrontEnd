package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Cliente de Notificaciones. La identidad sale del token de la sesión: ya no se envían email ni teléfono.
 * Las consultas de historial y de lectura son solo de la persona autenticada.
 */
@Service
public class NotificacionesApiService {
    private final WebApiCallerService http;
    private final String url;

    public NotificacionesApiService(WebApiCallerService http, @Value("${apis.notificaciones.url}") String url) {
        this.http = http;
        this.url = url.replaceAll("/+$", "");
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

    public List<Recibida> listar(boolean soloNoLeidas, int limite) {
        var response = http.get(uri("/notificaciones", soloNoLeidas, limite), RespuestaLista.class);
        if (!"success".equals(response.retorno()) || response.datos() == null)
            throw new IllegalStateException("La API devolvió un historial incompleto.");
        return response.datos();
    }

    /** La identidad sale del token de la sesión, no de un parámetro. */
    public long contarNoLeidas() {
        var response = http.get(url + "/notificaciones/no-leidas/cantidad", RespuestaCantidad.class);
        if (!"success".equals(response.retorno()) || response.datos() == null)
            throw new IllegalStateException("La API devolvió una cantidad incompleta.");
        return response.datos().noLeidas();
    }

    public void marcarLeida(Long id) {
        http.patch(URI.create(url + "/notificaciones/" + id + "/leida"));
    }

    public void marcarTodasLeidas() {
        http.patch(URI.create(url + "/notificaciones/leidas"));
    }

    private URI uri(String path, boolean soloNoLeidas, int limite) {
        var builder = UriComponentsBuilder.fromUriString(url + path).queryParam("limite", limite);
        if (soloNoLeidas) builder.queryParam("soloNoLeidas", true);
        return builder.build().toUri();
    }
}

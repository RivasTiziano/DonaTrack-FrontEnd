package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Recibida> listar(String email, String telefono, boolean soloNoLeidas) {
        var response = http.get(uri("/notificaciones", email, telefono, soloNoLeidas ? Map.of("soloNoLeidas", "true") : Map.of()),
                RespuestaLista.class);
        if (!"success".equals(response.retorno()) || response.datos() == null)
            throw new IllegalStateException("La API devolvió un historial incompleto.");
        return response.datos();
    }

    public long contarNoLeidas(String email, String telefono) {
        var response = http.get(uri("/notificaciones/no-leidas/cantidad", email, telefono, Map.of()), RespuestaCantidad.class);
        if (!"success".equals(response.retorno()) || response.datos() == null)
            throw new IllegalStateException("La API devolvió una cantidad incompleta.");
        return response.datos().noLeidas();
    }

    public void marcarLeida(Long id, String email, String telefono) {
        http.patch(uri("/notificaciones/" + id + "/leida", email, telefono, Map.of()));
    }

    public void marcarTodasLeidas(String email, String telefono) {
        http.patch(uri("/notificaciones/leidas", email, telefono, Map.of()));
    }

    /** Codifica cada valor por separado: un teléfono "+54..." no debe llegar como " 54...". */
    private URI uri(String path, String email, String telefono, Map<String, String> extra) {
        var builder = UriComponentsBuilder.fromUriString(url + path);
        Map<String, Object> values = new HashMap<>();
        if (email != null && !email.isBlank()) { builder.queryParam("email", "{email}"); values.put("email", email.trim()); }
        if (telefono != null && !telefono.isBlank()) { builder.queryParam("telefono", "{telefono}"); values.put("telefono", telefono.trim()); }
        extra.forEach((name, value) -> { builder.queryParam(name, "{" + name + "}"); values.put(name, value); });
        return builder.encode().buildAndExpand(values).toUri();
    }
}

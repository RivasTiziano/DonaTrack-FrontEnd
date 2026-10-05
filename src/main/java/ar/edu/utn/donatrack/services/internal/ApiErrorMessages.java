package ar.edu.utn.donatrack.services.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ApiErrorMessages {
    private final ObjectMapper mapper;
    public ApiErrorMessages(ObjectMapper mapper) { this.mapper = mapper; }
    public String describe(Exception exception) {
        if (exception instanceof RestClientResponseException response) {
            if (response.getStatusCode().is5xxServerError())
                return "El servicio no pudo completar la operación. Consultá su disponibilidad antes de volver a enviarla.";
            try {
                var json = mapper.readTree(response.getResponseBodyAsString());
                String message = json.path("message").asText(json.path("mensaje").asText(""));
                if (!message.isBlank()) return message;
            } catch (Exception ignored) { /* No exponemos HTML, trazas ni cuerpos sin interpretar. */ }
            return "El servicio rechazó la solicitud (HTTP " + response.getStatusCode().value() + "). Revisá los datos y sus restricciones.";
        }
        return "No se pudo comunicar con el servicio. No se confirmó la operación; verificá el resultado antes de reintentar.";
    }
}

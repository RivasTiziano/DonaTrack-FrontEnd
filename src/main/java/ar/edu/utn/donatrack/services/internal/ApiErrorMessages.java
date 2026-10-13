package ar.edu.utn.donatrack.services.internal;

import com.fasterxml.jackson.databind.JsonNode;
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
            if (response.getStatusCode().value() == 401)
                return "Tu sesión venció o no es válida. Volvé a iniciar sesión.";
            if (response.getStatusCode().value() == 403)
                return "Tu usuario no tiene permiso para esta operación.";
            try {
                var json = mapper.readTree(response.getResponseBodyAsString());
                String message = primerTexto(json, "message", "mensaje", "detail", "title");
                if (!message.isBlank() && !message.matches("(?is).*\\b(?:java|sql|jdbc|exception|stacktrace|localhost|http|endpoint)\\b.*")) return message;
            } catch (Exception ignored) { /* No exponemos HTML, trazas ni cuerpos sin interpretar. */ }
            return "No se pudo completar la solicitud. Revisá los datos ingresados.";
        }
        return "No se pudo comunicar con el servicio. No se confirmó la operación; verificá el resultado antes de reintentar.";
    }

    // Acepta el formato propio de las APIs (message/mensaje) y ProblemDetail (detail/title).
    private String primerTexto(JsonNode json, String... campos) {
        for (String campo : campos) {
            String valor = json.path(campo).asText("");
            if (!valor.isBlank()) return valor;
        }
        return "";
    }
}

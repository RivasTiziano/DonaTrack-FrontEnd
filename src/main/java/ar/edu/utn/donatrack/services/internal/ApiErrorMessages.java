package ar.edu.utn.donatrack.services.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
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
            if (response.getStatusCode().value() == 429)
                return demasiadasSolicitudes(segundosParaReintentar(response));
            try {
                var json = mapper.readTree(response.getResponseBodyAsString());
                String message = primerTexto(json, "message", "mensaje", "detail", "title");
                if (!message.isBlank() && !message.matches("(?is).*\\b(?:java|sql|jdbc|exception|stacktrace|localhost|http|endpoint)\\b.*")) return message;
            } catch (Exception ignored) { /* No exponemos HTML, trazas ni cuerpos sin interpretar. */ }
            return "No se pudo completar la solicitud. Revisá los datos ingresados.";
        }
        return "No se pudo comunicar con el servicio. No se confirmó la operación; verificá el resultado antes de reintentar.";
    }

    /** Texto para un 429 del rate limiting del backend (E6, req. 4). */
    public static String demasiadasSolicitudes(long segundos) {
        return "Hiciste demasiados intentos seguidos. Esperá " + segundos + (segundos == 1 ? " segundo" : " segundos")
                + " y volvé a intentar.";
    }

    /**
     * Cuánto esperar ante un 429: el header Retry-After y, si no viene, el campo reintentarEnSegundos del body.
     * Si no hay ninguno, un minuto (el período de la mayoría de los límites del backend).
     */
    public long segundosParaReintentar(RestClientResponseException response) {
        String retryAfter = response.getResponseHeaders() == null ? null
                : response.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        try {
            if (retryAfter != null) return Math.max(1, Long.parseLong(retryAfter.trim()));
        } catch (NumberFormatException ignored) { /* Retry-After también puede ser una fecha: se usa el body. */ }
        try {
            long segundos = mapper.readTree(response.getResponseBodyAsString()).path("reintentarEnSegundos").asLong(0);
            if (segundos > 0) return segundos;
        } catch (Exception ignored) { /* Cuerpo vacío o no JSON. */ }
        return 60;
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

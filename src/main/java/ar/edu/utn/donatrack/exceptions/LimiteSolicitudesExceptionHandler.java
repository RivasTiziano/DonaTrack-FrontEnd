package ar.edu.utn.donatrack.exceptions;

import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

/**
 * El backend respondió 429 (rate limiting, E6 req. 4) en una página que no lo maneja por su cuenta.
 * En lugar de un error genérico, se muestra cuánto esperar y se le devuelve al navegador el mismo
 * 429 con Retry-After. El login y el registro lo resuelven en su propio formulario (ApiErrorMessages).
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class LimiteSolicitudesExceptionHandler {
    private final ApiErrorMessages errors;

    public LimiteSolicitudesExceptionHandler(ApiErrorMessages errors) {
        this.errors = errors;
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public String demasiadasSolicitudes(HttpClientErrorException.TooManyRequests exception,
                                        HttpServletResponse response, Model model) {
        long segundos = errors.segundosParaReintentar(exception);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(segundos));
        model.addAttribute("segundos", segundos);
        model.addAttribute("message", ApiErrorMessages.demasiadasSolicitudes(segundos));
        return "error/429";
    }
}

package ar.edu.utn.donatrack.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

/** Sesión vencida o inexistente: se invalida y se vuelve al login con aviso. Se aplica antes que los demás handlers. */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SesionExceptionHandler {
    @ExceptionHandler({SesionExpiradaException.class, SesionRequeridaException.class,
            HttpClientErrorException.Unauthorized.class})
    public String sesionVencida(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login?expirada";
    }
}

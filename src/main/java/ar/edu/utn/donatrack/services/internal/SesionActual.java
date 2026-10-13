package ar.edu.utn.donatrack.services.internal;

import ar.edu.utn.donatrack.exceptions.SesionRequeridaException;
import ar.edu.utn.donatrack.models.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Optional;

/** Acceso a la sesión del usuario actual. Sin request activo (por ejemplo, en tests unitarios) no hay sesión y no se lanza error. */
@Component
public class SesionActual {
    public static final String ATRIBUTO = "SESION_USUARIO";

    public Optional<SesionUsuario> obtener() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return Optional.empty();
        HttpSession session = attributes.getRequest().getSession(false);
        if (session == null) return Optional.empty();
        return Optional.ofNullable(session.getAttribute(ATRIBUTO)).filter(SesionUsuario.class::isInstance)
                .map(SesionUsuario.class::cast);
    }

    /** Guarda la sesión. Si ya existía una sesión, cambia su id para evitar fijación de sesión. */
    public void guardar(HttpServletRequest request, SesionUsuario sesion) {
        if (request.getSession(false) != null) request.changeSessionId();
        request.getSession(true).setAttribute(ATRIBUTO, sesion);
    }

    public void cerrar(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public Long referenciaId() {
        return obtener().map(SesionUsuario::getReferenciaId).orElseThrow(SesionRequeridaException::new);
    }
}

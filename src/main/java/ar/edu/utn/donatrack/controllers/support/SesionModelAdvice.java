package ar.edu.utn.donatrack.controllers.support;

import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Datos de sesión disponibles en todas las vistas: el usuario y, en áreas personales, el contador de notificaciones. */
@ControllerAdvice
public class SesionModelAdvice {
    private final SesionActual sesiones;
    private final NotificacionesApiService notificaciones;

    public SesionModelAdvice(SesionActual sesiones, NotificacionesApiService notificaciones) {
        this.sesiones = sesiones;
        this.notificaciones = notificaciones;
    }

    @ModelAttribute("sesion")
    public SesionUsuario sesion() {
        return sesiones.obtener().orElse(null);
    }

    /** Solo se consulta en donante y entidad, para no llamar al servicio de notificaciones en cada página pública. */
    @ModelAttribute("notificacionesNoLeidas")
    public Long notificacionesNoLeidas(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        if (!(ruta.startsWith("/donante/") || ruta.startsWith("/entidad/"))) return null;
        if (sesiones.obtener().isEmpty()) return null;
        try {
            return notificaciones.contarNoLeidas();
        } catch (RuntimeException exception) {
            // Si el servicio no responde, el badge se oculta; no rompe la página.
            return null;
        }
    }
}

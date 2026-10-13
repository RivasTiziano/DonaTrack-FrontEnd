package ar.edu.utn.donatrack.config;

import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;

/**
 * Control de acceso por rol para las áreas personales.
 * Sin sesión, redirige al login. Con sesión de otro rol, responde 403.
 * La regla se aplica por prefijo de ruta; el resto de la web pública queda fuera de este interceptor.
 */
@Component
public class AccesoInterceptor implements HandlerInterceptor {
    private final SesionActual sesiones;

    public AccesoInterceptor(SesionActual sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        var sesion = sesiones.obtener();
        if (sesion.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!permitido(ruta, sesion.get())) {
            // La página de error usa este dato para ofrecer el enlace a su propio panel.
            request.setAttribute("panelPropio", sesion.get().panel());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    // Por defecto, cualquier ruta que llegue acá (incluida /admin sin barra final) exige ADMIN.
    private boolean permitido(String ruta, SesionUsuario sesion) {
        if (enArea(ruta, "/donante")) return sesion.esDonante();
        if (enArea(ruta, "/entidad")) return sesion.esBeneficiaria();
        return sesion.esAdmin();
    }

    private boolean enArea(String ruta, String area) {
        return ruta.equals(area) || ruta.startsWith(area + "/");
    }
}

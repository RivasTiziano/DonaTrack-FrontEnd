package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import org.springframework.mock.web.MockHttpSession;
import java.util.List;

/** Sesiones de prueba con los permisos reales de cada rol (plan, sección 1). Los tokens son ficticios. */
public final class TestSesiones {
    private TestSesiones() {}

    private static final List<String> PERMISOS_ADMIN = List.of("GESTIONAR_DONANTES", "REGISTRAR_DONACIONES",
            "MARCAR_DONACION_VENCIDA", "ASIGNAR_DONACIONES", "GESTIONAR_CAMIONES", "SEGUIR_ENTREGAS", "VER_RANKINGS",
            "IMPORTAR_DONANTES_CSV", "REVISAR_ENTREGAS_FALLIDAS", "GESTIONAR_MISIONES", "GESTIONAR_USUARIOS",
            "VER_ENTIDADES_BENEFICIARIAS");
    private static final List<String> PERMISOS_DONANTE = List.of("VER_DONACIONES_PROPIAS", "VER_ENTIDADES_BENEFICIARIAS",
            "VER_INCENTIVOS_PROPIOS", "SEGUIR_ENTREGAS", "VER_NOTIFICACIONES_PROPIAS");
    private static final List<String> PERMISOS_ENTIDAD = List.of("GESTIONAR_NECESIDADES", "VER_DONACIONES_ASIGNADAS",
            "CONFIRMAR_RECEPCION", "SEGUIR_ENTREGAS", "VER_NOTIFICACIONES_PROPIAS");

    /** Sesión de un ADMIN (referenciaId nulo). */
    public static MockHttpSession admin() {
        return sesion("ADMIN", null, "admin@example.com", "admin@example.com", PERMISOS_ADMIN);
    }

    /** Sesión de un donante humano con el id indicado. */
    public static MockHttpSession donante(long id) {
        return sesion("DONANTE_HUMANO", id, "donante@example.com", "Ana Prueba", PERMISOS_DONANTE);
    }

    /** Sesión de una entidad beneficiaria con el id indicado. */
    public static MockHttpSession entidad(long id) {
        return sesion("BENEFICIARIA", id, "entidad@example.com", "Entidad de prueba", PERMISOS_ENTIDAD);
    }

    private static MockHttpSession sesion(String rol, Long referenciaId, String email, String nombre, List<String> permisos) {
        var token = new AuthDtos.Token("access-de-prueba", "refresh-de-prueba", 900, rol, permisos, referenciaId);
        var session = new MockHttpSession();
        session.setAttribute(SesionActual.ATRIBUTO, SesionUsuario.desde(email, token, nombre));
        return session;
    }
}

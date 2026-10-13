package ar.edu.utn.donatrack.models;

import ar.edu.utn.donatrack.dto.AuthDtos;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Sesión del usuario autenticado. Guarda los tokens que emitió el servicio de autenticación.
 * Los datos de identidad son inmutables; solo cambian los tokens al refrescarse.
 * toString no incluye tokens.
 */
public final class SesionUsuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String email;
    private final String rol;
    private final List<String> permisos;
    private final Long referenciaId;
    private final String nombreVisible;
    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile Instant expiraEn;

    private SesionUsuario(String email, String rol, List<String> permisos, Long referenciaId, String nombreVisible,
                          String accessToken, String refreshToken, Instant expiraEn) {
        this.email = email;
        this.rol = rol;
        this.permisos = permisos == null ? List.of() : List.copyOf(permisos);
        this.referenciaId = referenciaId;
        this.nombreVisible = nombreVisible;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiraEn = expiraEn;
    }

    /** Crea la sesión a partir de la respuesta de login o refresh. */
    public static SesionUsuario desde(String email, AuthDtos.Token t, String nombreVisible) {
        return new SesionUsuario(email, t.rol(), t.permisos(), t.referenciaId(), nombreVisible,
                t.accessToken(), t.refreshToken(), Instant.now().plusSeconds(t.expiraEnSegundos()));
    }

    /** Reemplaza los tokens por los que devolvió un refresh. */
    public void renovar(AuthDtos.Token t) {
        this.accessToken = t.accessToken();
        this.refreshToken = t.refreshToken();
        this.expiraEn = Instant.now().plusSeconds(t.expiraEnSegundos());
    }

    public boolean tiene(String permiso) { return permisos.contains(permiso); }
    public boolean esAdmin() { return "ADMIN".equals(rol); }
    public boolean esDonante() { return "DONANTE_HUMANO".equals(rol) || "DONANTE_JURIDICO".equals(rol); }
    public boolean esBeneficiaria() { return "BENEFICIARIA".equals(rol); }

    /** Segmento de la API de donantes: "humanos" o "juridicos". Null si el rol no es de donante. */
    public String tipoDonante() {
        if ("DONANTE_HUMANO".equals(rol)) return "humanos";
        if ("DONANTE_JURIDICO".equals(rol)) return "juridicos";
        return null;
    }

    /** Ruta del panel que corresponde al rol. */
    public String panel() {
        if (esAdmin()) return "/admin/dashboard";
        if (esDonante()) return "/donante/dashboard";
        if (esBeneficiaria()) return "/entidad/dashboard";
        return "/";
    }

    /** Vence dentro de 60 segundos o antes: conviene refrescar antes de llamar a la API. */
    public boolean vencePronto() { return Instant.now().isAfter(expiraEn.minusSeconds(60)); }

    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public List<String> getPermisos() { return permisos; }
    public Long getReferenciaId() { return referenciaId; }
    public String getNombreVisible() { return nombreVisible; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getExpiraEn() { return expiraEn; }

    @Override
    public String toString() {
        return "SesionUsuario[rol=" + rol + ", referenciaId=" + referenciaId + ", expiraEn=" + expiraEn + ", tokens=***]";
    }
}

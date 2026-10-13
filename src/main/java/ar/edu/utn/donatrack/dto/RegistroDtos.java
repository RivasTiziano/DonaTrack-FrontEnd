package ar.edu.utn.donatrack.dto;

/**
 * Cuerpos del registro público (POST /publico/registro/...) y su respuesta 201, sin token.
 * La contraseña nunca aparece en toString.
 */
public final class RegistroDtos {
    private RegistroDtos() {}

    public record RegistroHumano(DonanteRequests.Humano perfil, String email, String contrasenia) {
        @Override public String toString() { return "RegistroHumano[perfil=" + perfil + ", email=" + email + ", contrasenia=********]"; }
    }

    public record RegistroJuridico(DonanteRequests.Juridico perfil, String email, String contrasenia) {
        @Override public String toString() { return "RegistroJuridico[perfil=" + perfil + ", email=" + email + ", contrasenia=********]"; }
    }

    public record RegistroEntidad(DonacionesDetalles.CrearBeneficiario perfil, String email, String contrasenia) {
        @Override public String toString() { return "RegistroEntidad[perfil=" + perfil + ", email=" + email + ", contrasenia=********]"; }
    }

    /** Respuesta 201 del backend: id del perfil creado, rol de la cuenta y email. */
    public record Registrado(Long id, String rol, String email) {}
}

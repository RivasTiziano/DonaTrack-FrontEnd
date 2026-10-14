package ar.edu.utn.donatrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contratos del envío de acceso en Donaciones. El alta de donante o entidad trae solo el estado (enum),
 * por eso el mensaje de cada estado vive en el front.
 */
public final class AccesoDtos {
    private AccesoDtos() {}

    /** Respuesta de POST .../acceso: estado (ENVIADO, SIN_EMAIL, YA_TIENE_CUENTA, EMAIL_DE_OTRA_CUENTA, ERROR) y mensaje del back. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(String estado, String mensaje) {}

    /** Acceso que devuelve el alta (POST de donantes o entidades): solo el estado, o null. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alta(String acceso) {}

    private static final String ERROR = "No se pudo enviar el acceso en este momento. Intentá de nuevo más tarde";

    /** Mensaje para mostrar según el estado. Null o desconocido se trata como ERROR. */
    public static String mensaje(String estado) {
        if (estado == null) return ERROR;
        return switch (estado) {
            case "ENVIADO" -> "Se envió el mail de bienvenida para que elija su contraseña";
            case "SIN_EMAIL" -> "El perfil no tiene un email de contacto: cargalo y volvé a enviar el acceso";
            case "YA_TIENE_CUENTA" -> "La persona ya eligió su contraseña y puede ingresar";
            case "EMAIL_DE_OTRA_CUENTA" -> "El email del perfil ya es de la cuenta de otro perfil";
            default -> ERROR;
        };
    }
}

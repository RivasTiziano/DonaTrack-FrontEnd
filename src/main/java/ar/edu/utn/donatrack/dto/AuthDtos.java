package ar.edu.utn.donatrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Contratos del microservicio de autenticación (puerto 8084). Los toString ocultan credenciales y tokens. */
public final class AuthDtos {
    private AuthDtos() {}

    public record Login(String email, String password) {
        @Override public String toString() { return "Login[email=" + email + ", password=***]"; }
    }

    public record Refresh(String refreshToken) {
        @Override public String toString() { return "Refresh[refreshToken=***]"; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Token(String accessToken, String refreshToken, long expiraEnSegundos,
                        String rol, List<String> permisos, Long referenciaId) {
        @Override public String toString() {
            return "Token[rol=" + rol + ", referenciaId=" + referenciaId + ", expiraEnSegundos=" + expiraEnSegundos + ", tokens=***]";
        }
    }

    public record CrearUsuario(String email, String password, String rol, Long referenciaId) {
        @Override public String toString() {
            return "CrearUsuario[email=" + email + ", rol=" + rol + ", referenciaId=" + referenciaId + ", password=***]";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usuario(Long id, String email, String rol, Long referenciaId) {}
}

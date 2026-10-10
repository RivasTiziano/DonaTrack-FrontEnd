package ar.edu.utn.donatrack.dto.notificacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Contrato actual del servicio, sin dependencias del modelo JPA del backend. */
public final class NotificacionDtos {
    private NotificacionDtos() {}
    public enum Medio { EMAIL, SMS, WHATSAPP }
    public enum Estado { PENDIENTE, COMPLETADA, FALLIDA }
    public record Destinatario(String nombre, String email, String telefono) {}
    public record Mensaje(String asunto, String cuerpo) {}
    public record Enviar(Destinatario destinatario, Mensaje mensaje, Medio medioContacto) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(String retorno, Estado datos, String mensajeError) {}
}

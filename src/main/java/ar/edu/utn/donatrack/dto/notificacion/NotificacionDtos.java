package ar.edu.utn.donatrack.dto.notificacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

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

    /** Notificación del historial de una persona (GET /notificaciones). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Recibida(Long id, String asunto, String cuerpo, Medio medio, LocalDateTime fecha,
                           boolean leida, LocalDateTime fechaLectura, String referencia) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespuestaLista(String retorno, List<Recibida> datos, String mensajeError) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cantidad(long noLeidas) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespuestaCantidad(String retorno, Cantidad datos, String mensajeError) {}
}

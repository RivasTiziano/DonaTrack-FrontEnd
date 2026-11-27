package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.forms.NotificacionForm;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
@Component
public class NotificacionesMapper {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Notificación lista para mostrar: la fecha ya viene formateada y el canal con nombre legible. */
    public record Vista(Long id, String asunto, String cuerpo, String canal, String fecha, boolean leida) {}

    public Enviar solicitud(NotificacionForm form) {
        return new Enviar(new Destinatario(form.nombre(),form.medioContacto()==Medio.EMAIL?form.email():null,
                form.medioContacto()==Medio.EMAIL?null:form.telefono()),
                new Mensaje(form.asunto(),form.cuerpo()),form.medioContacto());
    }

    public Vista vista(Recibida n) {
        String canalTexto = canal(n.medio());
        if (n.estado() == Estado.COMPLETADA) {
            canalTexto = canalTexto + " · Enviada";
        } else if (n.estado() == Estado.FALLIDA) {
            canalTexto = canalTexto + " · En bandeja web (envío externo no disponible)";
        }
        return new Vista(n.id(), n.asunto(), n.cuerpo(), canalTexto,
                n.fecha() == null ? "" : FECHA.format(n.fecha()), n.leida());
    }

    private String canal(Medio medio) {
        if (medio == null) return "";
        return switch (medio) { case EMAIL -> "Email"; case SMS -> "SMS"; case WHATSAPP -> "WhatsApp"; };
    }
}

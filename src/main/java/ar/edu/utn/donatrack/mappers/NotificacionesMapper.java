package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.forms.NotificacionForm;
import org.springframework.stereotype.Component;
@Component
public class NotificacionesMapper {
    public Enviar solicitud(NotificacionForm form) {
        return new Enviar(new Destinatario(form.nombre(),form.medioContacto()==Medio.EMAIL?form.email():null,
                form.medioContacto()==Medio.EMAIL?null:form.telefono()),
                new Mensaje(form.asunto(),form.cuerpo()),form.medioContacto());
    }
}

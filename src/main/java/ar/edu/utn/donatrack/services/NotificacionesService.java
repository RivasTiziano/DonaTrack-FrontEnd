package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.forms.NotificacionForm;
import ar.edu.utn.donatrack.mappers.NotificacionesMapper;
import ar.edu.utn.donatrack.validators.NotificacionesValidator;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class NotificacionesService {
    private final NotificacionesApiService api;
    private final NotificacionesMapper mapper;
    private final NotificacionesValidator validator;
    public NotificacionesService(NotificacionesApiService api,NotificacionesMapper mapper,NotificacionesValidator validator) {
        this.api=api; this.mapper=mapper; this.validator=validator;
    }
    public record Resultado(boolean exitoso,String mensaje) {}
    /** Notificaciones de una persona y cuántas siguen sin leer. */
    public record Historial(List<NotificacionesMapper.Vista> notificaciones, long noLeidas) {}
    public String comprobar() {
        return api.disponible()
                ? "El servicio responde al health check. Esto no verifica Gmail, Twilio ni RabbitMQ."
                : "El servicio respondió un health check inesperado.";
    }
    public Resultado enviar(NotificacionForm form) {
        var errors=new BeanPropertyBindingResult(form,"form");
        validator.validate(form,errors);
        if(errors.hasErrors()) throw new FormularioInvalidoException(errors.getAllErrors().get(0).getDefaultMessage());
        var response=api.enviar(mapper.solicitud(form));
        if(response==null || response.retorno()==null || response.datos()==null)
            throw new IllegalStateException("Respuesta de envío inválida.");
        if(!"success".equals(response.retorno()) || response.datos()==Estado.FALLIDA)
            return new Resultado(false,response.mensajeError()==null?"El servicio informó que el envío falló.":response.mensajeError());
        return new Resultado(true,response.datos()==Estado.PENDIENTE
                ? "Solicitud aceptada: PENDIENTE. El proveedor todavía puede fallar; no es confirmación de entrega."
                : "El servicio informó COMPLETADA. No significa que la persona haya leído el mensaje.");
    }
    public Historial historial(String email, String telefono) {
        exigirContacto(email, telefono);
        var notificaciones = api.listar(email, telefono, false).stream().map(mapper::vista).toList();
        return new Historial(notificaciones, notificaciones.stream().filter(n -> !n.leida()).count());
    }
    public void marcarLeida(Long id, String email, String telefono) {
        exigirContacto(email, telefono);
        api.marcarLeida(id, email, telefono);
    }
    public void marcarTodasLeidas(String email, String telefono) {
        exigirContacto(email, telefono);
        api.marcarTodasLeidas(email, telefono);
    }
    private void exigirContacto(String email, String telefono) {
        if ((email == null || email.isBlank()) && (telefono == null || telefono.isBlank()))
            throw new FormularioInvalidoException("Indicá un email o un teléfono para consultar las notificaciones.");
    }
}

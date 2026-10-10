package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.forms.NotificacionForm;
import ar.edu.utn.donatrack.mappers.NotificacionesMapper;
import ar.edu.utn.donatrack.validators.NotificacionesValidator;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.stereotype.Service;
@Service
public class NotificacionesService {
    private final NotificacionesApiService api;
    private final NotificacionesMapper mapper;
    private final NotificacionesValidator validator;
    public NotificacionesService(NotificacionesApiService api,NotificacionesMapper mapper,NotificacionesValidator validator) {
        this.api=api; this.mapper=mapper; this.validator=validator;
    }
    public record Resultado(boolean exitoso,String mensaje) {}
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
}

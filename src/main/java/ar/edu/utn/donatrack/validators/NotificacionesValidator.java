package ar.edu.utn.donatrack.validators;

import ar.edu.utn.donatrack.forms.NotificacionForm;
import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.Medio;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** Validación de entrada del formulario, no de eventos ni reglas de negocio. */
@Component
public class NotificacionesValidator {
    public void validate(NotificacionForm form, Errors errors) {
        if (form.medioContacto() == null) errors.rejectValue("medioContacto", "required", "Seleccioná un canal.");
        required(form.nombre(), "nombre", 150, errors);
        required(form.asunto(), "asunto", 255, errors);
        required(form.cuerpo(), "cuerpo", 10000, errors);
        if (form.medioContacto() == Medio.EMAIL &&
                (form.email() == null || !form.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")))
            errors.rejectValue("email", "format", "Ingresá un email válido para el envío.");
        if (form.medioContacto() != null && form.medioContacto() != Medio.EMAIL &&
                (form.telefono() == null || !form.telefono().matches("^\\+[1-9][0-9]{7,14}$")))
            errors.rejectValue("telefono", "format", "Usá un teléfono internacional con + y código de país, sin espacios.");
    }
    private void required(String value, String field, int max, Errors errors) {
        if (value == null || value.isBlank()) errors.rejectValue(field, "required", "Este campo es obligatorio.");
        else if (value.length() > max) errors.rejectValue(field, "length", "Supera el máximo de " + max + " caracteres.");
    }
}

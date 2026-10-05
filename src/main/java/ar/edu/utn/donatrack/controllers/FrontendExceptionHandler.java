package ar.edu.utn.donatrack.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice(assignableTypes = {AdminOperacionesController.class, AdminDonantesController.class, DonanteFormularioController.class})
public class FrontendExceptionHandler {
    @ExceptionHandler({BindException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String malformed(Model model) {
        model.addAttribute("message", "La solicitud contiene campos faltantes o formatos inválidos. Volvé al formulario y revisá los datos.");
        return "api-error";
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String tooLarge(Model model) {
        model.addAttribute("message", "El archivo supera el límite de 20 MB. Seleccioná uno más pequeño.");
        return "api-error";
    }
}

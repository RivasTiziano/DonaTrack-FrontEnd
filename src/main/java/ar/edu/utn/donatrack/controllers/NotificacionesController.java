package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.forms.NotificacionForm;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.validators.NotificacionFormValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
public class NotificacionesController {
    private static final String TOKEN = "notificacionesEnvioToken";
    private final NotificacionesApiService api;
    private final NotificacionFormValidator validator;
    private final ApiErrorMessages errors;
    private final boolean enabled;
    public NotificacionesController(NotificacionesApiService api, NotificacionFormValidator validator,
                                     ApiErrorMessages errors,
                                     @Value("${app.notificaciones.envio-manual-habilitado:false}") boolean enabled) {
        this.api = api; this.validator = validator; this.errors = errors; this.enabled = enabled;
    }
    @GetMapping("/admin/dashboard/notificaciones")
    public String admin(Model model, HttpSession session) {
        return prepare(model, session, "admin");
    }
    @GetMapping("/donante/dashboard/notificaciones")
    public String donante(Model model, HttpSession session) {
        return prepare(model, session, "donante");
    }
    @GetMapping("/entidad/dashboard/notificaciones")
    public String entidad(Model model, HttpSession session) {
        return prepare(model, session, "entidad");
    }
    private String prepare(Model model, HttpSession session, String audience) {
        model.addAttribute("audience", audience);
        model.addAttribute("manualEnabled", enabled);
        if (audience.equals("admin") && enabled) {
            if (session.getAttribute(TOKEN) == null) session.setAttribute(TOKEN, UUID.randomUUID().toString());
            model.addAttribute("sendToken", session.getAttribute(TOKEN));
            if (!model.containsAttribute("form")) model.addAttribute("form", new NotificacionForm(Medio.EMAIL, "", "", "", "", ""));
        }
        // No se consulta ningún historial por email/ID ingresado ni por el primer usuario de la API.
        return "notificaciones";
    }
    @PostMapping("/admin/dashboard/notificaciones/comprobar")
    public String comprobar(RedirectAttributes flash) {
        try {
            flash.addFlashAttribute("serviceStatus", api.disponible()
                    ? "El servicio responde al health check. Esto no verifica Gmail, Twilio ni RabbitMQ."
                    : "El servicio respondió un health check inesperado.");
        } catch (RestClientException exception) { flash.addFlashAttribute("apiError", errors.describe(exception)); }
        return "redirect:/admin/dashboard/notificaciones";
    }
    @PostMapping("/admin/dashboard/notificaciones/enviar")
    public String enviar(@ModelAttribute("form") NotificacionForm form, BindingResult binding,
                         @RequestParam String sendToken, HttpSession session, Model model, RedirectAttributes flash) {
        if (!enabled) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Envío manual deshabilitado.");
        validator.validate(form, binding);
        if (binding.hasErrors()) return prepare(model, session, "admin");
        // Evita CSRF de este formulario y reutilizar el mismo envío. No sustituye permisos ni idempotencia del backend.
        synchronized (session) {
            if (!sendToken.equals(session.getAttribute(TOKEN)))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Formulario vencido o ya enviado. Recargá la página.");
            session.removeAttribute(TOKEN);
        }
        var request = new Enviar(new Destinatario(form.nombre(),
                form.medioContacto() == Medio.EMAIL ? form.email() : null,
                form.medioContacto() == Medio.EMAIL ? null : form.telefono()),
                new Mensaje(form.asunto(), form.cuerpo()), form.medioContacto());
        try {
            var response = api.enviar(request);
            if (!"success".equals(response.retorno()) || response.datos() == Estado.FALLIDA) {
                model.addAttribute("apiError", response.mensajeError() == null ? "El servicio informó que el envío falló." : response.mensajeError());
                return prepare(model, session, "admin");
            }
            flash.addFlashAttribute("sendResult", response.datos() == Estado.PENDIENTE
                    ? "Solicitud aceptada: PENDIENTE. El proveedor todavía puede fallar; no es confirmación de entrega."
                    : "El servicio informó COMPLETADA. No significa que la persona haya leído el mensaje.");
            return "redirect:/admin/dashboard/notificaciones";
        } catch (RestClientException exception) {
            model.addAttribute("apiError", errors.describe(exception));
        } catch (IllegalStateException exception) {
            model.addAttribute("apiError", "Respuesta de envío inválida. Consultá el resultado en el servicio antes de reintentar.");
        }
        return prepare(model, session, "admin");
    }
}

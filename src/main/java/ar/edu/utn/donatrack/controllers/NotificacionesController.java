package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.forms.NotificacionForm;
import ar.edu.utn.donatrack.services.NotificacionesService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.validators.NotificacionesValidator;
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
    private final NotificacionesService service;
    private final NotificacionesValidator validator;
    private final ApiErrorMessages errors;
    private final boolean enabled;
    public NotificacionesController(NotificacionesService service, NotificacionesValidator validator,
                                     ApiErrorMessages errors,
                                     @Value("${app.notificaciones.envio-manual-habilitado:false}") boolean enabled) {
        this.service = service; this.validator = validator; this.errors = errors; this.enabled = enabled;
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
            flash.addFlashAttribute("serviceStatus", service.comprobar());
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
        try {
            var response = service.enviar(form);
            if (!response.exitoso()) {
                model.addAttribute("apiError", response.mensaje());
                return prepare(model, session, "admin");
            }
            flash.addFlashAttribute("sendResult", response.mensaje());
            return "redirect:/admin/dashboard/notificaciones";
        } catch (RestClientException exception) {
            model.addAttribute("apiError", errors.describe(exception));
        } catch (IllegalStateException exception) {
            model.addAttribute("apiError", "Respuesta de envío inválida. Consultá el resultado en el servicio antes de reintentar.");
        }
        return prepare(model, session, "admin");
    }
}

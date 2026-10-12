package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
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
    private final boolean historialPorContacto;
    public NotificacionesController(NotificacionesService service, NotificacionesValidator validator,
                                     ApiErrorMessages errors,
                                     @Value("${app.notificaciones.envio-manual-habilitado:false}") boolean enabled,
                                     @Value("${app.notificaciones.historial-por-contacto-habilitado:false}") boolean historialPorContacto) {
        this.service = service; this.validator = validator; this.errors = errors; this.enabled = enabled;
        this.historialPorContacto = historialPorContacto;
    }
    @GetMapping("/admin/dashboard/notificaciones")
    public String admin(Model model, HttpSession session) {
        return prepare(model, session, "admin");
    }
    @GetMapping("/donante/dashboard/notificaciones")
    public String donante(@RequestParam(required = false) String email, @RequestParam(required = false) String telefono,
                          Model model, HttpSession session) {
        cargarHistorial(model, "donante", email, telefono);
        return prepare(model, session, "donante");
    }
    @GetMapping("/entidad/dashboard/notificaciones")
    public String entidad(@RequestParam(required = false) String email, @RequestParam(required = false) String telefono,
                          Model model, HttpSession session) {
        cargarHistorial(model, "entidad", email, telefono);
        return prepare(model, session, "entidad");
    }

    /**
     * Historial por contacto: HERRAMIENTA DE DESARROLLO LOCAL, apagada por defecto. Quien conozca un email o
     * teléfono puede leer sus avisos, así que no sustituye la identidad. Cuando exista la sesión con token,
     * el contacto debe salir de la identidad autenticada y este parámetro desaparece.
     */
    private void cargarHistorial(Model model, String audience, String email, String telefono) {
        if (!historialPorContacto) return;
        model.addAttribute("historialHabilitado", true);
        model.addAttribute("audienceRuta", audience);
        model.addAttribute("email", email == null ? "" : email.trim());
        model.addAttribute("telefono", telefono == null ? "" : telefono.trim());
        if (vacio(email) && vacio(telefono)) return;
        try {
            var historial = service.historial(email, telefono);
            model.addAttribute("notificaciones", historial.notificaciones());
            model.addAttribute("noLeidas", historial.noLeidas());
        } catch (RestClientException | IllegalStateException exception) {
            model.addAttribute("apiError", exception instanceof RestClientException
                    ? errors.describe(exception) : "El servicio devolvió un historial inválido.");
        }
    }
    @PostMapping("/donante/dashboard/notificaciones/{id}/leida")
    public String marcarLeidaDonante(@PathVariable Long id, @RequestParam(required = false) String email,
                                     @RequestParam(required = false) String telefono, RedirectAttributes flash) {
        return marcarLeida("donante", id, email, telefono, flash);
    }
    @PostMapping("/entidad/dashboard/notificaciones/{id}/leida")
    public String marcarLeidaEntidad(@PathVariable Long id, @RequestParam(required = false) String email,
                                     @RequestParam(required = false) String telefono, RedirectAttributes flash) {
        return marcarLeida("entidad", id, email, telefono, flash);
    }
    @PostMapping("/donante/dashboard/notificaciones/leidas")
    public String marcarTodasDonante(@RequestParam(required = false) String email,
                                     @RequestParam(required = false) String telefono, RedirectAttributes flash) {
        return marcarLeida("donante", null, email, telefono, flash);
    }
    @PostMapping("/entidad/dashboard/notificaciones/leidas")
    public String marcarTodasEntidad(@RequestParam(required = false) String email,
                                     @RequestParam(required = false) String telefono, RedirectAttributes flash) {
        return marcarLeida("entidad", null, email, telefono, flash);
    }
    /** Con id marca una; sin id marca todas. */
    private String marcarLeida(String audience, Long id, String email, String telefono, RedirectAttributes flash) {
        if (!historialPorContacto) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Historial por contacto deshabilitado.");
        try {
            if (id == null) service.marcarTodasLeidas(email, telefono);
            else service.marcarLeida(id, email, telefono);
        } catch (RestClientException exception) {
            flash.addFlashAttribute("apiError", errors.describe(exception));
        } catch (FormularioInvalidoException exception) {
            flash.addFlashAttribute("apiError", exception.getMessage());
        }
        if (!vacio(email)) flash.addAttribute("email", email.trim());
        if (!vacio(telefono)) flash.addAttribute("telefono", telefono.trim());
        return "redirect:/" + audience + "/dashboard/notificaciones";
    }
    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
    private String prepare(Model model, HttpSession session, String audience) {
        model.addAttribute("audience", audience);
        model.addAttribute("manualEnabled", enabled);
        if (audience.equals("admin") && enabled) {
            if (session.getAttribute(TOKEN) == null) session.setAttribute(TOKEN, UUID.randomUUID().toString());
            model.addAttribute("sendToken", session.getAttribute(TOKEN));
            if (!model.containsAttribute("form")) model.addAttribute("form", new NotificacionForm(Medio.EMAIL, "", "", "", "", ""));
        }
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

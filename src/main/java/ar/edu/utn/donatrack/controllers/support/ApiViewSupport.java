package ar.edu.utn.donatrack.controllers.support;

import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.function.Supplier;

/** Manejo de presentación compartido; no contiene llamadas ni reglas del backend. */
@Component
public class ApiViewSupport {
    private final ApiErrorMessages errors;
    public ApiViewSupport(ApiErrorMessages errors) { this.errors = errors; }

    public void defaults(Model model) {
        model.addAttribute("user", java.util.Map.of("name", "Administración (sin autenticación)"));
        for (String name : java.util.List.of("donaciones","bienes","categorias","necesidades","beneficiarios",
                "camiones","ranking","algoritmos","asignaciones","entregas","rutas"))
            model.addAttribute(name, java.util.List.of());
    }

    public void load(Model model, Runnable operation) {
        try { operation.run(); }
        catch (RestClientException | IllegalStateException e) { model.addAttribute("apiError", errors.describe(e)); }
    }

    public String adminPage(Model model, String title, String section, String activeTab) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("section", section);
        model.addAttribute("activeTab", activeTab);
        return "admin-api";
    }

    public String invalid(RedirectAttributes flash, String path) {
        flash.addFlashAttribute("apiError", "Revisá el formato de los números y las fechas del formulario.");
        return "redirect:/admin/dashboard/" + path;
    }

    // Carga de datos de la API para la vista, con manejo de errores y mensajes de éxito.
    public void load(Model model, String attribute, Supplier<?> operation) {
        try {
            Object value = operation.get();
            if (value == null) throw new IllegalStateException("Respuesta incompleta");
            model.addAttribute(attribute, value);
        } catch (RestClientException | IllegalStateException exception) {
            model.addAttribute("apiError", errors.describe(exception));
        }
    }

    // Ejecución de una operación de escritura en la API, con manejo de errores y mensajes de éxito.
    public String write(RedirectAttributes flash, String redirect, Runnable operation) {
        try {
            operation.run();
            flash.addFlashAttribute("success", "Operación confirmada por el servicio.");
        } catch (ar.edu.utn.donatrack.exceptions.FormularioInvalidoException exception) {
            flash.addFlashAttribute("apiError", exception.getMessage());
        } catch (RestClientException | IllegalStateException exception) {
            flash.addFlashAttribute("apiError", errors.describe(exception));
        }
        return "redirect:" + redirect;
    }

    // Configuración de la vista, con título y sección.
    public String page(Model model, String title, String section) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("section", section);
        return "admin-complementos";
    }
}

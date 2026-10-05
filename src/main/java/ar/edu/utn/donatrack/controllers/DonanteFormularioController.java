package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.DonanteRequests;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.forms.DonanteForm;
import ar.edu.utn.donatrack.services.DonantesApiService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard/donantes")
public class DonanteFormularioController {
    private final DonantesApiService api;
    private final ApiErrorMessages errors;
    public DonanteFormularioController(DonantesApiService api, ApiErrorMessages errors) {
        this.api = api; this.errors = errors;
    }
    @GetMapping("/nuevo/{tipo:humanos|juridicos}")
    public String nuevo(@PathVariable String tipo, Model model) {
        model.addAttribute("tipo", tipo);
        return "admin-donante-form";
    }
    @PostMapping("/nuevo/{tipo:humanos|juridicos}")
    public String crear(@PathVariable String tipo, @ModelAttribute("form") DonanteForm form,
                        BindingResult binding, Model model, RedirectAttributes flash) {
        model.addAttribute("tipo", tipo);
        if (binding.hasErrors()) {
            model.addAttribute("apiError", "Revisá las fechas y coordenadas del formulario.");
            return "admin-donante-form";
        }
        var direction = new DonanteRequests.Direccion(form.calle(), form.ciudad(), form.provincia(),
                form.numero(), form.latitud(), form.longitud());
        var email = new MedioDeContactoDto("EMAIL", form.email());
        var contacts = new ArrayList<MedioDeContactoDto>();
        contacts.add(email);
        if (form.telefono() != null && !form.telefono().isBlank()) contacts.add(new MedioDeContactoDto("SMS", form.telefono()));
        try {
            if (tipo.equals("humanos")) {
                api.crearHumano(new DonanteRequests.Humano(form.nombre(), form.apellido(), form.fechaNacimiento(),
                        form.numeroDocumento(), form.genero(), direction, contacts, email));
            } else {
                var representative = new DonanteRequests.Representante(null, form.representanteNombre(),
                        form.representanteApellido(), "DNI", form.representanteDocumento(),
                        form.representanteNacimiento(), form.representanteGenero());
                api.crearJuridico(new DonanteRequests.Juridico(form.numeroDocumento(), form.razonSocial(),
                        form.tipoEntidad(), form.rubro(), direction, contacts, email, List.of(representative)));
            }
            flash.addFlashAttribute("success", "Donante registrado. Esta operación no crea una cuenta de acceso.");
            return "redirect:/admin/dashboard/donantes";
        } catch (RestClientException exception) {
            model.addAttribute("apiError", errors.describe(exception));
            return "admin-donante-form";
        }
    }
    @PostMapping("/{tipo:humanos|juridicos}/{id}/eliminar")
    public String eliminar(@PathVariable String tipo, @PathVariable Long id, RedirectAttributes flash) {
        try {
            if (tipo.equals("humanos")) api.eliminarHumano(id); else api.eliminarJuridico(id);
            flash.addFlashAttribute("success", "Eliminación confirmada por la API.");
        } catch (RestClientException exception) { flash.addFlashAttribute("apiError", errors.describe(exception)); }
        return "redirect:/admin/dashboard/donantes";
    }
}

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard/donantes")
public class DonanteFormularioController {
    private final DonantesApiService api;
    private final ApiErrorMessages errors;

    public DonanteFormularioController(DonantesApiService api, ApiErrorMessages errors) {
        this.api = api;
        this.errors = errors;
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
        if (form.telefono() != null && !form.telefono().isBlank()) {
            contacts.add(new MedioDeContactoDto("SMS", form.telefono()));
        }

        // Procesar contactos adicionales dinámicos
        if (form.contactoTipo() != null && form.contactoValor() != null) {
            for (int i = 0; i < form.contactoTipo().size(); i++) {
                String t = form.contactoTipo().get(i);
                String v = (i < form.contactoValor().size()) ? form.contactoValor().get(i) : null;
                if (t != null && !t.isBlank() && v != null && !v.isBlank()) {
                    contacts.add(new MedioDeContactoDto(t, v));
                }
            }
        }

        MedioDeContactoDto defaultContact = email;
        if (form.contactoPredeterminadoTipo() != null && !form.contactoPredeterminadoTipo().isBlank()
                && form.contactoPredeterminadoValor() != null && !form.contactoPredeterminadoValor().isBlank()) {
            defaultContact = new MedioDeContactoDto(form.contactoPredeterminadoTipo(), form.contactoPredeterminadoValor());
        }

        try {
            if (tipo.equals("humanos")) {
                api.crearHumano(new DonanteRequests.Humano(form.nombre(), form.apellido(), form.fechaNacimiento(),
                        form.numeroDocumento(), form.genero(), direction, contacts, defaultContact));
            } else {
                var representatives = new ArrayList<DonanteRequests.Representante>();
                if (form.representanteNombre() != null && !form.representanteNombre().isBlank()) {
                    representatives.add(new DonanteRequests.Representante(null, form.representanteNombre(),
                            form.representanteApellido(), "DNI", form.representanteDocumento(),
                            form.representanteNacimiento() != null ? form.representanteNacimiento() : LocalDate.of(1985, 1, 1),
                            form.representanteGenero() != null ? form.representanteGenero() : "OTRO"));
                }
                if (form.repNombre() != null) {
                    for (int i = 0; i < form.repNombre().size(); i++) {
                        String rNom = form.repNombre().get(i);
                        if (rNom == null || rNom.isBlank()) continue;
                        String rApe = (form.repApellido() != null && i < form.repApellido().size()) ? form.repApellido().get(i) : "";
                        String rDoc = (form.repDocumento() != null && i < form.repDocumento().size()) ? form.repDocumento().get(i) : "";
                        LocalDate rNac = (form.repNacimiento() != null && i < form.repNacimiento().size() && form.repNacimiento().get(i) != null)
                                ? form.repNacimiento().get(i) : LocalDate.of(1985, 1, 1);
                        String rGen = (form.repGenero() != null && i < form.repGenero().size()) ? form.repGenero().get(i) : "MASCULINO";
                        representatives.add(new DonanteRequests.Representante(null, rNom, rApe, "DNI", rDoc, rNac, rGen));
                    }
                }
                if (representatives.isEmpty()) {
                    representatives.add(new DonanteRequests.Representante(null, "Representante", "Principal", "DNI", "11111111", LocalDate.of(1990, 1, 1), "OTRO"));
                }
                api.crearJuridico(new DonanteRequests.Juridico(form.numeroDocumento(), form.razonSocial(),
                        form.tipoEntidad(), form.rubro(), direction, contacts, defaultContact, representatives));
            }
            flash.addFlashAttribute("success", "Donante registrado correctamente en la API.");
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
        } catch (RestClientException exception) {
            flash.addFlashAttribute("apiError", errors.describe(exception));
        }
        return "redirect:/admin/dashboard/donantes";
    }
}

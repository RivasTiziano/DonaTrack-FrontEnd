package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.services.AccesoService;
import ar.edu.utn.donatrack.services.AccesoService.Acceso;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Crear acceso para un donante o una entidad. Solo se llega desde la fila del listado: no hay un id libre.
 * La contraseña nunca vuelve a la vista.
 */
@Controller
public class AccesoController {
    private static final String ACCESO_CREADO = "Acceso creado. Comunicale la contraseña inicial a la persona por un canal seguro.";
    private static final String LISTADO_DONANTES = "/admin/dashboard/donantes";
    private static final String LISTADO_ENTIDADES = "/admin/dashboard/beneficiarios";

    private final AccesoService service;
    private final ApiErrorMessages errors;

    public AccesoController(AccesoService service, ApiErrorMessages errors) {
        this.service = service;
        this.errors = errors;
    }

    @GetMapping("/admin/dashboard/donantes/{tipo:humanos|juridicos}/{id}/acceso")
    public String formularioDonante(@PathVariable String tipo, @PathVariable Long id, Model model) {
        return formulario(model, service.perfil(tipo, id), LISTADO_DONANTES);
    }

    @PostMapping("/admin/dashboard/donantes/{tipo:humanos|juridicos}/{id}/acceso")
    public String crearDonante(@PathVariable String tipo, @PathVariable Long id,
                               @RequestParam(defaultValue = "") String password,
                               @RequestParam(defaultValue = "") String confirmacion,
                               Model model, RedirectAttributes flash) {
        return crear(service.perfil(tipo, id), password, confirmacion, model, flash, LISTADO_DONANTES);
    }

    @PostMapping("/admin/dashboard/donantes/{tipo:humanos|juridicos}/{id}/acceso/contacto")
    public String completarContacto(@PathVariable String tipo,@PathVariable Long id,
                                    @RequestParam(defaultValue="") String email,
                                    Model model,RedirectAttributes flash) {
        try {
            service.completarEmail(tipo,id,email);
            flash.addFlashAttribute("success","Email de contacto guardado.");
            return "redirect:/admin/dashboard/donantes/" + tipo + "/" + id + "/acceso";
        } catch(FormularioInvalidoException exception) { model.addAttribute("apiError",exception.getMessage()); }
        catch(RestClientException exception) { model.addAttribute("apiError",errors.describe(exception)); }
        model.addAttribute("tipo",tipo);
        model.addAttribute("id",id);
        model.addAttribute("completarContacto",true);
        model.addAttribute("emailContacto",email);
        return "admin-perfil";
    }

    @GetMapping("/admin/dashboard/beneficiarios/{id}/acceso")
    public String formularioEntidad(@PathVariable Long id, Model model) {
        return formulario(model, service.perfil("beneficiarios", id), LISTADO_ENTIDADES);
    }

    @PostMapping("/admin/dashboard/beneficiarios/{id}/acceso")
    public String crearEntidad(@PathVariable Long id,
                               @RequestParam(defaultValue = "") String password,
                               @RequestParam(defaultValue = "") String confirmacion,
                               Model model, RedirectAttributes flash) {
        return crear(service.perfil("beneficiarios", id), password, confirmacion, model, flash, LISTADO_ENTIDADES);
    }

    private String formulario(Model model, Acceso perfil, String volver) {
        if (!service.habilitado(perfil.tipo(), perfil.id())) {
            cargar(model, perfil, volver);
            model.addAttribute("yaTieneAcceso", true);
            return "admin-acceso";
        }
        if (perfil.email() == null) return "redirect:" + ("beneficiarios".equals(perfil.tipo()) ? "/admin/dashboard/beneficiarios/" + perfil.id() : "/admin/dashboard/donantes/" + perfil.tipo() + "/" + perfil.id()) + "?completarContacto=true";
        cargar(model, perfil, volver);
        return "admin-acceso";
    }

    private String crear(Acceso perfil, String password, String confirmacion, Model model,
                         RedirectAttributes flash, String volver) {
        try {
            service.crear(perfil, password, confirmacion);
            flash.addFlashAttribute("success", ACCESO_CREADO);
            return "redirect:" + volver;
        } catch (FormularioInvalidoException exception) {
            model.addAttribute("apiError", exception.getMessage());
        } catch (RestClientException exception) {
            model.addAttribute("apiError", errors.describe(exception));
        }
        cargar(model, perfil, volver);
        return "admin-acceso";
    }

    private void cargar(Model model, Acceso perfil, String volver) {
        String base = "beneficiarios".equals(perfil.tipo())
                ? "/admin/dashboard/beneficiarios/" + perfil.id()
                : "/admin/dashboard/donantes/" + perfil.tipo() + "/" + perfil.id();
        model.addAttribute("perfil", perfil);
        model.addAttribute("accion", base + "/acceso");
        model.addAttribute("volver", volver);
    }
}

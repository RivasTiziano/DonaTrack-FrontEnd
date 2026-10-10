package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.controllers.support.ApiViewSupport;
import ar.edu.utn.donatrack.forms.BienForm;
import ar.edu.utn.donatrack.forms.DonanteForm;
import ar.edu.utn.donatrack.forms.PerfilForm;
import ar.edu.utn.donatrack.services.DonacionesService;
import ar.edu.utn.donatrack.validators.DonacionesValidator;
import ar.edu.utn.donatrack.forms.BienesAdicionalesForm;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controlador MVC: prepara vistas y delega los casos de uso en el servicio del frontend. */
@Controller
public class DonacionesController {
    private final DonacionesService service;
    private final DonacionesValidator validator;
    private final ApiViewSupport views;
    private final ApiErrorMessages errors;

    public DonacionesController(DonacionesService service, ApiViewSupport views, ApiErrorMessages errors, DonacionesValidator validator) {
        this.service = service;
        this.validator = validator;
        this.views = views;
        this.errors = errors;
    }

    // Listados y operaciones de Donaciones

    @GetMapping("/admin/dashboard/donaciones")
    public String donaciones(Model model) {
        load(model, () -> model.addAttribute("donaciones", service.donaciones()));
        load(model, () -> model.addAttribute("categorias", service.categorias()));
        return page(model, "Donaciones", "donaciones", "donations");
    }

    @GetMapping("/admin/dashboard/donaciones/{id}")
    public String donacion(@PathVariable Long id, Model model) {
        load(model, () -> model.addAttribute("donacion", service.donacion(id)));
        load(model, () -> model.addAttribute("asignaciones", service.asignaciones(id)));
        return page(model, "Detalle de donación", "detalle", "donations");
    }

    @GetMapping("/admin/dashboard/bienes")
    public String bienes(Model model) {
        load(model, () -> model.addAttribute("bienes", service.bienes()));
        load(model, () -> model.addAttribute("categorias", service.categorias()));
        return page(model, "Bienes", "bienes", "donations");
    }

    @GetMapping("/admin/dashboard/catalogo")
    public String catalogo(Model model) {
        load(model, () -> model.addAttribute("categorias", service.categorias()));
        return page(model, "Catálogo de categorías", "catalogo", "catalog");
    }

    @GetMapping("/admin/dashboard/necesidades")
    public String necesidades(Model model) {
        load(model, () -> model.addAttribute("necesidades", service.necesidades()));
        load(model, () -> model.addAttribute("beneficiarios", service.beneficiarios()));
        load(model, () -> model.addAttribute("categorias", service.categorias()));
        return page(model, "Necesidades", "necesidades", "needs");
    }

    @GetMapping("/admin/dashboard/beneficiarios")
    public String beneficiarios(Model model) {
        load(model, () -> model.addAttribute("beneficiarios", service.beneficiarios()));
        return page(model, "Entidades beneficiarias", "beneficiarios", "beneficiaries");
    }

    @GetMapping("/admin/dashboard/asignar")
    public String asignar(Model model) {
        load(model, () -> model.addAttribute("donaciones", service.donaciones()));
        load(model, () -> model.addAttribute("necesidades", service.necesidades()));
        load(model, () -> model.addAttribute("algoritmos", service.algoritmos()));
        return page(model, "Matchmaking y asignación", "asignar", "assign");
    }

    @GetMapping("/admin/dashboard/importar")
    public String importar(Model model) {
        return page(model, "Importar personas donantes desde CSV", "importar", "import");
    }

    @PostMapping("/admin/dashboard/catalogo/categorias")
    public String categoria(@RequestParam String nombre, RedirectAttributes flash) {
        return write(flash, "catalogo", () -> service.crearCategoria(nombre));
    }

    @PostMapping("/admin/dashboard/catalogo/subcategorias")
    public String subcategoria(@RequestParam Long categoriaId, @RequestParam String nombre, RedirectAttributes flash) {
        return write(flash, "catalogo", () -> service.crearSubCategoria(categoriaId, nombre));
    }

    @PostMapping("/admin/dashboard/bienes")
    public String bien(@ModelAttribute BienForm form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "bienes");
        return write(flash, "bienes", () -> service.crearBien(form));
    }

    @PostMapping("/admin/dashboard/donaciones")
    public String donar(@RequestParam Long donanteId,@RequestParam String descripcionGeneral,
                        @ModelAttribute BienForm form, BindingResult binding,
                        @ModelAttribute BienesAdicionalesForm adicionales, BindingResult adicionalesBinding,
                        RedirectAttributes flash) {
        if(binding.hasErrors() || adicionalesBinding.hasErrors()) return invalid(flash,"donaciones");
        return write(flash,"donaciones",()->service.donar(donanteId,descripcionGeneral,form,adicionales));
    }

    @PostMapping("/admin/dashboard/bienes/{id}")
    public String actualizarBien(@PathVariable Long id, @ModelAttribute BienForm form,
                                 BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "bienes");
        return write(flash, "bienes", () -> service.actualizarBien(id, form));
    }

    @PostMapping("/admin/dashboard/necesidades/{id}")
    public String actualizarNecesidad(@PathVariable Long id, @RequestParam String descripcionNecesidad,
                                      @RequestParam String tipoNecesidad, @RequestParam(defaultValue = "") String periodo,
                                      @ModelAttribute BienForm form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "necesidades");
        return write(flash, "necesidades", () -> service.actualizarNecesidad(id, descripcionNecesidad, tipoNecesidad, periodo, form));
    }

    @PostMapping("/admin/dashboard/donaciones/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam String estado,
                         @RequestParam(defaultValue = "") String justificacion, RedirectAttributes flash) {
        return write(flash, "donaciones", () -> service.estado(id, estado, justificacion));
    }

    @PostMapping("/admin/dashboard/donaciones/{id}/eliminar")
    public String eliminarDonacion(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "donaciones", () -> service.eliminarDonacion(id));
    }

    @PostMapping("/admin/dashboard/bienes/{id}/eliminar")
    public String eliminarBien(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "bienes", () -> service.eliminarBien(id));
    }

    @PostMapping("/admin/dashboard/necesidades")
    public String necesidad(@RequestParam Long entidadBeneficiariaId, @RequestParam String descripcionNecesidad,
                            @RequestParam String tipoNecesidad, @RequestParam(defaultValue = "") String periodo,
                            @ModelAttribute BienForm form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "necesidades");
        return write(flash, "necesidades", () -> service.crearNecesidad(entidadBeneficiariaId, descripcionNecesidad, tipoNecesidad, periodo, form));
    }

    @PostMapping("/admin/dashboard/necesidades/{id}/eliminar")
    public String eliminarNecesidad(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "necesidades", () -> service.eliminarNecesidad(id));
    }

    @PostMapping("/admin/dashboard/asignar/sugerir")
    public String sugerir(@RequestParam Long donacionId, RedirectAttributes flash) {
        return write(flash, "asignar", () -> flash.addFlashAttribute("sugerencia", service.sugerir(donacionId)));
    }

    @PostMapping("/admin/dashboard/asignar")
    public String confirmar(@RequestParam Long donacionId, @RequestParam Long necesidadId,
                            @RequestParam Float cantidadAAsignar, RedirectAttributes flash) {
        return write(flash, "asignar", () -> service.asignar(donacionId, necesidadId, cantidadAAsignar));
    }

    @PostMapping("/admin/dashboard/algoritmos")
    public String algoritmo(@RequestParam String nombre, @RequestParam boolean activo, RedirectAttributes flash) {
        return write(flash, "asignar", () -> service.algoritmo(nombre, activo));
    }

    @PostMapping("/admin/dashboard/importar")
    public String importarCsv(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes flash) {
        return write(flash, "importar", () -> flash.addFlashAttribute("importacion", service.importar(archivo)));
    }

    // Registro de donantes

    @GetMapping("/admin/dashboard/donantes/nuevo/{tipo:humanos|juridicos}")
    public String nuevo(@PathVariable String tipo, Model model) {
        model.addAttribute("tipo", tipo);
        return "admin-donante-form";
    }

    @PostMapping("/admin/dashboard/donantes/nuevo/{tipo:humanos|juridicos}")
    public String crear(@PathVariable String tipo, @ModelAttribute("form") DonanteForm form,
                        BindingResult binding, Model model, RedirectAttributes flash) {
        model.addAttribute("tipo",tipo);
        model.addAllAttributes(service.camposDonante(form));
        validator.validar(tipo,form,binding);
        if(binding.hasErrors()) {
            model.addAttribute("apiError",binding.getAllErrors().get(0).getDefaultMessage());
            return "admin-donante-form";
        }
        try {
            service.crear(tipo,form);
            flash.addFlashAttribute("success","Donante registrado correctamente en la API.");
            return "redirect:/admin/dashboard/donantes";
        } catch (FormularioInvalidoException e) {
            model.addAttribute("apiError",e.getMessage());
        } catch (RestClientException e) { model.addAttribute("apiError",errors.describe(e)); }
        return "admin-donante-form";
    }

    @PostMapping("/admin/dashboard/donantes/{tipo:humanos|juridicos}/{id}/eliminar")
    public String eliminarDonante(@PathVariable String tipo, @PathVariable Long id, RedirectAttributes flash) {
        try {
            service.eliminarDonante(tipo,id);
            flash.addFlashAttribute("success", "Eliminación confirmada por la API.");
        } catch (RestClientException exception) {
            flash.addFlashAttribute("apiError", errors.describe(exception));
        }
        return "redirect:/admin/dashboard/donantes";
    }

    @GetMapping("/admin/dashboard/donantes")
    public String listar(Model model) {
        model.addAttribute("user",Map.of("name","Administrador","email","","role","Administrador de Depósito"));
        model.addAttribute("donorsList",List.of());
        try { model.addAttribute("donorsList",service.filasDonantes()); }
        catch(RestClientException | IllegalStateException e) {
            model.addAttribute("errorDonantes","No se pudo obtener el listado de donantes. Verificá que el servicio de Donaciones esté disponible.");
        }
        return "dashboard-admin-donantes";
    }

    @GetMapping("/admin/dashboard/donantes/{tipo:humanos|juridicos}/{id}")
    public String donante(@PathVariable String tipo,@PathVariable Long id,Model model) {
        prepare(model,tipo,id);
        try {
            var perfil=service.perfilDonante(tipo,id);
            model.addAttribute("form",perfil.form());
            model.addAttribute("contactos",perfil.contactos());
            model.addAttribute("representantes",perfil.representantes());
        } catch(RestClientException | IllegalStateException e) { model.addAttribute("apiError",errors.describe(e)); }
        return "admin-perfil";
    }

    @PostMapping("/admin/dashboard/donantes/{tipo:humanos|juridicos}/{id}")
    public String guardarDonante(@PathVariable String tipo,@PathVariable Long id,
            @ModelAttribute("form") PerfilForm form,BindingResult binding,Model model,RedirectAttributes flash) {
        prepare(model,tipo,id);
        validator.validarPerfil(tipo,form,id,binding);
        if(binding.hasErrors()) {
            model.addAttribute("apiError",binding.getAllErrors().get(0).getDefaultMessage());
            return "admin-perfil";
        }
        try {
            service.guardarDonante(tipo,id,form);
            flash.addFlashAttribute("success","Datos actualizados; contactos y representantes conservados.");
            return "redirect:/admin/dashboard/donantes";
        } catch(FormularioInvalidoException e) { model.addAttribute("apiError",e.getMessage()); }
        catch(RestClientException | IllegalStateException e) { model.addAttribute("apiError",errors.describe(e)); }
        return "admin-perfil";
    }

    @GetMapping("/admin/dashboard/beneficiarios/nuevo")
    public String nuevoBeneficiario(Model model) {
        prepare(model,"beneficiarios",null);
        model.addAttribute("form",service.nuevoBeneficiario());
        return "admin-perfil";
    }

    @GetMapping("/admin/dashboard/beneficiarios/{id}")
    public String beneficiario(@PathVariable Long id,Model model) {
        prepare(model,"beneficiarios",id);
        try {
            var perfil=service.perfilBeneficiario(id);
            model.addAttribute("form",perfil.form());
            model.addAttribute("representantes",perfil.representantes());
        } catch(RestClientException | IllegalStateException e) { model.addAttribute("apiError",errors.describe(e)); }
        return "admin-perfil";
    }

    @PostMapping({"/admin/dashboard/beneficiarios/nuevo","/admin/dashboard/beneficiarios/{id}"})
    public String guardarBeneficiario(@PathVariable(required=false) Long id,
            @ModelAttribute("form") PerfilForm form,BindingResult binding,
            @RequestParam MultiValueMap<String,String> params,Model model,RedirectAttributes flash) {
        prepare(model,"beneficiarios",id);
        model.addAttribute("repNombre",params.get("repNombre"));
        model.addAttribute("repTelefono",params.get("repTelefono"));
        model.addAttribute("repEmail",params.get("repEmail"));
        validator.validarPerfil("beneficiarios",form,id,binding);
        if(id==null) validator.validarRepresentantesBeneficiario(params.get("repNombre"),params.get("repTelefono"),params.get("repEmail"),binding);
        if(binding.hasErrors()) {
            model.addAttribute("apiError",binding.getAllErrors().get(0).getDefaultMessage());
            return "admin-perfil";
        }
        try {
            service.guardarBeneficiario(id,form,params.get("repNombre"),params.get("repTelefono"),params.get("repEmail"));
            flash.addFlashAttribute("success","Entidad guardada por Donaciones.");
            return "redirect:/admin/dashboard/beneficiarios";
        } catch(FormularioInvalidoException e) { model.addAttribute("apiError",e.getMessage()); }
        catch(RestClientException | IllegalStateException e) { model.addAttribute("apiError",errors.describe(e)); }
        return "admin-perfil";
    }

    @PostMapping("/admin/dashboard/beneficiarios/{id}/eliminar")
    public String eliminarBeneficiario(@PathVariable Long id, RedirectAttributes flash) {
        return views.write(flash, "/admin/dashboard/beneficiarios", () -> service.eliminarBeneficiario(id));
    }

    private void prepare(Model model, String tipo, Long id) {
        model.addAttribute("tipo", tipo);
        model.addAttribute("id", id);
    }

    // Consultas y evaluación

    @GetMapping("/admin/dashboard/bienes/{id}")
    public String bien(@PathVariable Long id, Model model) {
        views.load(model, "bien", () -> service.bien(id));
        return views.page(model, "Detalle del bien", "bien");
    }

    @GetMapping("/admin/dashboard/necesidades/{id}")
    public String necesidad(@PathVariable Long id, Model model) {
        views.load(model, "necesidad", () -> service.necesidad(id));
        return views.page(model, "Detalle de necesidad", "necesidad");
    }

    @GetMapping("/admin/dashboard/asignaciones")
    public String asignaciones(@RequestParam(required = false) Long entidadBeneficiariaId,
                               @RequestParam(defaultValue = "") String estado, Model model) {
        views.load(model, "beneficiarios", service::beneficiarios);
        model.addAttribute("entidadId", entidadBeneficiariaId);
        model.addAttribute("estado", estado);
        if (entidadBeneficiariaId != null)
            views.load(model, "asignaciones", () -> service.asignacionesEntidad(entidadBeneficiariaId, estado));
        return views.page(model, "Asignaciones por entidad", "asignaciones");
    }

    @GetMapping("/admin/dashboard/donaciones/{id}/contactos")
    public String contactosDonacion(@PathVariable Long id, Model model) {
        views.load(model, "contactos", () -> service.contactosDonacion(id));
        return views.page(model, "Contactos de la donación", "contactos");
    }

    @GetMapping("/admin/dashboard/asignaciones/{id}/contactos")
    public String contactosAsignacion(@PathVariable Long id, Model model) {
        views.load(model, "contactos", () -> service.contactosAsignacion(id));
        return views.page(model, "Contactos", "contactos");
    }

    @GetMapping("/admin/dashboard/sugerencias")
    public String sugerencias(Model model) {
        views.load(model, "sugerencias", service::sugerencias);
        return views.page(model, "Sugerencias guardadas", "sugerencias");
    }

    @GetMapping("/admin/dashboard/sugerencias/{id}")
    public String sugerencia(@PathVariable Long id, Model model) {
        views.load(model, "sugerencia", () -> service.sugerencia(id));
        return views.page(model, "Sugerencia de donación", "sugerencia");
    }

    @PostMapping("/admin/dashboard/evaluador/ejecutar")
    public String evaluar(RedirectAttributes flash) {
        return views.write(flash, "/admin/dashboard/sugerencias", () -> service.evaluar());
    }

    @ModelAttribute
    public void defaults(Model model, jakarta.servlet.http.HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/admin/dashboard")) views.defaults(model);
    }

    private void load(Model model,Runnable loader) { views.load(model,loader); }

    private String page(Model model,String title,String section,String tab) {
        return views.adminPage(model,title,section,tab);
    }

    private String write(RedirectAttributes flash,String path,Runnable operation) {
        return views.write(flash,"/admin/dashboard/"+path,operation);
    }

    private String invalid(RedirectAttributes flash,String path) { return views.invalid(flash,path); }
    // Pantallas públicas o personales: conservan sus URL y no requieren prefijo administrativo.

    @GetMapping("/explorar-donaciones")
    public String map(Model model) {
        model.addAllAttributes(service.explorarDonaciones());
        return "map";
    }

    @GetMapping("/explorar-donaciones/{id}")
    public String donationDetail(@PathVariable String id, Model model) {
        model.addAllAttributes(service.detallePublico(id));
        return "donation-detail";
    }

    @GetMapping("/donante/dashboard/donaciones")
    public String donorDonations(Model model) {
        model.addAllAttributes(service.donacionesDonante());
        return "dashboard-donor-donaciones";
    }

    @GetMapping("/donante/dashboard/entidades")
    public String donorEntities(Model model) {
        model.addAllAttributes(service.entidadesDonante());
        return "dashboard-donor-entidades";
    }

    @GetMapping("/entidad/dashboard/necesidades")
    public String beneficiaryNeeds(Model model) {
        model.addAllAttributes(service.necesidadesBeneficiario());
        return "dashboard-beneficiary-necesidades";
    }

    @GetMapping("/entidad/dashboard/donaciones")
    public String beneficiaryDonations(Model model) {
        model.addAllAttributes(service.donacionesBeneficiario());
        return "dashboard-beneficiary-donaciones";
    }
}

package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.ApiRequests;
import ar.edu.utn.donatrack.forms.BienForm;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminOperacionesController {
    private final DonacionesApiService donaciones;
    private final LogisticaApiService logistica;
    private final IncentivosApiService incentivos;
    private final ApiErrorMessages errors;

    public AdminOperacionesController(DonacionesApiService donaciones, LogisticaApiService logistica,
                                      IncentivosApiService incentivos, ApiErrorMessages errors) {
        this.donaciones = donaciones; this.logistica = logistica;
        this.incentivos = incentivos; this.errors = errors;
    }

    @ModelAttribute
    public void defaults(Model model) {
        model.addAttribute("user", Map.of("name", "Administración (sin autenticación)"));
        for (String name : List.of("donaciones", "bienes", "categorias", "necesidades", "beneficiarios", "camiones", "ranking", "algoritmos", "asignaciones", "entregas", "rutas"))
            model.addAttribute(name, List.of());
    }

    private void load(Model model, Runnable loader) {
        try { loader.run(); } catch (RestClientException | IllegalStateException exception) {
            model.addAttribute("apiError", errors.describe(exception));
        }
    }
    private String page(Model model, String pageTitle, String section, String activeTab) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("section", section);
        model.addAttribute("activeTab", activeTab);
        return "admin-api";
    }

    @GetMapping
    public String index(Model model) { return page(model, "Panel General", "inicio", "inicio"); }

    @GetMapping("/donaciones")
    public String donaciones(Model model) {
        load(model, () -> model.addAttribute("donaciones", donaciones.donaciones()));
        load(model, () -> model.addAttribute("categorias", donaciones.categorias()));
        return page(model, "Donaciones", "donaciones", "donations");
    }

    @GetMapping("/donaciones/{id}")
    public String donacion(@PathVariable Long id, Model model) {
        load(model, () -> model.addAttribute("donacion", donaciones.donacion(id)));
        load(model, () -> model.addAttribute("asignaciones", donaciones.asignaciones(id)));
        return page(model, "Detalle de donación", "detalle", "donations");
    }

    @GetMapping("/bienes")
    public String bienes(Model model) {
        load(model, () -> model.addAttribute("bienes", donaciones.bienes()));
        load(model, () -> model.addAttribute("categorias", donaciones.categorias()));
        return page(model, "Bienes", "bienes", "donations");
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        load(model, () -> model.addAttribute("categorias", donaciones.categorias()));
        return page(model, "Catálogo de categorías", "catalogo", "catalog");
    }

    @GetMapping("/necesidades")
    public String necesidades(Model model) {
        load(model, () -> model.addAttribute("necesidades", donaciones.necesidades()));
        load(model, () -> model.addAttribute("beneficiarios", donaciones.beneficiarios()));
        load(model, () -> model.addAttribute("categorias", donaciones.categorias()));
        return page(model, "Necesidades", "necesidades", "needs");
    }

    @GetMapping("/beneficiarios")
    public String beneficiarios(Model model) {
        load(model, () -> model.addAttribute("beneficiarios", donaciones.beneficiarios()));
        return page(model, "Entidades beneficiarias", "beneficiarios", "beneficiaries");
    }

    @GetMapping("/camiones")
    public String camiones(Model model) {
        load(model, () -> model.addAttribute("camiones", logistica.camiones()));
        return page(model, "Camiones de logística", "camiones", "trucks");
    }

    @GetMapping("/rankings")
    public String rankings(Model model) {
        load(model, () -> model.addAttribute("ranking", incentivos.ranking()));
        return page(model, "Ranking de incentivos", "rankings", "rankings");
    }

    @GetMapping("/entregas")
    public String entregas(Model model) {
        load(model, () -> model.addAttribute("entregas", logistica.entregas()));
        return page(model, "Entregas logísticas", "entregas", "trucks");
    }

    @GetMapping("/rutas")
    public String rutas(Model model) {
        load(model, () -> model.addAttribute("rutas", logistica.rutas()));
        return page(model, "Rutas logísticas", "rutas", "trucks");
    }

    @PostMapping("/rutas/{id}/iniciar")
    public String iniciarRuta(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "rutas", () -> logistica.iniciarRuta(id));
    }
    @PostMapping("/rutas/{id}/finalizar")
    public String finalizarRuta(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "rutas", () -> logistica.finalizarRuta(id));
    }
    @PostMapping("/rutas/{id}/cancelar")
    public String cancelarRuta(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "rutas", () -> logistica.cancelarRuta(id));
    }

    @GetMapping("/asignar")
    public String asignar(Model model) {
        load(model, () -> model.addAttribute("donaciones", donaciones.donaciones()));
        load(model, () -> model.addAttribute("necesidades", donaciones.necesidades()));
        load(model, () -> model.addAttribute("algoritmos", donaciones.algoritmos()));
        return page(model, "Matchmaking y asignación", "asignar", "assign");
    }

    @GetMapping("/importar")
    public String importar(Model model) {
        return page(model, "Importar personas donantes desde CSV", "importar", "import");
    }

    private String write(RedirectAttributes flash, String path, Runnable operation) {
        try {
            operation.run(); flash.addFlashAttribute("success", "Operación confirmada por el servicio.");
        } catch (RestClientException | IllegalStateException exception) {
            flash.addFlashAttribute("apiError", errors.describe(exception));
        }
        return "redirect:/admin/dashboard/" + path;
    }

    @PostMapping("/catalogo/categorias")
    public String categoria(@RequestParam String nombre, RedirectAttributes flash) {
        return write(flash, "catalogo", () -> donaciones.crearCategoria(nombre));
    }
    @PostMapping("/catalogo/subcategorias")
    public String subcategoria(@RequestParam Long categoriaId, @RequestParam String nombre, RedirectAttributes flash) {
        return write(flash, "catalogo", () -> donaciones.crearSubCategoria(categoriaId, nombre));
    }
    @PostMapping("/bienes")
    public String bien(@ModelAttribute BienForm form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "bienes");
        return write(flash, "bienes", () -> donaciones.crearBien(form.request()));
    }
    @PostMapping("/donaciones")
    public String donar(@RequestParam Long donanteId, @RequestParam String descripcionGeneral,
                        @ModelAttribute BienForm form,
                        @RequestParam(name = "bienDescripcion", required = false) List<String> bienDescripcion,
                        @RequestParam(name = "bienFoto", required = false) List<String> bienFoto,
                        @RequestParam(name = "bienCantidad", required = false) List<Float> bienCantidad,
                        @RequestParam(name = "bienSubCategoriaId", required = false) List<Long> bienSubCategoriaId,
                        @RequestParam(name = "bienUnidadMedida", required = false) List<String> bienUnidadMedida,
                        @RequestParam(name = "bienTipoBien", required = false) List<String> bienTipoBien,
                        @RequestParam(name = "bienFechaDeVencimiento", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> bienFechaDeVencimiento,
                        @RequestParam(name = "bienFueUsado", required = false) List<Boolean> bienFueUsado,
                        BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "donaciones");
        var listaBienes = new ArrayList<ApiRequests.Bien>();
        if (form.descripcion() != null && !form.descripcion().isBlank()) {
            listaBienes.add(form.request());
        }
        if (bienDescripcion != null) {
            for (int i = 0; i < bienDescripcion.size(); i++) {
                String desc = bienDescripcion.get(i);
                if (desc == null || desc.isBlank()) continue;
                String foto = (bienFoto != null && i < bienFoto.size()) ? bienFoto.get(i) : null;
                Float cant = (bienCantidad != null && i < bienCantidad.size()) ? bienCantidad.get(i) : 1f;
                Long subCat = (bienSubCategoriaId != null && i < bienSubCategoriaId.size()) ? bienSubCategoriaId.get(i) : null;
                String unidad = (bienUnidadMedida != null && i < bienUnidadMedida.size()) ? bienUnidadMedida.get(i) : "UNIDAD";
                String tipo = (bienTipoBien != null && i < bienTipoBien.size()) ? bienTipoBien.get(i) : "DURABLE";
                LocalDate venc = (bienFechaDeVencimiento != null && i < bienFechaDeVencimiento.size()) ? bienFechaDeVencimiento.get(i) : null;
                Boolean usado = (bienFueUsado != null && i < bienFueUsado.size()) ? bienFueUsado.get(i) : null;
                listaBienes.add(new ApiRequests.Bien(desc, foto, cant, subCat, unidad, tipo, venc, usado));
            }
        }
        if (listaBienes.isEmpty()) {
            flash.addFlashAttribute("apiError", "Debe agregar al menos un bien a la donación.");
            return "redirect:/admin/dashboard/donaciones";
        }
        return write(flash, "donaciones", () -> donaciones.crearDonacion(
                new ApiRequests.Donacion(donanteId, descripcionGeneral, listaBienes)));
    }
    @PostMapping("/bienes/{id}")
    public String actualizarBien(@PathVariable Long id, @ModelAttribute BienForm form,
                                 BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "bienes");
        return write(flash, "bienes", () -> donaciones.actualizarBien(id, form.request()));
    }
    @PostMapping("/necesidades/{id}")
    public String actualizarNecesidad(@PathVariable Long id, @RequestParam String descripcionNecesidad,
                                      @RequestParam String tipoNecesidad, @RequestParam(defaultValue = "") String periodo,
                                      @ModelAttribute BienForm form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "necesidades");
        return write(flash, "necesidades", () -> donaciones.actualizarNecesidad(id,
                new ApiRequests.ActualizarNecesidad(form.request(), descripcionNecesidad,
                        tipoNecesidad, periodo.isBlank() ? null : periodo)));
    }
    @PostMapping("/donaciones/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam String estado,
                         @RequestParam(defaultValue = "") String justificacion, RedirectAttributes flash) {
        return write(flash, "donaciones", () -> donaciones.estado(id, new ApiRequests.EstadoDonacion(null, estado, justificacion)));
    }
    @PostMapping("/donaciones/{id}/eliminar")
    public String eliminarDonacion(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "donaciones", () -> donaciones.eliminarDonacion(id));
    }
    @PostMapping("/bienes/{id}/eliminar")
    public String eliminarBien(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "bienes", () -> donaciones.eliminarBien(id));
    }
    @PostMapping("/necesidades")
    public String necesidad(@RequestParam Long entidadBeneficiariaId, @RequestParam String descripcionNecesidad,
                            @RequestParam String tipoNecesidad, @RequestParam(defaultValue = "") String periodo,
                            @ModelAttribute BienForm form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return invalid(flash, "necesidades");
        return write(flash, "necesidades", () -> donaciones.crearNecesidad(new ApiRequests.Necesidad(
                entidadBeneficiariaId, form.request(), descripcionNecesidad, tipoNecesidad,
                periodo.isBlank() ? null : periodo)));
    }
    @PostMapping("/necesidades/{id}/eliminar")
    public String eliminarNecesidad(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "necesidades", () -> donaciones.eliminarNecesidad(id));
    }
    @PostMapping("/camiones")
    public String camion(@RequestParam String patente, @RequestParam Float capacidadVolumen,
                         @RequestParam Float altura, @RequestParam Float capacidadCarga, RedirectAttributes flash) {
        return write(flash, "camiones", () -> logistica.crear(new ApiRequests.Camion(patente, capacidadVolumen, altura, capacidadCarga)));
    }
    @PostMapping("/camiones/{id}/estado")
    public String estadoCamion(@PathVariable Long id, @RequestParam String estado, RedirectAttributes flash) {
        return write(flash, "camiones", () -> logistica.estado(id, estado));
    }
    @PostMapping("/camiones/{id}/eliminar")
    public String eliminarCamion(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "camiones", () -> logistica.eliminar(id));
    }
    @PostMapping("/asignar/sugerir")
    public String sugerir(@RequestParam Long donacionId, RedirectAttributes flash) {
        return write(flash, "asignar", () -> flash.addFlashAttribute("sugerencia", donaciones.sugerir(donacionId)));
    }
    @PostMapping("/asignar")
    public String confirmar(@RequestParam Long donacionId, @RequestParam Long necesidadId,
                            @RequestParam Float cantidadAAsignar, RedirectAttributes flash) {
        return write(flash, "asignar", () -> donaciones.asignar(new ApiRequests.Asignacion(donacionId, necesidadId, cantidadAAsignar)));
    }
    @PostMapping("/algoritmos")
    public String algoritmo(@RequestParam String nombre, @RequestParam boolean activo, RedirectAttributes flash) {
        return write(flash, "asignar", () -> donaciones.algoritmo(new ApiRequests.Algoritmo(nombre, activo)));
    }
    @PostMapping("/importar")
    public String importarCsv(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes flash) {
        if (archivo.isEmpty()) {
            flash.addFlashAttribute("apiError", "Seleccioná un archivo CSV no vacío.");
            return "redirect:/admin/dashboard/importar";
        }
        return write(flash, "importar", () -> flash.addFlashAttribute("importacion", donaciones.importar(archivo)));
    }
    private String invalid(RedirectAttributes flash, String path) {
        flash.addFlashAttribute("apiError", "Revisá el formato de los números y las fechas del formulario.");
        return "redirect:/admin/dashboard/" + path;
    }
}

package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.controllers.support.ApiViewSupport;

import ar.edu.utn.donatrack.services.DonacionesService;
import ar.edu.utn.donatrack.services.IncentivosService;
import ar.edu.utn.donatrack.services.PanelDonanteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class IncentivosController {
    private final IncentivosService service;
    private final DonacionesService donaciones;
    private final ApiViewSupport views;
    private final PanelDonanteService panelDonante;
    public IncentivosController(IncentivosService service, DonacionesService donaciones, ApiViewSupport views, PanelDonanteService panelDonante) {
        this.service = service; this.donaciones = donaciones; this.views = views; this.panelDonante = panelDonante;
    }

    @GetMapping("/admin/dashboard/rankings")
    public String rankings(Model model) {
        var nombres = donaciones.nombresDonantes();
        views.load(model, "ranking", () -> service.rankingConNombres(nombres));
        model.addAttribute("pageTitle", "Ranking de incentivos");
        model.addAttribute("section", "rankings");
        model.addAttribute("activeTab", "rankings");
        return "admin-api";
    }

    @GetMapping("/admin/dashboard/incentivos")
    public String index(Model model) {
        views.load(model, "categoriasIncentivos", service::categorias);
        return views.page(model, "Administración de Incentivos", "incentivos");
    }

    @GetMapping("/admin/dashboard/incentivos/categorias/{id}")
    public String categoria(@PathVariable Long id, Model model) {
        views.load(model, "categoriaIncentivos", () -> service.categoria(id));
        views.load(model, "misiones", () -> service.misiones(id));
        return views.page(model, "Misiones de categoría", "misiones");
    }

    @PostMapping("/admin/dashboard/incentivos/categorias")
    public String crearCategoria(@RequestParam String nombre, @RequestParam(required = false) Long categoriaSiguienteId,
                                 @RequestParam String tipo, @RequestParam Integer valorObjetivo, RedirectAttributes flash) {
        return views.write(flash, "/admin/dashboard/incentivos", () -> service.crearCategoria(nombre, categoriaSiguienteId, tipo, valorObjetivo));
    }

    @PostMapping("/admin/dashboard/incentivos/categorias/{id}/misiones")
    public String mision(@PathVariable Long id, @RequestParam String tipo, @RequestParam Integer valorObjetivo,
                         RedirectAttributes flash) {
        return views.write(flash, "/admin/dashboard/incentivos/categorias/" + id,
                () -> service.agregarMision(id, tipo, valorObjetivo));
    }

    @PostMapping("/admin/dashboard/incentivos/categorias/{id}/misiones/{misionId}/eliminar")
    public String eliminar(@PathVariable Long id, @PathVariable Long misionId, RedirectAttributes flash) {
        return views.write(flash, "/admin/dashboard/incentivos/categorias/" + id, () -> service.eliminarMision(id, misionId));
    }

    @GetMapping("/admin/dashboard/incentivos/donantes")
    public String seleccionar(Model model) {
        views.load(model, "humanos", donaciones::listarHumanos);
        views.load(model, "juridicos", donaciones::listarJuridicos);
        return views.page(model, "Consultar Incentivos de un donante", "seleccionarIncentivos");
    }

    @GetMapping("/admin/dashboard/incentivos/donantes/{id}")
    public String donante(@PathVariable Long id, Model model) {
        model.addAttribute("donanteId", id);
        views.load(model, () -> {
            try {
                model.addAttribute("metricas", service.metricas(id));
            } catch (HttpClientErrorException.NotFound exception) {
                model.addAttribute("donanteSinIncentivos", true);
            }
        });
        String nombre = donaciones.nombresDonantes().get(id);
        String titulo = nombre != null && !nombre.isBlank() ? "Incentivos de " + nombre : "Incentivos del donante";
        if (Boolean.TRUE.equals(model.getAttribute("donanteSinIncentivos")) || model.getAttribute("metricas") == null)
            return views.page(model, titulo, "donanteIncentivos");
        views.load(model, "misionesCompletadas", () -> service.misionesCompletadas(id));
        views.load(model, "insignias", () -> service.insignias(id));
        views.load(model, "historialCategorias", () -> service.historialCategorias(id));
        views.load(model, "progreso", () -> service.progreso(id));
        return views.page(model, titulo, "donanteIncentivos");
    }

    @GetMapping("/admin/dashboard/incentivos/historial")
    public String historial(Model model) {
        var nombres = donaciones.nombresDonantes();
        views.load(model, "historialRanking", () -> service.historialConNombres(nombres));
        return views.page(model, "Historial de rankings", "historialRanking");
    }

    // Pantallas personales: conservan sus URL y no requieren prefijo administrativo.

    @GetMapping("/donante/dashboard/incentivos")
    public String donorIncentives(Model model) {
        model.addAllAttributes(panelDonante.incentivos());
        return "dashboard-donor-incentivos";
    }

    /** Solo el propio donante puede cambiar la visibilidad de sus insignias (ADMIN recibe 403 del backend). */
    @PostMapping("/donante/dashboard/incentivos/visibilidad")
    public String visibilidadDonante(@RequestParam boolean visible, RedirectAttributes flash) {
        return views.write(flash, "/donante/dashboard/incentivos", () -> panelDonante.visibilidad(visible));
    }
}

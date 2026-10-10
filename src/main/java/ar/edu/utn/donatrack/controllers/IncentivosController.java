package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.controllers.support.ApiViewSupport;

import ar.edu.utn.donatrack.services.DonacionesService;
import ar.edu.utn.donatrack.services.IncentivosService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IncentivosController {
    private final IncentivosService service;
    private final DonacionesService donaciones;
    private final ApiViewSupport views;
    public IncentivosController(IncentivosService service, DonacionesService donaciones, ApiViewSupport views) {
        this.service = service; this.donaciones = donaciones; this.views = views;
    }

    @GetMapping("/admin/dashboard/rankings")
    public String rankings(Model model) {
        views.load(model, "ranking", service::ranking);
        model.addAttribute("pageTitle", "Ranking de incentivos");
        model.addAttribute("section", "rankings");
        model.addAttribute("activeTab", "rankings");
        model.addAttribute("user", java.util.Map.of("name", "Administración (sin autenticación)"));
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
        views.load(model, "metricas", () -> service.metricas(id));
        views.load(model, "misionesCompletadas", () -> service.misionesCompletadas(id));
        views.load(model, "insignias", () -> service.insignias(id));
        views.load(model, "historialCategorias", () -> service.historialCategorias(id));
        views.load(model, "progreso", () -> service.progreso(id));
        return views.page(model, "Incentivos del donante " + id, "donanteIncentivos");
    }

    @PostMapping("/admin/dashboard/incentivos/donantes/{id}/visibilidad")
    public String visibilidad(@PathVariable Long id, @RequestParam boolean visible, RedirectAttributes flash) {
        return views.write(flash, "/admin/dashboard/incentivos/donantes/" + id, () -> service.visibilidad(id, visible));
    }

    @GetMapping("/admin/dashboard/incentivos/historial")
    public String historial(Model model) {
        views.load(model, "historialRanking", service::historialRankings);
        return views.page(model, "Historial de rankings", "historialRanking");
    }

    // Pantallas públicas o personales: conservan sus URL y no requieren prefijo administrativo.

    @GetMapping("/donante/dashboard/incentivos")
    public String donorIncentives(Model model) {
        model.addAllAttributes(service.incentivosDonante());
        return "dashboard-donor-incentivos";
    }
}

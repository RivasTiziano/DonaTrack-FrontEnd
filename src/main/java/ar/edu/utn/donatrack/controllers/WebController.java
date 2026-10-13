package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.services.PanelDonanteService;
import ar.edu.utn.donatrack.services.PanelEntidadService;
import ar.edu.utn.donatrack.services.WebService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    private final WebService service;
    private final PanelDonanteService donante;
    private final PanelEntidadService entidad;

    public WebController(WebService service, PanelDonanteService donante, PanelEntidadService entidad) {
        this.service = service; this.donante = donante; this.entidad = entidad;
    }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAllAttributes(service.landing());
        return "landing";
    }

    /** Mapa público: entregadas con la ubicación de la entidad que las recibió. */
    @GetMapping("/explorar-donaciones")
    public String mapa(Model model) {
        model.addAllAttributes(service.mapa());
        return "map";
    }

    @GetMapping("/explorar-donaciones/{id}")
    public String detalle(@PathVariable String id, Model model) {
        model.addAllAttributes(service.detalle(id));
        return "donation-detail";
    }

    @GetMapping({"/legal", "/privacidad"})
    public String legal(Model model) {
        model.addAllAttributes(service.legal());
        return "legal";
    }

    // =========================================================================
    // DONANTE DASHBOARD
    // =========================================================================
    @GetMapping("/donante/dashboard")
    public String donorDashboard(Model model) {
        model.addAllAttributes(donante.resumen());
        return "dashboard-donor";
    }

    // =========================================================================
    // ENTIDAD BENEFICIARIA DASHBOARD
    // =========================================================================
    @GetMapping("/entidad/dashboard")
    public String beneficiaryDashboard(Model model) {
        model.addAllAttributes(entidad.resumen());
        return "dashboard-beneficiary";
    }

}

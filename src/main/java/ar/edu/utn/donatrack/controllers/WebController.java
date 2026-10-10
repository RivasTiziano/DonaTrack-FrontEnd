package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.services.WebService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final WebService service;

    public WebController(WebService service) { this.service = service; }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAllAttributes(service.landing());
        return "landing";
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
        model.addAllAttributes(service.donorDashboard());
        return "dashboard-donor";
    }

    // =========================================================================
    // ENTIDAD BENEFICIARIA DASHBOARD
    // =========================================================================
    @GetMapping("/entidad/dashboard")
    public String beneficiaryDashboard(Model model) {
        model.addAllAttributes(service.beneficiaryDashboard());
        return "dashboard-beneficiary";
    }

}

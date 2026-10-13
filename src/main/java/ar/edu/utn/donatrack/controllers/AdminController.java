package ar.edu.utn.donatrack.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Vista general del administrador; las operaciones se agrupan por microservicio. */
@Controller
public class AdminController {
    private final ar.edu.utn.donatrack.services.PanelAdminService service;
    public AdminController(ar.edu.utn.donatrack.services.PanelAdminService service) { this.service=service; }
    @GetMapping("/admin/dashboard")
    public String index(Model model, @org.springframework.web.bind.annotation.ModelAttribute("sesion") ar.edu.utn.donatrack.models.SesionUsuario sesion) {
        model.addAllAttributes(service.cargar(sesion));
        model.addAttribute("pageTitle", "Panel General");
        model.addAttribute("section", "inicio");
        model.addAttribute("activeTab", "inicio");
        return "admin-panel";
    }
}

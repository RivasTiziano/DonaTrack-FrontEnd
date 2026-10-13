package ar.edu.utn.donatrack.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Vista general del administrador; las operaciones se agrupan por microservicio. */
@Controller
public class AdminController {
    @GetMapping("/admin/dashboard")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Panel General");
        model.addAttribute("section", "inicio");
        model.addAttribute("activeTab", "inicio");
        return "admin-api";
    }
}

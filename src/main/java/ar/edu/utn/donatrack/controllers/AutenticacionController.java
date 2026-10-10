package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.services.AutenticacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Pantallas de acceso y registro de la maqueta.
 * Los POST aún no crean cuentas, verifican contraseñas ni emiten tokens.
 */
@Controller
public class AutenticacionController {
    private final AutenticacionService service;

    public AutenticacionController(AutenticacionService service) {
        this.service = service;
    }

    @GetMapping("/registro")
    public String register(Model model) {
        model.addAllAttributes(service.register());
        return "register";
    }

    @GetMapping({"/registro/entidad-beneficiaria", "/registro/beneficiario"})
    public String registerBeneficiary(Model model) {
        return "register-beneficiary";
    }

    @PostMapping({"/registro/entidad-beneficiaria", "/registro/beneficiario"})
    public String handleRegisterBeneficiary() {
        return "redirect:/entidad/dashboard";
    }

    @GetMapping("/registro/donante-humano")
    public String registerDonorHuman(Model model) {
        return "register-donor-human";
    }

    @PostMapping("/registro/donante-humano")
    public String handleRegisterDonorHuman() {
        return "redirect:/donante/dashboard";
    }

    @GetMapping("/registro/donante-organizacion")
    public String registerDonorOrganization(Model model) {
        return "register-donor-org";
    }

    @PostMapping("/registro/donante-organizacion")
    public String handleRegisterDonorOrg() {
        return "redirect:/donante/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam(value = "email", defaultValue = "") String email,
                               @RequestParam(value = "password", defaultValue = "") String password) {
        return "redirect:" + service.destinoDemostracion(email);
    }
}

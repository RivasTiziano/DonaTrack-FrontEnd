package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.services.AutenticacionService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.validators.AccesoValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Primer acceso (Entrega 1, Registro de personas donantes: correo de bienvenida para acceder por primera vez). Público, sin sesión.
 * El token viaja en el enlace del correo y en un campo oculto; la contraseña nunca vuelve a la vista.
 */
@Controller
public class PrimerAccesoController {
    private static final String VISTA = "primer-acceso";

    private final AutenticacionService service;
    private final AccesoValidator validator;
    private final ApiErrorMessages errors;

    public PrimerAccesoController(AutenticacionService service, AccesoValidator validator, ApiErrorMessages errors) {
        this.service = service;
        this.validator = validator;
        this.errors = errors;
    }

    @GetMapping("/primer-acceso")
    public String formulario(@RequestParam(defaultValue = "") String token, Model model) {
        if (token.isBlank()) model.addAttribute("enlaceInvalido", true);
        else model.addAttribute("token", token);
        return VISTA;
    }

    @PostMapping("/primer-acceso")
    public String guardar(@RequestParam(defaultValue = "") String token,
                          @RequestParam(defaultValue = "") String contrasenia,
                          @RequestParam(defaultValue = "") String confirmacion,
                          Model model) {
        if (token.isBlank()) {
            model.addAttribute("enlaceInvalido", true);
            return VISTA;
        }
        try {
            validator.validar(contrasenia, confirmacion);
            service.definirContrasenia(token, contrasenia);
            return "redirect:/login?primerAcceso";
        } catch (FormularioInvalidoException exception) {
            model.addAttribute("error", exception.getMessage());
        } catch (ResourceAccessException exception) {
            model.addAttribute("error", "No se pudo conectar con el servicio de autenticación.");
        } catch (RestClientException exception) {
            model.addAttribute("error", errors.describe(exception));
        }
        model.addAttribute("token", token);
        return VISTA;
    }
}

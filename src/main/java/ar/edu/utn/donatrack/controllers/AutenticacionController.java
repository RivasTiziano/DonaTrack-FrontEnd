package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.AutenticacionService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import java.util.Map;

/**
 * Acceso y registro. El login y el logout usan el servicio de autenticación.
 * El registro público todavía no está habilitado: el backend no tiene autoregistro (pedido E1),
 * así que los POST de registro no crean cuentas ni llaman a la API.
 */
@Controller
public class AutenticacionController {
    private final AutenticacionService service;
    private final SesionActual sesiones;
    private final ApiErrorMessages errors;

    public AutenticacionController(AutenticacionService service, SesionActual sesiones, ApiErrorMessages errors) {
        this.service = service;
        this.sesiones = sesiones;
        this.errors = errors;
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
    public String handleRegisterBeneficiary(@RequestParam Map<String, String> params, Model model) {
        return registroNoHabilitado(params, model, "register-beneficiary");
    }

    @GetMapping("/registro/donante-humano")
    public String registerDonorHuman(Model model) {
        return "register-donor-human";
    }

    @PostMapping("/registro/donante-humano")
    public String handleRegisterDonorHuman(@RequestParam Map<String, String> params, Model model) {
        return registroNoHabilitado(params, model, "register-donor-human");
    }

    @GetMapping("/registro/donante-organizacion")
    public String registerDonorOrganization(Model model) {
        return "register-donor-org";
    }

    @PostMapping("/registro/donante-organizacion")
    public String handleRegisterDonorOrg(@RequestParam Map<String, String> params, Model model) {
        return registroNoHabilitado(params, model, "register-donor-org");
    }

    /** Vuelve a mostrar el formulario con los valores cargados. No crea cuentas ni llama a la API. */
    private String registroNoHabilitado(Map<String, String> valores, Model model, String vista) {
        model.addAttribute("valores", valores);
        model.addAttribute("registroMensaje", REGISTRO_NO_HABILITADO);
        return vista;
    }

    private static final String REGISTRO_NO_HABILITADO = "El registro en línea todavía no está habilitado. Acercate al depósito: una persona administradora registra tus datos y crea tu acceso.";

    @GetMapping("/login")
    public String login() {
        return sesiones.obtener().map(sesion -> "redirect:" + sesion.panel()).orElse("login");
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam(value = "email", defaultValue = "") String email,
                               @RequestParam(value = "password", defaultValue = "") String password,
                               HttpServletRequest request, Model model) {
        // Nunca se devuelve la contraseña al formulario: solo el email.
        String emailLimpio = email.trim();
        model.addAttribute("email", emailLimpio);
        if (emailLimpio.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Ingresá tu email y contraseña.");
            return "login";
        }
        AuthDtos.Token token;
        try {
            token = service.login(emailLimpio, password);
        } catch (HttpClientErrorException.Unauthorized exception) {
            model.addAttribute("error", "Email o contraseña incorrectos.");
            return "login";
        } catch (ResourceAccessException exception) {
            model.addAttribute("error", "No se pudo conectar con el servicio de autenticación.");
            return "login";
        } catch (RestClientException | IllegalStateException exception) {
            model.addAttribute("error", errors.describe(exception));
            return "login";
        }
        // La sesión debe existir antes de pedir el perfil: el interceptor de WebApiCallerService usa su token.
        sesiones.guardar(request, SesionUsuario.desde(emailLimpio, token, emailLimpio));
        String nombre = service.nombreVisible(emailLimpio, token);
        SesionUsuario sesion = SesionUsuario.desde(emailLimpio, token, nombre);
        sesiones.guardar(request, sesion);
        return "redirect:" + sesion.panel();
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        sesiones.cerrar(request);
        return "redirect:/";
    }
}

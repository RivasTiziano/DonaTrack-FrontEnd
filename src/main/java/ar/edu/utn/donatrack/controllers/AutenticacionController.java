package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.exceptions.RegistroRechazadoException;
import ar.edu.utn.donatrack.forms.RegistroForm;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.AutenticacionService;
import ar.edu.utn.donatrack.services.RegistroService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Acceso y registro. El login y el logout usan el servicio de autenticación.
 * El registro público crea la cuenta sin iniciar sesión: al terminar, redirige al login con un aviso.
 */
@Controller
public class AutenticacionController {
    private static final String REGISTRADO = "redirect:/login?registrado";
    private static final int REPRESENTANTES_INICIALES = 2;

    private final AutenticacionService service;
    private final SesionActual sesiones;
    private final ApiErrorMessages errors;
    private final RegistroService registro;

    public AutenticacionController(AutenticacionService service, SesionActual sesiones, ApiErrorMessages errors, RegistroService registro) {
        this.service = service;
        this.sesiones = sesiones;
        this.errors = errors;
        this.registro = registro;
    }

    @GetMapping("/registro")
    public String register(Model model) {
        model.addAllAttributes(service.register());
        return "register";
    }

    @GetMapping({"/registro/entidad-beneficiaria", "/registro/beneficiario"})
    public String registerBeneficiary(Model model) {
        return formulario(model, "register-beneficiary");
    }

    @PostMapping({"/registro/entidad-beneficiaria", "/registro/beneficiario"})
    public String handleRegisterBeneficiary(@ModelAttribute("form") RegistroForm form, BindingResult binding, Model model) {
        try {
            registro.registrarEntidad(form, binding);
            return REGISTRADO;
        } catch (RegistroRechazadoException exception) {
            return reenviar(model, form, "register-beneficiary", exception);
        }
    }

    @GetMapping("/registro/donante-humano")
    public String registerDonorHuman(Model model) {
        return formulario(model, "register-donor-human");
    }

    @PostMapping("/registro/donante-humano")
    public String handleRegisterDonorHuman(@ModelAttribute("form") RegistroForm form, BindingResult binding, Model model) {
        try {
            registro.registrarHumano(form, binding);
            return REGISTRADO;
        } catch (RegistroRechazadoException exception) {
            return reenviar(model, form, "register-donor-human", exception);
        }
    }

    @GetMapping("/registro/donante-organizacion")
    public String registerDonorOrganization(Model model) {
        return formulario(model, "register-donor-org");
    }

    @PostMapping("/registro/donante-organizacion")
    public String handleRegisterDonorOrg(@ModelAttribute("form") RegistroForm form, BindingResult binding, Model model) {
        try {
            registro.registrarJuridico(form, binding);
            return REGISTRADO;
        } catch (RegistroRechazadoException exception) {
            return reenviar(model, form, "register-donor-org", exception);
        }
    }

    private String formulario(Model model, String vista) {
        RegistroForm form = new RegistroForm();
        form.completarRepresentantes(REPRESENTANTES_INICIALES);
        model.addAttribute("form", form);
        return vista;
    }

    /** Vuelve a mostrar el formulario con los datos cargados, salvo las contraseñas. No inicia sesión. */
    private String reenviar(Model model, RegistroForm form, String vista, RegistroRechazadoException exception) {
        form.limpiarContrasenas();
        form.completarRepresentantes(REPRESENTANTES_INICIALES);
        model.addAttribute("form", form);
        model.addAttribute("apiError", exception.getMessage());
        model.addAttribute("cuentaExistente", exception.motivo() == RegistroRechazadoException.Motivo.CUENTA_EXISTENTE);
        return vista;
    }

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

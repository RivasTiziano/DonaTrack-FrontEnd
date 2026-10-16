package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.controllers.support.ApiViewSupport;
import ar.edu.utn.donatrack.services.FotosRecepcion;
import ar.edu.utn.donatrack.services.LogisticaService;
import ar.edu.utn.donatrack.services.PanelDonanteService;
import ar.edu.utn.donatrack.services.PanelEntidadService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controlador MVC: prepara vistas y delega los casos de uso en el servicio del frontend. */
@Controller
public class LogisticaController {
    private final LogisticaService service;
    private final ApiViewSupport views;
    private final ApiErrorMessages errors;
    private final PanelDonanteService panelDonante;
    private final PanelEntidadService panelEntidad;
    private final FotosRecepcion fotosRecepcion;
    private final ar.edu.utn.donatrack.services.ImagenesRecepcion imagenesRecepcion;
    private final ar.edu.utn.donatrack.services.DonacionesService donaciones;

    public LogisticaController(LogisticaService service, ApiViewSupport views, ApiErrorMessages errors,
                               PanelDonanteService panelDonante, PanelEntidadService panelEntidad, FotosRecepcion fotosRecepcion,
                               ar.edu.utn.donatrack.services.ImagenesRecepcion imagenesRecepcion,
                               ar.edu.utn.donatrack.services.DonacionesService donaciones) {
        this.service = service;
        this.views = views;
        this.errors = errors;
        this.panelDonante = panelDonante;
        this.panelEntidad = panelEntidad;
        this.fotosRecepcion = fotosRecepcion;
        this.imagenesRecepcion = imagenesRecepcion;
        this.donaciones = donaciones;
    }

    // Operaciones y listados

    @GetMapping("/admin/dashboard/camiones")
    public String camiones(Model model) {
        load(model, () -> model.addAttribute("camiones", service.camiones()));
        return page(model, "Camiones de logística", "camiones", "trucks");
    }

    @GetMapping("/admin/dashboard/entregas")
    public String entregas(Model model) {
        load(model, () -> model.addAttribute("entregas", service.entregasTabla(donaciones::nombresEntidades)));
        return page(model, "Entregas logísticas", "entregas", "trucks");
    }

    @GetMapping("/admin/dashboard/rutas")
    public String rutas(Model model) {
        load(model, () -> model.addAttribute("rutas", service.rutasTabla()));
        return page(model, "Rutas logísticas", "rutas", "trucks");
    }

    @PostMapping("/admin/dashboard/rutas/{id}/iniciar")
    public String iniciarRuta(@PathVariable Long id, @RequestParam String chofer, RedirectAttributes flash) {
        return write(flash, "rutas", () -> service.iniciarRuta(id, chofer));
    }

    @PostMapping("/admin/dashboard/rutas/{id}/finalizar")
    public String finalizarRuta(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "rutas", () -> service.finalizarRuta(id));
    }

    @PostMapping("/admin/dashboard/rutas/{id}/cancelar")
    public String cancelarRuta(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "rutas", () -> service.cancelarRuta(id));
    }

    @PostMapping("/admin/dashboard/camiones")
    public String camion(@RequestParam String patente, @RequestParam Float capacidadVolumen,
                         @RequestParam Float altura, @RequestParam Float capacidadCarga, RedirectAttributes flash) {
        return write(flash, "camiones", () -> service.crearCamion(patente, capacidadVolumen, altura, capacidadCarga));
    }

    @PostMapping("/admin/dashboard/camiones/{id}/estado")
    public String estadoCamion(@PathVariable Long id, @RequestParam String estado, RedirectAttributes flash) {
        return write(flash, "camiones", () -> service.estado(id, estado));
    }

    @PostMapping("/admin/dashboard/camiones/{id}/eliminar")
    public String eliminarCamion(@PathVariable Long id, RedirectAttributes flash) {
        return write(flash, "camiones", () -> service.eliminar(id));
    }

    // Detalles y seguimiento

    @GetMapping("/admin/dashboard/camiones/{id}")
    public String camion(@PathVariable Long id, Model model) {
        views.load(model, "camion", () -> service.camion(id));
        return views.page(model, "Detalle del camión", "camion");
    }

    @GetMapping("/admin/dashboard/entregas/{id}")
    public String entrega(@PathVariable Long id, Model model) {
        views.load(model, "entrega", () -> service.entrega(id));
        // Nombre de la entidad destino: una sola consulta; si falla, el detalle se muestra igual.
        if (model.getAttribute("entrega") instanceof ar.edu.utn.donatrack.dto.ApiDtos.Entrega e && e.entidadBeneficiariaId() != null) {
            try {
                var b = donaciones.beneficiario(e.entidadBeneficiariaId());
                if (b != null) model.addAttribute("entregaDestino", b.razonSocial());
            } catch (RuntimeException ignored) { }
        }
        return views.page(model, "Entrega #" + id, "entrega");
    }

    @GetMapping("/admin/dashboard/rutas/{id}")
    public String ruta(@PathVariable Long id, Model model) {
        views.load(model, "ruta", () -> service.ruta(id));
        return views.page(model, "Detalle y paradas de ruta", "ruta");
    }

    @GetMapping("/admin/dashboard/paradas")
    public String paradas(Model model) {
        views.load(model, "paradas", service::paradas);
        return views.page(model, "Paradas logísticas", "paradas");
    }

    @GetMapping("/admin/dashboard/paradas/{id}")
    public String parada(@PathVariable Long id, Model model) {
        views.load(model, "parada", () -> service.parada(id));
        return views.page(model, "Detalle de parada", "parada");
    }

    @GetMapping("/admin/dashboard/seguimiento")
    public String seguimiento(Model model) {
        views.load(model, "ubicaciones", service::ubicaciones);
        return views.page(model, "Seguimiento de camiones", "seguimiento");
    }

    @GetMapping("/admin/dashboard/seguimiento/{patente}")
    public String ubicacion(@PathVariable String patente, Model model) {
        views.load(model, "ubicacion", () -> service.ubicacion(patente));
        return views.page(model, "Última ubicación GPS", "ubicacion");
    }

    @PostMapping("/admin/dashboard/rutas/planificar")
    public String planificar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                             RedirectAttributes flash) {
        String redirect = views.write(flash, "/admin/dashboard/rutas", () -> {
            var result = service.planificar(fecha);
            flash.addFlashAttribute("planificacion", result);
        });
        // La aceptación no significa que el callback ya haya generado rutas.
        if (flash.getFlashAttributes().containsKey("planificacion"))
            flash.addFlashAttribute("success", "Solicitud de planificación aceptada. Las rutas se generan después del callback.");
        return redirect;
    }

    @ModelAttribute
    public void defaults(Model model, jakarta.servlet.http.HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/admin/dashboard")) views.defaults(model);
    }

    private void load(Model model,Runnable loader) { views.load(model,loader); }

    private String page(Model model,String title,String section,String tab) {
        return views.adminPage(model,title,section,tab);
    }

    private String write(RedirectAttributes flash,String path,Runnable operation) {
        return views.write(flash,"/admin/dashboard/"+path,operation);
    }
    // Pantallas públicas o personales: conservan sus URL y no requieren prefijo administrativo.

    @GetMapping("/donante/dashboard/entregas")
    public String donorDeliveries(Model model) {
        model.addAllAttributes(panelDonante.entregas());
        return "dashboard-donor-entregas";
    }

    @GetMapping("/entidad/dashboard/confirmar")
    public String beneficiaryConfirm(Model model) {
        model.addAllAttributes(panelEntidad.confirmar());
        return "dashboard-beneficiary-confirmar";
    }

    @GetMapping("/entidad/dashboard/entregas")
    public String beneficiaryDeliveries(Model model) {
        model.addAllAttributes(panelEntidad.entregas());
        return "dashboard-beneficiary-entregas";
    }

    /** Confirmación de recepción por asignación. El entregaId no se acepta: lo resuelve el servidor. */
    @PostMapping("/entidad/dashboard/confirmar")
    public String confirmarRecepcion(@RequestParam(required = false) Long asignacionId,
                                     @RequestParam(required = false) String resultado,
                                     @RequestParam(required = false) String fotos,
                                     @RequestParam(required = false) String justificacion,
                                     @RequestParam(required = false) org.springframework.web.multipart.MultipartFile imagen,
                                     RedirectAttributes flash) {
        return views.write(flash, "/entidad/dashboard/confirmar",
                () -> {
                    String guardada = null;
                    try {
                        var urls = new java.util.ArrayList<>(fotosRecepcion.resolver(fotos));
                        if ("RECIBIDA".equals(resultado) && imagen != null && !imagen.isEmpty()) {
                            guardada = imagenesRecepcion.guardar(imagen);
                            urls.add(org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path("/entidad/dashboard/recepciones/fotos/").path(guardada).toUriString());
                        }
                        panelEntidad.confirmarRecepcion(asignacionId, resultado, urls, justificacion);
                    } catch (RuntimeException exception) {
                        if (guardada != null) imagenesRecepcion.descartar(guardada);
                        throw exception;
                    }
                });
    }

    @GetMapping("/entidad/dashboard/recepciones/fotos/{nombre}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> fotoRecepcion(@PathVariable String nombre) {
        var archivo = imagenesRecepcion.buscar(nombre);
        return org.springframework.http.ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .contentType(nombre.endsWith(".png") ? org.springframework.http.MediaType.IMAGE_PNG : org.springframework.http.MediaType.IMAGE_JPEG)
                .body(new org.springframework.core.io.FileSystemResource(archivo));
    }
}

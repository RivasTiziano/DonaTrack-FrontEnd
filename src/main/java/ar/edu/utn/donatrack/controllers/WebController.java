package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.clients.DonantesApiClient;
import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final DonacionesApiService donacionesApi;
    private final DonantesApiClient donantesClient;
    private final LogisticaApiService logisticaApi;
    private final IncentivosApiService incentivosApi;

    public WebController(DonacionesApiService donacionesApi, DonantesApiClient donantesClient,
                         LogisticaApiService logisticaApi, IncentivosApiService incentivosApi) {
        this.donacionesApi = donacionesApi;
        this.donantesClient = donantesClient;
        this.logisticaApi = logisticaApi;
        this.incentivosApi = incentivosApi;
    }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("heroContent", Map.of(
            "kicker", "Donaciones transparentes y verificables",
            "title", "Cada ayuda cuenta, y cada entrega se puede comprobar.",
            "description", "DonaTrack conecta personas donantes con entidades beneficiarias de forma transparente, con trazabilidad de punta a punta y evidencia fotográfica pública.",
            "ctaLabel", "Ver cómo funciona",
            "heroImage", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=900&h=700&fit=crop"
        ));

        model.addAttribute("heroStats", List.of(
            Map.of("value", "100%", "label", "trazabilidad verificada"),
            Map.of("value", "REST", "label", "microservicios conectados"),
            Map.of("value", "UTN", "label", "Diseño de Sistemas")
        ));

        model.addAttribute("landingFeatures", List.of(
            Map.of("title", "Trazabilidad Total", "description", "Seguimiento en tiempo real del estado de cada bien donado desde el depósito hasta su destino final.", "icon", "check-circle"),
            Map.of("title", "Segmentación Inteligente", "description", "Algoritmos de compatibilidad semántica para emparejar donaciones con necesidades reales.", "icon", "users"),
            Map.of("title", "Verificación Fotográfica", "description", "Cada entrega queda documentada con fotos públicas subidas por la entidad receptora.", "icon", "camera")
        ));

        List<Map<String, Object>> featured = new ArrayList<>();
        try {
            var realDonations = donacionesApi.donaciones();
            if (realDonations != null) {
                for (var d : realDonations) {
                    String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Donación");
                    String foto = (d.bien() != null && d.bien().foto() != null && !d.bien().foto().isBlank()) ? d.bien().foto() : "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop";
                    featured.add(Map.of(
                        "id", String.valueOf(d.id()),
                        "item", bienDesc,
                        "donorName", "Donante #" + d.donanteId(),
                        "date", d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : "Reciente",
                        "beneficiary", "Sede Central",
                        "image", foto
                    ));
                    if (featured.size() >= 3) break;
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("featuredDonations", featured);

        model.addAttribute("recentDeliveries", List.of(
            Map.of("title", "Entrega de útiles escolares", "image", "https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=300&h=300&fit=crop"),
            Map.of("title", "Distribución de alimentos", "image", "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=300&h=300&fit=crop"),
            Map.of("title", "Donación de ropa de abrigo", "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=300&h=300&fit=crop")
        ));

        model.addAttribute("footerQuickLinks", List.of(
            Map.of("label", "Acerca de", "href", "/#about"),
            Map.of("label", "Donaciones", "href", "/#donations"),
            Map.of("label", "Galería", "href", "/#gallery")
        ));

        model.addAttribute("footerAccessLinks", List.of(
            Map.of("label", "Iniciar sesión", "href", "/login"),
            Map.of("label", "Registro donantes", "href", "/registro"),
            Map.of("label", "Registro beneficiarios", "href", "/registro/entidad-beneficiaria")
        ));

        model.addAttribute("footerLegalLinks", List.of(
            Map.of("label", "Información legal y de privacidad", "href", "/legal"),
            Map.of("label", "Política de privacidad", "href", "/legal#privacidad"),
            Map.of("label", "Términos del servicio", "href", "/legal#terminos"),
            Map.of("label", "Transparencia pública", "href", "/legal#transparencia")
        ));

        return "landing";
    }

    @GetMapping({"/legal", "/privacidad"})
    public String legal(Model model) {
        model.addAttribute("footerQuickLinks", List.of(
            Map.of("label", "Acerca de", "href", "/#about"),
            Map.of("label", "Donaciones", "href", "/#donations"),
            Map.of("label", "Galería", "href", "/#gallery")
        ));
        model.addAttribute("footerAccessLinks", List.of(
            Map.of("label", "Iniciar sesión", "href", "/login"),
            Map.of("label", "Registro donantes", "href", "/registro"),
            Map.of("label", "Registro beneficiarios", "href", "/registro/entidad-beneficiaria")
        ));
        model.addAttribute("footerLegalLinks", List.of(
            Map.of("label", "Información legal y de privacidad", "href", "/legal"),
            Map.of("label", "Política de privacidad", "href", "/legal#privacidad"),
            Map.of("label", "Términos del servicio", "href", "/legal#terminos"),
            Map.of("label", "Transparencia pública", "href", "/legal#transparencia")
        ));
        return "legal";
    }

    @GetMapping("/explorar-donaciones")
    public String map(Model model) { 
        List<Map<String, Object>> donations = new ArrayList<>();
        try {
            var realDonations = donacionesApi.donaciones();
            if (realDonations != null) {
                double[][] coords = {
                    {-34.5989, -58.4395}, {-34.5622, -58.4561}, {-34.6179, -58.4471},
                    {-34.6172, -58.3714}, {-34.5946, -58.4434}, {-34.6289, -58.4123}
                };
                for (int i = 0; i < realDonations.size(); i++) {
                    var d = realDonations.get(i);
                    String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Donación");
                    String cat = (d.bien() != null && d.bien().categoria() != null && d.bien().categoria().nombreCategoria() != null) ? d.bien().categoria().nombreCategoria() : "General";
                    int units = (d.bien() != null && d.bien().cantidad() != null) ? d.bien().cantidad().intValue() : 1;
                    String foto = (d.bien() != null && d.bien().foto() != null && !d.bien().foto().isBlank()) ? d.bien().foto() : "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop";
                    double lat = coords[i % coords.length][0];
                    double lng = coords[i % coords.length][1];
                    String fecha = d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : "Reciente";

                    donations.add(Map.ofEntries(
                        Map.entry("id", String.valueOf(d.id())),
                        Map.entry("title", bienDesc),
                        Map.entry("entity_name", "Entidad Beneficiaria"),
                        Map.entry("location", Map.of("lat", lat, "lng", lng, "address", "Sede de Entrega")),
                        Map.entry("total_units", units),
                        Map.entry("beneficiaries_count", 50),
                        Map.entry("category", cat),
                        Map.entry("status", d.estado() != null ? d.estado() : "EN_DEPOSITO"),
                        Map.entry("delivery_date", fecha),
                        Map.entry("image_url", foto)
                    ));
                }
            }
        } catch (Exception ignored) {}

        int beneficiariesCount = donations.stream().mapToInt(d -> (Integer) d.getOrDefault("beneficiaries_count", 0)).sum();
        int itemsCount = donations.stream().mapToInt(d -> (Integer) d.getOrDefault("total_units", 0)).sum();

        model.addAttribute("donations", donations);
        model.addAttribute("donationsCount", donations.size());
        model.addAttribute("beneficiariesCount", beneficiariesCount);
        model.addAttribute("itemsCount", itemsCount);

        return "map"; 
    }

    @GetMapping("/explorar-donaciones/{id}")
    public String donationDetail(@PathVariable String id, Model model) {
        try {
            Long numericId = Long.parseLong(id.replaceAll("\\D+", ""));
            var d = donacionesApi.donacion(numericId);
            if (d != null) {
                String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Bienes varios");
                String cant = (d.bien() != null && d.bien().cantidad() != null) ? String.valueOf(d.bien().cantidad()) : "1";
                String unidad = (d.bien() != null && d.bien().unidadMedida() != null) ? d.bien().unidadMedida() : "unidades";
                String foto = (d.bien() != null && d.bien().foto() != null && !d.bien().foto().isBlank()) ? d.bien().foto() : "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop";

                Map<String, Object> donation = Map.ofEntries(
                    Map.entry("id", String.valueOf(d.id())),
                    Map.entry("title", d.descripcionGeneral() != null ? d.descripcionGeneral() : bienDesc),
                    Map.entry("entity_name", "Entidad Beneficiaria"),
                    Map.entry("location", Map.of("address", "Dirección de Entrega")),
                    Map.entry("delivery_date", d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : "Reciente"),
                    Map.entry("total_units", d.bien() != null && d.bien().cantidad() != null ? d.bien().cantidad().intValue() : 1),
                    Map.entry("beneficiaries_count", 50),
                    Map.entry("description", d.descripcionGeneral() != null ? d.descripcionGeneral() : bienDesc),
                    Map.entry("image_url", foto),
                    Map.entry("status", d.estado() != null ? d.estado() : "EN_DEPOSITO"),
                    Map.entry("items", List.of(
                        Map.of("name", bienDesc, "quantity", cant + " " + unidad)
                    ))
                );
                model.addAttribute("donation", donation);
                return "donation-detail";
            }
        } catch (Exception ignored) {}

        model.addAttribute("donation", Map.ofEntries(
            Map.entry("id", id),
            Map.entry("title", "Donación #" + id),
            Map.entry("entity_name", "Entidad Beneficiaria"),
            Map.entry("location", Map.of("address", "Sede Central")),
            Map.entry("delivery_date", "Reciente"),
            Map.entry("total_units", 1),
            Map.entry("beneficiaries_count", 1),
            Map.entry("description", "Detalle informado por el servicio de donaciones."),
            Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop"),
            Map.entry("status", "EN_DEPOSITO"),
            Map.entry("items", List.of())
        ));
        return "donation-detail";
    }

    @GetMapping("/registro")
    public String register(Model model) { 
        List<Map<String, String>> registrationOptions = List.of(
            Map.of(
                "id", "donor-human",
                "title", "Soy una persona donante",
                "description", "Registro como persona humana para aportar bienes materiales",
                "icon", "heart",
                "path", "/registro/donante-humano"
            ),
            Map.of(
                "id", "donor-organization",
                "title", "Soy una organización donante",
                "description", "Registro como empresa, ONG, institución o entidad jurídica",
                "icon", "building-2",
                "path", "/registro/donante-organizacion"
            ),
            Map.of(
                "id", "beneficiary",
                "title", "Soy una entidad beneficiaria",
                "description", "Registro como comedor, escuela rural u hogar para solicitar donaciones",
                "icon", "users",
                "path", "/registro/entidad-beneficiaria"
            )
        );
        model.addAttribute("registrationOptions", registrationOptions);
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
        String lower = email.toLowerCase();
        if (lower.contains("admin")) {
            return "redirect:/admin/dashboard";
        } else if (lower.contains("entidad") || lower.contains("beneficiaria")) {
            return "redirect:/entidad/dashboard";
        } else {
            return "redirect:/donante/dashboard";
        }
    }

    // =========================================================================
    // DONANTE DASHBOARD
    // =========================================================================
    @GetMapping("/donante/dashboard")
    public String donorDashboard(Model model) {
        populateDonorModel(model);
        return "dashboard-donor";
    }

    @GetMapping("/donante/dashboard/donaciones")
    public String donorDonations(Model model) {
        populateDonorModel(model);
        return "dashboard-donor-donaciones";
    }

    @GetMapping("/donante/dashboard/entidades")
    public String donorEntities(Model model) {
        populateDonorModel(model);
        return "dashboard-donor-entidades";
    }

    @GetMapping("/donante/dashboard/incentivos")
    public String donorIncentives(Model model) {
        populateDonorModel(model);
        return "dashboard-donor-incentivos";
    }

    @GetMapping("/donante/dashboard/entregas")
    public String donorDeliveries(Model model) {
        populateDonorModel(model);
        return "dashboard-donor-entregas";
    }


    private void populateDonorModel(Model model) {
        String donorName = "Donante";
        String donorEmail = "contacto@donatrack.org";
        String donorDoc = "—";
        String donorType = "Humana";
        String donorCategory = "Iniciador Bronce";

        try {
            var humanos = donantesClient.listarHumanos();
            if (humanos != null && !humanos.isEmpty()) {
                var h = humanos.get(0);
                donorName = (h.nombre() != null ? h.nombre() : "") + " " + (h.apellido() != null ? h.apellido() : "");
                donorDoc = h.numeroDocumento() != null ? h.numeroDocumento() : "—";
                donorType = "Humana";
                if (h.mediosDeContacto() != null && !h.mediosDeContacto().isEmpty()) {
                    donorEmail = h.mediosDeContacto().get(0).formaContacto();
                }
            } else {
                var juridicos = donantesClient.listarJuridicos();
                if (juridicos != null && !juridicos.isEmpty()) {
                    var j = juridicos.get(0);
                    donorName = j.razonSocial();
                    donorDoc = j.numeroDocumento() != null ? j.numeroDocumento() : "—";
                    donorType = "Jurídica";
                    if (j.mediosDeContacto() != null && !j.mediosDeContacto().isEmpty()) {
                        donorEmail = j.mediosDeContacto().get(0).formaContacto();
                    }
                }
            }
        } catch (Exception ignored) {}

        try {
            var rankings = incentivosApi.ranking();
            if (rankings != null && !rankings.isEmpty()) {
                donorCategory = "Sostenedor Plata";
            }
        } catch (Exception ignored) {}

        model.addAttribute("user", mapOf(
            "name", donorName.trim().isEmpty() ? "Donante" : donorName.trim(),
            "email", donorEmail,
            "category", donorCategory,
            "type", donorType,
            "document", donorDoc
        ));
        
        // 1. Historial de Donaciones desde la API real
        List<Map<String, Object>> userDonations = new ArrayList<>();
        try {
            var realDonations = donacionesApi.donaciones();
            if (realDonations != null) {
                for (var d : realDonations) {
                    String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Bien donado");
                    String cat = (d.bien() != null && d.bien().categoria() != null && d.bien().categoria().nombreCategoria() != null) ? d.bien().categoria().nombreCategoria() : "General";
                    String subcat = (d.bien() != null && d.bien().categoria() != null && d.bien().categoria().subCategoria() != null && d.bien().categoria().subCategoria().nombreSubCategoria() != null) ? d.bien().categoria().subCategoria().nombreSubCategoria() : "General";
                    String tipo = (d.bien() != null && d.bien().tipoBien() != null) ? d.bien().tipoBien() : "Durable";
                    int units = (d.bien() != null && d.bien().cantidad() != null) ? d.bien().cantidad().intValue() : 1;
                    String unit = (d.bien() != null && d.bien().unidadMedida() != null) ? d.bien().unidadMedida().toLowerCase() : "unidades";
                    String foto = (d.bien() != null && d.bien().foto() != null && !d.bien().foto().isBlank()) ? d.bien().foto() : "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop";
                    String estado = d.estado() != null ? d.estado() : "EN_DEPOSITO";
                    String fecha = d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : "Reciente";

                    userDonations.add(mapOf(
                        "id", String.valueOf(d.id()),
                        "title", bienDesc,
                        "category", cat,
                        "subcategory", subcat,
                        "item_type", tipo,
                        "entity_name", "Depósito Central",
                        "entity_id", "e1",
                        "status", estado,
                        "delivery_date", fecha,
                        "total_units", units,
                        "unit", unit,
                        "image_url", foto,
                        "evidence_photos", List.of(foto)
                    ));
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("userDonations", userDonations);

        // 2. Directorio Detallado de Entidades Beneficiarias desde la API real
        List<Map<String, Object>> beneficiaryEntities = new ArrayList<>();
        try {
            var realBeneficiaries = donacionesApi.beneficiarios();
            if (realBeneficiaries != null) {
                for (var b : realBeneficiaries) {
                    beneficiaryEntities.add(mapOf(
                        "id", String.valueOf(b.id()),
                        "name", b.razonSocial(),
                        "cuit", "CUIT-" + b.id(),
                        "type", b.tipoEntidad() != null ? b.tipoEntidad() : "ONG",
                        "category", b.rubro() != null ? b.rubro() : "Comunidad",
                        "rubro", b.rubro() != null ? b.rubro() : "Asistencia Comunitaria",
                        "description", "Entidad beneficiaria verificada en la plataforma DonaTrack.",
                        "verified", b.activo(),
                        "contactsCount", 1,
                        "rating", 5.0,
                        "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=400&h=300&fit=crop",
                        "location", mapOf(
                            "address", "Sede Institucional",
                            "calle", "Sede", "numero", "100", "ciudad", "CABA", "provincia", "Buenos Aires",
                            "lat", -34.5989, "lng", -58.4395
                        ),
                        "representatives", List.of(),
                        "contactChannels", List.of(
                            mapOf("type", "EMAIL", "value", "contacto@" + b.razonSocial().toLowerCase().replaceAll("[^a-z0-9]", "") + ".org", "isDefault", true)
                        ),
                        "activeNeeds", List.of(),
                        "recentDonations", List.of()
                    ));
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("beneficiaryEntities", beneficiaryEntities);

        // 3. Sistema de Incentivos
        List<Map<String, Object>> incentiveCategories = List.of(
            mapOf("id", 1, "nombre", "Iniciador Bronce", "nivel", 1, "status", "Completada", "misionesTotal", 3, "misionesCumplidas", 3, "descripcion", "Primeros pasos en la red solidaria."),
            mapOf("id", 2, "nombre", "Sostenedor Plata", "nivel", 2, "status", "Actual", "misionesTotal", 4, "misionesCumplidas", 3, "descripcion", "Donante recurrente con compromiso activo."),
            mapOf("id", 3, "nombre", "Protector Oro", "nivel", 3, "status", "Siguiente Nivel", "misionesTotal", 4, "misionesCumplidas", 0, "descripcion", "Alto impacto comunitario y diversidad de rubros."),
            mapOf("id", 4, "nombre", "Embajador Platino", "nivel", 4, "status", "Bloqueada", "misionesTotal", 5, "misionesCumplidas", 0, "descripcion", "Líder solidario referente de la plataforma.")
        );
        model.addAttribute("incentiveCategories", incentiveCategories);

        List<Map<String, Object>> missions = List.of(
            mapOf(
                "id", 201,
                "orden", 1,
                "categoriaId", 2,
                "categoriaNombre", "Sostenedor Plata",
                "tipo", "MisionRacha",
                "tipoNombre", "Racha Consecutiva",
                "tipoLabel", "Racha Mensual Solidaria",
                "tipoBadgeClass", "badge-racha",
                "tipoIcon", "flame",
                "nombre", "Constancia Solidaria (3 Meses)",
                "descripcion", "Realizar al menos una donación mensual durante 3 meses consecutivos sin interrumpir la racha.",
                "valorObjetivo", 3,
                "valorActual", Math.min(3, userDonations.size()),
                "unidad", "meses consecutivos",
                "progresoPorcentaje", Math.min(100, (userDonations.size() * 100) / 3),
                "completada", userDonations.size() >= 3,
                "fechaCompletada", userDonations.size() >= 3 ? "Completada" : "En progreso",
                "insigniaPremio", mapOf("nombre", "Llama Solidaria", "descripcion", "Racha de donaciones consecutivas", "icon", "flame", "color", "#f97316")
            )
        );
        model.addAttribute("missions", missions);

        List<Map<String, Object>> badges = List.of(
            mapOf("id", "b1", "name", "Primer Paso Solidario", "description", "Realizaste tu primera donación física verificada", "earnedDate", "Verificada", "icon", "star", "status", userDonations.isEmpty() ? "Bloqueada" : "Obtenida", "isPublic", !userDonations.isEmpty())
        );
        model.addAttribute("badges", badges);

        List<Map<String, Object>> categoryAudits = List.of(
            mapOf("fecha", "Reciente", "categoriaAnterior", "Registro Inicial", "categoriaNueva", donorCategory, "motivo", "Evaluación automática del Servicio de Incentivos")
        );
        model.addAttribute("categoryAudits", categoryAudits);

        Map<String, Object> donorMetrics = mapOf(
            "totalDonacionesHistoricas", userDonations.size(),
            "impactoAcumulado", userDonations.size() * 30,
            "organizacionesAyudadas", beneficiaryEntities.size(),
            "rachaMeses", userDonations.isEmpty() ? 0 : 1,
            "puestoRankingMes", 1
        );
        model.addAttribute("donorMetrics", donorMetrics);

        // 4. Notificaciones
        List<Map<String, Object>> notifications = List.of();
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", 0);

        // 5. Entregas Activas desde Logística real
        List<Map<String, Object>> activeDonorDeliveries = new ArrayList<>();
        try {
            var realDeliveries = logisticaApi.entregas();
            if (realDeliveries != null) {
                for (var e : realDeliveries) {
                    activeDonorDeliveries.add(mapOf(
                        "id", "del-" + e.id(),
                        "donationId", String.valueOf(e.asignacionId()),
                        "donationTitle", "Entrega #" + e.id(),
                        "category", "Logística",
                        "destinationEntity", "Sede Beneficiaria",
                        "destinationAddress", "Destino Asignado",
                        "destinationCoords", mapOf("lat", -34.5989, "lng", -58.4395),
                        "status", e.estado() != null ? e.estado() : "EN_TRASLADO",
                        "eta", "En curso",
                        "totalDistanceKm", 5.0,
                        "progressPercent", 50,
                        "truck", mapOf(
                            "plate", e.camionPatente() != null ? e.camionPatente() : "CAMION-" + e.camionId(),
                            "model", "Unidad de Transporte",
                            "driver", "Chofer Asignado",
                            "driverPhone", "—",
                            "capacityKg", 5000,
                            "currentLoadKg", 2500,
                            "gps", mapOf(
                                "lat", -34.6050,
                                "lng", -58.4250,
                                "speed", 40.0,
                                "lastUpdate", "En ruta"
                            )
                        ),
                        "stops", List.of(
                            mapOf("orden", 1, "direccion", "Depósito Central", "hora", "Salida", "completada", true),
                            mapOf("orden", 2, "direccion", "Sede Beneficiaria", "hora", "Llegada", "completada", false)
                        )
                    ));
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("activeDonorDeliveries", activeDonorDeliveries);
    }

    // =========================================================================
    // ENTIDAD BENEFICIARIA DASHBOARD
    // =========================================================================
    @GetMapping("/entidad/dashboard")
    public String beneficiaryDashboard(Model model) {
        populateBeneficiaryModel(model);
        return "dashboard-beneficiary";
    }

    @GetMapping("/entidad/dashboard/necesidades")
    public String beneficiaryNeeds(Model model) {
        populateBeneficiaryModel(model);
        return "dashboard-beneficiary-necesidades";
    }

    @GetMapping("/entidad/dashboard/donaciones")
    public String beneficiaryDonations(Model model) {
        populateBeneficiaryModel(model);
        return "dashboard-beneficiary-donaciones";
    }

    @GetMapping("/entidad/dashboard/confirmar")
    public String beneficiaryConfirm(Model model) {
        populateBeneficiaryModel(model);
        return "dashboard-beneficiary-confirmar";
    }

    @GetMapping("/entidad/dashboard/entregas")
    public String beneficiaryDeliveries(Model model) {
        populateBeneficiaryModel(model);
        return "dashboard-beneficiary-entregas";
    }


    private void populateBeneficiaryModel(Model model) {
        String entityName = "Entidad Beneficiaria";
        String entityCuit = "—";
        String entityEmail = "contacto@entidad.org";

        try {
            var beneficiaries = donacionesApi.beneficiarios();
            if (beneficiaries != null && !beneficiaries.isEmpty()) {
                var b = beneficiaries.get(0);
                entityName = b.razonSocial();
                entityCuit = "CUIT-" + b.id();
                entityEmail = "contacto@" + b.razonSocial().toLowerCase().replaceAll("[^a-z0-9]", "") + ".org";
            }
        } catch (Exception ignored) {}

        model.addAttribute("user", mapOf(
            "name", entityName,
            "cuit", entityCuit,
            "address", "Sede Comunitaria",
            "representative", "Responsable Institucional",
            "email", entityEmail
        ));
        
        List<Map<String, Object>> needs = new ArrayList<>();
        try {
            var realNeeds = donacionesApi.necesidades();
            if (realNeeds != null) {
                for (var n : realNeeds) {
                    String desc = n.descripcion() != null ? n.descripcion() : "Necesidad de insumos";
                    String cat = (n.bien() != null && n.bien().categoria() != null && n.bien().categoria().nombreCategoria() != null) ? n.bien().categoria().nombreCategoria() : "General";
                    String subcat = (n.bien() != null && n.bien().categoria() != null && n.bien().categoria().subCategoria() != null && n.bien().categoria().subCategoria().nombreSubCategoria() != null) ? n.bien().categoria().subCategoria().nombreSubCategoria() : "General";
                    int qty = (n.bien() != null && n.bien().cantidad() != null) ? n.bien().cantidad().intValue() : 100;
                    String unit = (n.bien() != null && n.bien().unidadMedida() != null) ? n.bien().unidadMedida().toLowerCase() : "unidades";
                    int prog = (n.cantidadRecibida() != null && n.bien() != null && n.bien().cantidad() != null && n.bien().cantidad() > 0)
                            ? Math.min(100, Math.round((n.cantidadRecibida() / n.bien().cantidad()) * 100)) : 0;

                    needs.add(Map.ofEntries(
                        Map.entry("id", String.valueOf(n.necesidadId())),
                        Map.entry("title", desc),
                        Map.entry("type", n.tipoNecesidad() != null ? n.tipoNecesidad() : "Recurrente"),
                        Map.entry("subcategory", subcat),
                        Map.entry("category", cat),
                        Map.entry("description", desc),
                        Map.entry("priority", "Alta"),
                        Map.entry("status", n.estado() != null ? n.estado() : "Activa"),
                        Map.entry("quantity", qty),
                        Map.entry("unit", unit),
                        Map.entry("deadline", n.periodo() != null ? n.periodo() : "Periódico"),
                        Map.entry("progress", prog),
                        Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=200&h=150&fit=crop")
                    ));
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("needs", needs);

        List<Map<String, Object>> assignedDonations = new ArrayList<>();
        try {
            var realDonations = donacionesApi.donaciones();
            if (realDonations != null) {
                for (var d : realDonations) {
                    if ("ASIGNACION_REALIZADA".equalsIgnoreCase(d.estado()) || "EN_TRASLADO".equalsIgnoreCase(d.estado())) {
                        String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Donación asignada");
                        int count = (d.bien() != null && d.bien().cantidad() != null) ? d.bien().cantidad().intValue() : 1;
                        assignedDonations.add(mapOf(
                            "id", "d" + d.id(),
                            "title", bienDesc,
                            "donor_name", "Donante #" + d.donanteId(),
                            "status", d.estado(),
                            "items_count", count,
                            "expected_delivery", "En proceso",
                            "driver", "Chofer Asignado",
                            "truck_plate", "Flota Central"
                        ));
                    }
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("assignedDonations", assignedDonations);

        List<Map<String, Object>> beneficiaryNotifications = List.of();
        model.addAttribute("beneficiaryNotifications", beneficiaryNotifications);

        List<Map<String, Object>> incomingDeliveries = new ArrayList<>();
        try {
            var realDeliveries = logisticaApi.entregas();
            if (realDeliveries != null) {
                for (var e : realDeliveries) {
                    incomingDeliveries.add(mapOf(
                        "id", "inc-" + e.id(),
                        "donationId", "D-" + e.asignacionId(),
                        "title", "Partida #" + e.id(),
                        "donor", "Red DonaTrack",
                        "category", "Logística",
                        "isPerishable", false,
                        "goods", List.of(),
                        "handlingNotes", "Recepción institucional en sede.",
                        "status", e.estado() != null ? e.estado() : "EN_TRASLADO",
                        "urgency", "En tránsito",
                        "isImminent", false,
                        "eta", "Hoy",
                        "etaMinutes", 30,
                        "truck", mapOf(
                            "plate", e.camionPatente() != null ? e.camionPatente() : "CAMION-" + e.camionId(),
                            "model", "Camión de Logística",
                            "driver", "Chofer",
                            "driverPhone", "—",
                            "currentSpeed", 40.0,
                            "lastPing", "En ruta",
                            "coords", mapOf("lat", -34.6050, "lng", -58.4250)
                        )
                    ));
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("incomingDeliveries", incomingDeliveries);
    }

    private static Map<String, Object> mapOf(Object... kvs) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((String) kvs[i], kvs[i + 1]);
        }
        return map;
    }
}

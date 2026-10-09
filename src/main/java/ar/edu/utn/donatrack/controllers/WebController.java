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
            Map.of("value", "2,847", "label", "donaciones verificadas"),
            Map.of("value", "1,234", "label", "familias impactadas"),
            Map.of("value", "98%", "label", "satisfacción reportada")
        ));

        model.addAttribute("landingFeatures", List.of(
            Map.of("title", "Trazabilidad Total", "description", "Seguimiento en tiempo real del estado de cada bien donado desde el depósito hasta su destino final.", "icon", "check-circle"),
            Map.of("title", "Segmentación Inteligente", "description", "Algoritmos de compatibilidad semántica para emparejar donaciones con necesidades reales.", "icon", "users"),
            Map.of("title", "Verificación Fotográfica", "description", "Cada entrega queda documentada con fotos públicas subidas por la entidad receptora.", "icon", "camera")
        ));

        model.addAttribute("featuredDonations", List.of(
            Map.of("id", "donation-1", "item", "Ropa de invierno y frazadas", "donorName", "María González", "date", "15 de marzo, 2026", "beneficiary", "Centro Comunitario Norte", "image", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&h=300&fit=crop"),
            Map.of("id", "donation-2", "item", "Útiles escolares y mochilas", "donorName", "Carlos Ramírez", "date", "20 de marzo, 2026", "beneficiary", "Escuela Primaria La Esperanza", "image", "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=400&h=300&fit=crop"),
            Map.of("id", "donation-3", "item", "Alimentos no perecederos (Arroz y Legumbres)", "donorName", "Ana López", "date", "28 de marzo, 2026", "beneficiary", "Comedor Social San José", "image", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop")
        ));

        model.addAttribute("recentDeliveries", List.of(
            Map.of("title", "Entrega de útiles escolares", "image", "https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=300&h=300&fit=crop"),
            Map.of("title", "Distribución de alimentos", "image", "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=300&h=300&fit=crop"),
            Map.of("title", "Donación de ropa de abrigo", "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=300&h=300&fit=crop"),
            Map.of("title", "Mobiliario para escuela rural", "image", "https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=300&h=300&fit=crop")
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
            if (realDonations != null && !realDonations.isEmpty()) {
                double[][] coords = {
                    {-34.5989, -58.4395}, {-34.5622, -58.4561}, {-34.6179, -58.4471},
                    {-34.6172, -58.3714}, {-34.5946, -58.4434}, {-34.6289, -58.4123}
                };
                for (int i = 0; i < realDonations.size(); i++) {
                    var d = realDonations.get(i);
                    String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Donación");
                    String cat = (d.bien() != null && d.bien().categoria() != null && d.bien().categoria().nombreCategoria() != null) ? d.bien().categoria().nombreCategoria() : "General";
                    int units = (d.bien() != null && d.bien().cantidad() != null) ? d.bien().cantidad().intValue() : 50;
                    String foto = (d.bien() != null && d.bien().foto() != null && !d.bien().foto().isBlank()) ? d.bien().foto() : "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop";
                    double lat = coords[i % coords.length][0];
                    double lng = coords[i % coords.length][1];
                    String fecha = d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : "Reciente";

                    donations.add(Map.ofEntries(
                        Map.entry("id", String.valueOf(d.id())),
                        Map.entry("title", bienDesc),
                        Map.entry("entity_name", "Fundación Despierta"),
                        Map.entry("location", Map.of("lat", lat, "lng", lng, "address", "Av. Corrientes 4500, CABA")),
                        Map.entry("total_units", units),
                        Map.entry("beneficiaries_count", 150),
                        Map.entry("category", cat),
                        Map.entry("status", d.estado() != null ? d.estado() : "EN_DEPOSITO"),
                        Map.entry("delivery_date", fecha),
                        Map.entry("image_url", foto)
                    ));
                }
            }
        } catch (Exception ignored) {}

        if (donations.isEmpty()) {
            donations = List.of(
                Map.ofEntries(
                    Map.entry("id", "donation-1"),
                    Map.entry("title", "Donación de Alimentos - Fundación Despierta"),
                    Map.entry("entity_name", "Fundación Despierta"),
                    Map.entry("location", Map.of("lat", -34.5989, "lng", -58.4395, "address", "Av. Corrientes 4500, CABA")),
                    Map.entry("total_units", 125),
                    Map.entry("beneficiaries_count", 150),
                    Map.entry("category", "Alimentos"),
                    Map.entry("status", "Entregada"),
                    Map.entry("delivery_date", "12 de marzo de 2026"),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop")
                ),
                Map.ofEntries(
                    Map.entry("id", "donation-2"),
                    Map.entry("title", "Donación de Abrigo - Red Solidaria Norte"),
                    Map.entry("entity_name", "Red Solidaria Norte"),
                    Map.entry("location", Map.of("lat", -34.5622, "lng", -58.4561, "address", "Juramento 2100, Belgrano")),
                    Map.entry("total_units", 130),
                    Map.entry("beneficiaries_count", 95),
                    Map.entry("category", "Vestimenta"),
                    Map.entry("status", "Entregada"),
                    Map.entry("delivery_date", "10 de marzo de 2026"),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&h=300&fit=crop")
                ),
                Map.ofEntries(
                    Map.entry("id", "donation-3"),
                    Map.entry("title", "Donación Escolar - Escuela Puentes"),
                    Map.entry("entity_name", "Escuela Puentes"),
                    Map.entry("location", Map.of("lat", -34.6179, "lng", -58.4471, "address", "Av. Rivadavia 5800, Caballito")),
                    Map.entry("total_units", 230),
                    Map.entry("beneficiaries_count", 180),
                    Map.entry("category", "Educación"),
                    Map.entry("status", "Entregada"),
                    Map.entry("delivery_date", "08 de marzo de 2026"),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=400&h=300&fit=crop")
                ),
                Map.ofEntries(
                    Map.entry("id", "donation-4"),
                    Map.entry("title", "Donación de Alimentos - Comedor San Telmo"),
                    Map.entry("entity_name", "Comedor San Telmo"),
                    Map.entry("location", Map.of("lat", -34.6172, "lng", -58.3714, "address", "Defensa 1100, San Telmo")),
                    Map.entry("total_units", 160),
                    Map.entry("beneficiaries_count", 210),
                    Map.entry("category", "Alimentos"),
                    Map.entry("status", "Entregada"),
                    Map.entry("delivery_date", "05 de marzo de 2026"),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop")
                ),
                Map.ofEntries(
                    Map.entry("id", "donation-5"),
                    Map.entry("title", "Donación Mobiliario - Centro Nueva Esperanza"),
                    Map.entry("entity_name", "Centro Nueva Esperanza"),
                    Map.entry("location", Map.of("lat", -34.5946, "lng", -58.4434, "address", "Warnes 700, Villa Crespo")),
                    Map.entry("total_units", 205),
                    Map.entry("beneficiaries_count", 140),
                    Map.entry("category", "Mobiliario"),
                    Map.entry("status", "Entregada"),
                    Map.entry("delivery_date", "01 de marzo de 2026"),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1503602642458-232111445657?w=400&h=300&fit=crop")
                )
            );
        }

        int beneficiariesCount = donations.stream().mapToInt(d -> (Integer) d.get("beneficiaries_count")).sum();
        int itemsCount = donations.stream().mapToInt(d -> (Integer) d.get("total_units")).sum();

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
                    Map.entry("entity_name", "Fundación Despierta"),
                    Map.entry("location", Map.of("address", "Av. Corrientes 4500, Villa Crespo, CABA")),
                    Map.entry("delivery_date", d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : "Reciente"),
                    Map.entry("total_units", d.bien() != null && d.bien().cantidad() != null ? d.bien().cantidad().intValue() : 1),
                    Map.entry("beneficiaries_count", 150),
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

        Map<String, Object> donation = Map.ofEntries(
            Map.entry("id", id),
            Map.entry("title", "Donación de Alimentos - Fundación Despierta"),
            Map.entry("entity_name", "Fundación Despierta"),
            Map.entry("location", Map.of("address", "Av. Corrientes 4500, Villa Crespo, CABA")),
            Map.entry("delivery_date", "12 de marzo de 2026"),
            Map.entry("total_units", 125),
            Map.entry("beneficiaries_count", 150),
            Map.entry("description", "La entrega fue organizada para abastecer el comedor comunitario del barrio."),
            Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop"),
            Map.entry("status", "Entregada"),
            Map.entry("items", List.of(
                Map.of("name", "Fideos Secos", "quantity", "50 kg"),
                Map.of("name", "Arroz Blanco", "quantity", "40 kg"),
                Map.of("name", "Lentejas Secas", "quantity", "35 kg")
            ))
        );
        model.addAttribute("donation", donation);
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

    @GetMapping("/donante/dashboard/notificaciones")
    public String donorNotifications(Model model) {
        populateDonorModel(model);
        return "dashboard-donor-notificaciones";
    }

    private void populateDonorModel(Model model) {
        String donorName = "Juan Pérez";
        String donorEmail = "juan.perez@donatrack.org";
        String donorDoc = "38450123";
        String donorType = "Humana";
        String donorCategory = "Sostenedor Plata";

        try {
            var humanos = donantesClient.listarHumanos();
            if (humanos != null && !humanos.isEmpty()) {
                var h = humanos.get(0);
                donorName = (h.nombre() != null ? h.nombre() : "") + " " + (h.apellido() != null ? h.apellido() : "");
                donorDoc = h.numeroDocumento() != null ? h.numeroDocumento() : donorDoc;
                donorType = "Humana";
                if (h.mediosDeContacto() != null && !h.mediosDeContacto().isEmpty()) {
                    donorEmail = h.mediosDeContacto().get(0).formaContacto();
                }
            } else {
                var juridicos = donantesClient.listarJuridicos();
                if (juridicos != null && !juridicos.isEmpty()) {
                    var j = juridicos.get(0);
                    donorName = j.razonSocial();
                    donorDoc = j.numeroDocumento();
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
            "name", donorName.trim().isEmpty() ? "Juan Pérez" : donorName,
            "email", donorEmail,
            "category", donorCategory,
            "type", donorType,
            "document", donorDoc
        ));
        
        // 1. Historial de Donaciones desde la API real
        List<Map<String, Object>> userDonations = new ArrayList<>();
        try {
            var realDonations = donacionesApi.donaciones();
            if (realDonations != null && !realDonations.isEmpty()) {
                for (var d : realDonations) {
                    String bienDesc = (d.bien() != null && d.bien().descripcion() != null) ? d.bien().descripcion() : (d.descripcionGeneral() != null ? d.descripcionGeneral() : "Bien donado");
                    String cat = (d.bien() != null && d.bien().categoria() != null && d.bien().categoria().nombreCategoria() != null) ? d.bien().categoria().nombreCategoria() : "General";
                    String subcat = (d.bien() != null && d.bien().categoria() != null && d.bien().categoria().subCategoria() != null && d.bien().categoria().subCategoria().nombreSubCategoria() != null) ? d.bien().categoria().subCategoria().nombreSubCategoria() : "General";
                    String tipo = (d.bien() != null && d.bien().tipoBien() != null) ? d.bien().tipoBien() : "Durable";
                    int units = (d.bien() != null && d.bien().cantidad() != null) ? d.bien().cantidad().intValue() : 10;
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

        if (userDonations.isEmpty()) {
            userDonations = List.of(
                mapOf(
                    "id", "101",
                    "title", "Camperas Térmicas y Ropa de Invierno",
                    "category", "Vestimenta",
                    "subcategory", "Camperas de abrigo",
                    "item_type", "Durable",
                    "entity_name", "Comedor Los Niños",
                    "entity_id", "e3",
                    "status", "Entregada",
                    "delivery_date", "10 Mar 2026",
                    "total_units", 25,
                    "unit", "unidades",
                    "image_url", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&h=300&fit=crop",
                    "evidence_photos", List.of("https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=300&h=200&fit=crop", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=300&h=200&fit=crop")
                ),
                mapOf(
                    "id", "102",
                    "title", "Fideos Secos Matarazzo y Harina 000",
                    "category", "Alimentos",
                    "subcategory", "Fideos secos",
                    "item_type", "Perecedero",
                    "entity_name", "Fundación Despierta",
                    "entity_id", "e1",
                    "status", "En traslado",
                    "delivery_date", "Hoy, 15:30",
                    "total_units", 50,
                    "unit", "kg",
                    "truck_plate", "AB 123 CD",
                    "image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop"
                )
            );
        }
        model.addAttribute("userDonations", userDonations);

        // 2. Directorio Detallado de Entidades Beneficiarias desde la API real
        List<Map<String, Object>> beneficiaryEntities = new ArrayList<>();
        try {
            var realBeneficiaries = donacionesApi.beneficiarios();
            if (realBeneficiaries != null && !realBeneficiaries.isEmpty()) {
                for (var b : realBeneficiaries) {
                    beneficiaryEntities.add(mapOf(
                        "id", String.valueOf(b.id()),
                        "name", b.razonSocial(),
                        "cuit", "30-71458923-4",
                        "type", b.tipoEntidad() != null ? b.tipoEntidad() : "ONG",
                        "category", b.rubro() != null ? b.rubro() : "Comunidad",
                        "rubro", b.rubro() != null ? b.rubro() : "Asistencia Comunitaria",
                        "description", "Entidad beneficiaria verificada en la plataforma DonaTrack.",
                        "verified", b.activo(),
                        "contactsCount", 120,
                        "rating", 4.9,
                        "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=400&h=300&fit=crop",
                        "location", mapOf(
                            "address", "Av. Corrientes 4500, Almagro, CABA",
                            "calle", "Av. Corrientes", "numero", "4500", "ciudad", "CABA", "provincia", "Buenos Aires",
                            "lat", -34.5989, "lng", -58.4395
                        ),
                        "representatives", List.of(
                            mapOf("nombre", "Laura Domínguez", "cargo", "Directora Ejecutiva", "telefono", "+54 11 4862-1100")
                        ),
                        "contactChannels", List.of(
                            mapOf("type", "EMAIL", "value", "contacto@fundaciondespierta.org", "isDefault", true)
                        ),
                        "activeNeeds", List.of(),
                        "recentDonations", List.of()
                    ));
                }
            }
        } catch (Exception ignored) {}

        if (beneficiaryEntities.isEmpty()) {
            beneficiaryEntities = List.of(
                mapOf(
                    "id", "e1",
                    "name", "Fundación Despierta",
                    "cuit", "30-71458923-4",
                    "type", "ONG",
                    "category", "Comedor Comunitario",
                    "rubro", "Nutrición infantil y asistencia alimentaria",
                    "description", "Brindamos asistencia nutricional, apoyo escolar y talleres comunitarios a más de 200 niños y sus familias.",
                    "verified", true,
                    "contactsCount", 1250,
                    "rating", 4.9,
                    "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=400&h=300&fit=crop",
                    "location", mapOf(
                        "address", "Av. Corrientes 4500, Almagro, CABA",
                        "calle", "Av. Corrientes", "numero", "4500", "ciudad", "CABA", "provincia", "Buenos Aires",
                        "lat", -34.5989, "lng", -58.4395
                    ),
                    "representatives", List.of(
                        mapOf("nombre", "Laura Domínguez", "cargo", "Directora Ejecutiva", "telefono", "+54 11 4862-1100")
                    ),
                    "contactChannels", List.of(
                        mapOf("type", "EMAIL", "value", "contacto@fundaciondespierta.org", "isDefault", true),
                        mapOf("type", "WHATSAPP", "value", "+54 9 11 5544-3322", "isDefault", false)
                    ),
                    "activeNeeds", List.of(
                        mapOf("title", "Alimentos no perecederos (Fideos y Arroz)", "type", "Recurrente", "target", 150, "current", 90, "unit", "kg", "urgency", "Alta")
                    ),
                    "recentDonations", List.of()
                ),
                mapOf(
                    "id", "e2",
                    "name", "Escuela Rural N°10 'Alas de Futuro'",
                    "cuit", "30-68994512-1",
                    "type", "INSTITUCION",
                    "category", "Institución Educativa",
                    "rubro", "Educación rural y jornada extendida",
                    "description", "Escuela rural de jornada completa con comedor escolar, huerta educativa y biblioteca comunitaria.",
                    "verified", true,
                    "contactsCount", 480,
                    "rating", 4.8,
                    "image", "https://images.unsplash.com/photo-1509062522246-3755977927d7?w=400&h=300&fit=crop",
                    "location", mapOf(
                        "address", "Ruta Prov. 41 Km 12, Navarro, Buenos Aires",
                        "calle", "Ruta Provincial 41", "numero", "Km 12", "ciudad", "Navarro", "provincia", "Buenos Aires",
                        "lat", -34.9812, "lng", -59.2745
                    ),
                    "representatives", List.of(
                        mapOf("nombre", "Prof. María Elena Walsh", "cargo", "Directora de Escuela", "telefono", "+54 2272 45-2311")
                    ),
                    "contactChannels", List.of(
                        mapOf("type", "EMAIL", "value", "escuela10.navarro@abc.gob.ar", "isDefault", true)
                    ),
                    "activeNeeds", List.of(),
                    "recentDonations", List.of()
                )
            );
        }
        model.addAttribute("beneficiaryEntities", beneficiaryEntities);

        // 3. Sistema Completo de Incentivos
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
                "valorActual", 3,
                "unidad", "meses consecutivos",
                "progresoPorcentaje", 100,
                "completada", true,
                "fechaCompletada", "01/03/2026",
                "insigniaPremio", mapOf("nombre", "Llama Solidaria", "descripcion", "Racha de 3 meses consecutivos de donaciones", "icon", "flame", "color", "#f97316")
            ),
            mapOf(
                "id", 202,
                "orden", 2,
                "categoriaId", 2,
                "categoriaNombre", "Sostenedor Plata",
                "tipo", "MisionDonacionesExitosas",
                "tipoLabel", "Volumen de Entregas Efectivas",
                "tipoBadgeClass", "badge-entregas",
                "tipoIcon", "package-check",
                "nombre", "Compromiso Comprobado (5 Entregas)",
                "descripcion", "Alcanzar 5 donaciones recibidas por entidades beneficiarias con evidencia fotográfica pública aprobada.",
                "valorObjetivo", 5,
                "valorActual", 5,
                "unidad", "entregas con foto",
                "progresoPorcentaje", 100,
                "completada", true,
                "fechaCompletada", "10/03/2026",
                "insigniaPremio", mapOf("nombre", "Sello de Confianza", "descripcion", "5 donaciones entregadas y verificadas", "icon", "award", "color", "#059669")
            )
        );
        model.addAttribute("missions", missions);

        List<Map<String, Object>> badges = List.of(
            mapOf("id", "b1", "name", "Primer Paso Solidario", "description", "Realizaste tu primera donación física verificada en depósito", "earnedDate", "12/01/2026", "icon", "star", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b2", "name", "Llama Solidaria", "description", "Racha ininterrumpida de 3 meses continuos realizando donaciones", "earnedDate", "01/03/2026", "icon", "flame", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b3", "name", "Sello de Confianza", "description", "5 donaciones con entrega y recepción fotográfica validada", "earnedDate", "10/03/2026", "icon", "award", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b4", "name", "Guardián del Invierno", "description", "Abasteciste al 100% una necesidad extraordinaria de abrigo escolar", "earnedDate", "10/03/2026", "icon", "snowflake", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b5", "name", "Puente Solidario", "description", "Apoya a entidades de 3 rubros diferentes (Comedor, Escuela, Hogar)", "earnedDate", "Por desbloquear (Misión #4)", "icon", "heart-handshake", "status", "Bloqueada", "isPublic", false),
            mapOf("id", "b6", "name", "Corona de Oro Solidaria", "description", "Alcanza la categoría Protector Oro y ayuda a 5 instituciones", "earnedDate", "Por desbloquear (Nivel Oro)", "icon", "crown", "status", "Bloqueada", "isPublic", false)
        );
        model.addAttribute("badges", badges);

        List<Map<String, Object>> categoryAudits = List.of(
            mapOf("fecha", "15 Feb 2026, 11:30", "categoriaAnterior", "Iniciador Bronce", "categoriaNueva", "Sostenedor Plata", "motivo", "Completó las 3 misiones de iniciación y alcanzó 4 donaciones efectivas"),
            mapOf("fecha", "12 Ene 2026, 09:00", "categoriaAnterior", "Registro Inicial", "categoriaNueva", "Iniciador Bronce", "motivo", "Primera donación registrada y clasificada en Depósito Central")
        );
        model.addAttribute("categoryAudits", categoryAudits);

        Map<String, Object> donorMetrics = mapOf(
            "totalDonacionesHistoricas", userDonations.size(),
            "impactoAcumulado", 285,
            "organizacionesAyudadas", 3,
            "rachaMeses", 3,
            "puestoRankingMes", 3
        );
        model.addAttribute("donorMetrics", donorMetrics);

        // 4. Notificaciones
        List<Map<String, Object>> notifications = List.of(
            mapOf(
                "id", "n-101",
                "eventType", "ENTREGA_FOTOGRAFICA",
                "typeLabel", "Recepción con Evidencia",
                "title", "Entrega Confirmada con Fotos",
                "message", "Comedor Los Niños confirmó la recepción de 25 camperas térmicas y subió 2 fotografías de evidencia pública al sistema.",
                "channel", "Email (juan.perez@donatrack.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "Hoy, 16:45",
                "unread", true,
                "badgeType", "success",
                "hasEvidence", true,
                "evidenceUrl", "/explorar-donaciones"
            ),
            mapOf(
                "id", "n-102",
                "eventType", "MISION_CUMPLIDA",
                "typeLabel", "Misión Cumplida",
                "title", "¡Misión Cumplida!",
                "message", "Completaste la MisionCompletitud al abastecer la necesidad de abrigo.",
                "channel", "Email (juan.perez@donatrack.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "Hoy, 14:00",
                "unread", true,
                "badgeType", "reward",
                "badgeName", "Guardián del Invierno"
            )
        );
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", 2);

        // 5. Entregas Activas desde Logística real
        List<Map<String, Object>> activeDonorDeliveries = new ArrayList<>();
        try {
            var realDeliveries = logisticaApi.entregas();
            if (realDeliveries != null && !realDeliveries.isEmpty()) {
                for (var e : realDeliveries) {
                    activeDonorDeliveries.add(mapOf(
                        "id", "del-" + e.id(),
                        "donationId", String.valueOf(e.asignacionId()),
                        "donationTitle", "Entrega #" + e.id(),
                        "category", "Logística",
                        "destinationEntity", "Fundación Despierta",
                        "destinationAddress", "Av. Corrientes 4500, Almagro, CABA",
                        "destinationCoords", mapOf("lat", -34.5989, "lng", -58.4395),
                        "status", e.estado() != null ? e.estado() : "En traslado",
                        "eta", "Hoy, 15:30",
                        "totalDistanceKm", 4.2,
                        "progressPercent", 75,
                        "truck", mapOf(
                            "plate", e.camionPatente() != null ? e.camionPatente() : "AB 123 CD",
                            "model", "Mercedes-Benz Sprinter 516",
                            "driver", "Miguel Ángel",
                            "driverPhone", "+54 11 1234-5678",
                            "capacityKg", 5000,
                            "currentLoadKg", 2500,
                            "gps", mapOf(
                                "lat", -34.6050,
                                "lng", -58.4250,
                                "speed", 38.5,
                                "lastUpdate", "Hace 35 segundos"
                            )
                        ),
                        "stops", List.of(
                            mapOf("orden", 1, "direccion", "Depósito Central UTN", "hora", "14:30", "completada", true),
                            mapOf("orden", 2, "direccion", "Av. Corrientes 4500 (Fundación Despierta)", "hora", "15:30", "completada", false)
                        )
                    ));
                }
            }
        } catch (Exception ignored) {}

        if (activeDonorDeliveries.isEmpty()) {
            activeDonorDeliveries = List.of(
                mapOf(
                    "id", "del-102",
                    "donationId", "102",
                    "donationTitle", "Fideos Secos y Harina (50 kg)",
                    "category", "Alimentos",
                    "destinationEntity", "Fundación Despierta",
                    "destinationAddress", "Av. Corrientes 4500, Almagro, CABA",
                    "destinationCoords", mapOf("lat", -34.5989, "lng", -58.4395),
                    "status", "En traslado",
                    "eta", "Hoy, 15:30 (en 12 mins)",
                    "totalDistanceKm", 4.2,
                    "progressPercent", 75,
                    "truck", mapOf(
                        "plate", "AB 123 CD",
                        "model", "Mercedes-Benz Sprinter 516",
                        "driver", "Miguel Ángel",
                        "driverPhone", "+54 11 1234-5678",
                        "capacityKg", 5000,
                        "currentLoadKg", 2500,
                        "gps", mapOf(
                            "lat", -34.6050,
                            "lng", -58.4250,
                            "speed", 38.5,
                            "lastUpdate", "Hace 35 segundos"
                        )
                    ),
                    "stops", List.of(
                        mapOf("orden", 1, "direccion", "Depósito Central UTN", "hora", "14:30", "completada", true),
                        mapOf("orden", 2, "direccion", "Av. Corrientes 4500 (Fundación Despierta)", "hora", "15:30", "completada", false)
                    )
                )
            );
        }
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

    @GetMapping("/entidad/dashboard/notificaciones")
    public String beneficiaryNotifications(Model model) {
        populateBeneficiaryModel(model);
        return "dashboard-beneficiary-notificaciones";
    }

    private void populateBeneficiaryModel(Model model) {
        String entityName = "Fundación Vida";
        String entityCuit = "30-71234567-8";
        String entityEmail = "contacto@fundacionvida.org";

        try {
            var beneficiaries = donacionesApi.beneficiarios();
            if (beneficiaries != null && !beneficiaries.isEmpty()) {
                var b = beneficiaries.get(0);
                entityName = b.razonSocial();
                entityEmail = "contacto@" + b.razonSocial().toLowerCase().replaceAll("[^a-z0-9]", "") + ".org";
            }
        } catch (Exception ignored) {}

        model.addAttribute("user", mapOf(
            "name", entityName,
            "cuit", entityCuit,
            "address", "Av. San Martín 1234, CABA",
            "representative", "Laura Domínguez",
            "email", entityEmail
        ));
        
        List<Map<String, Object>> needs = new ArrayList<>();
        try {
            var realNeeds = donacionesApi.necesidades();
            if (realNeeds != null && !realNeeds.isEmpty()) {
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
                        Map.entry("deadline", n.periodo() != null ? n.periodo() : "15/04/2026"),
                        Map.entry("progress", prog),
                        Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=200&h=150&fit=crop")
                    ));
                }
            }
        } catch (Exception ignored) {}

        if (needs.isEmpty()) {
            needs = List.of(
                Map.ofEntries(
                    Map.entry("id", "need-1"),
                    Map.entry("title", "Alimentos no perecederos (Fideos y Arroz)"),
                    Map.entry("type", "Recurrente"),
                    Map.entry("subcategory", "Fideos secos"),
                    Map.entry("category", "Alimentos"),
                    Map.entry("description", "Consumo periódico para 250 viandas semanales del comedor comunitario."),
                    Map.entry("priority", "Alta"),
                    Map.entry("status", "Activa"),
                    Map.entry("quantity", 100),
                    Map.entry("unit", "kg"),
                    Map.entry("deadline", "15/04/2026"),
                    Map.entry("progress", 60),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=200&h=150&fit=crop")
                ),
                Map.ofEntries(
                    Map.entry("id", "need-2"),
                    Map.entry("title", "Sillas y Bancos para Aula"),
                    Map.entry("type", "Extraordinaria"),
                    Map.entry("subcategory", "Sillas"),
                    Map.entry("category", "Mobiliario"),
                    Map.entry("description", "Reposición urgente tras rotura por temporal."),
                    Map.entry("priority", "Crítica"),
                    Map.entry("status", "En progreso"),
                    Map.entry("quantity", 30),
                    Map.entry("unit", "unidades"),
                    Map.entry("deadline", "30/03/2026"),
                    Map.entry("progress", 40),
                    Map.entry("image_url", "https://images.unsplash.com/photo-1503602642458-232111445657?w=200&h=150&fit=crop")
                )
            );
        }
        model.addAttribute("needs", needs);

        List<Map<String, Object>> assignedDonations = List.of(
            mapOf(
                "id", "d1",
                "title", "Donación de Fideos y Arroz",
                "donor_name", "Supermercados Dia",
                "status", "En camino",
                "items_count", 50,
                "expected_delivery", "Hoy, 15:30",
                "driver", "Miguel Ángel",
                "truck_plate", "AB 123 CD"
            ),
            mapOf(
                "id", "d2",
                "title", "Donación de Sillas Escolares",
                "donor_name", "Arcos Plateados S.A.",
                "status", "Lista para entregar",
                "items_count", 12,
                "expected_delivery", "Mañana, 10:00",
                "driver", "Carlos Ruiz",
                "truck_plate", "EF 456 GH"
            )
        );
        model.addAttribute("assignedDonations", assignedDonations);

        List<Map<String, Object>> beneficiaryNotifications = List.of(
            mapOf(
                "id", "bn-01",
                "eventType", "ASIGNACION_SEMANTICA",
                "title", "Nueva Donación Asignada",
                "message", "El Algoritmo Semántico asignó donaciones a su necesidad recurrente.",
                "channel", "Email (" + entityEmail + ")",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "Hoy, 11:20",
                "unread", true,
                "badgeType", "info",
                "actionUrl", "/entidad/dashboard/donaciones"
            ),
            mapOf(
                "id", "bn-02",
                "eventType", "CAMION_DESPACHADO",
                "title", "Camión en Camino hacia su Sede",
                "message", "El camión asignado inició viaje hacia su sede.",
                "channel", "WhatsApp institucional",
                "channelIcon", "message-circle",
                "status", "COMPLETADA",
                "date", "Hoy, 14:00",
                "unread", true,
                "badgeType", "warning",
                "actionUrl", "/entidad/dashboard/entregas"
            )
        );
        model.addAttribute("beneficiaryNotifications", beneficiaryNotifications);

        List<Map<String, Object>> incomingDeliveries = List.of(
            mapOf(
                "id", "inc-1",
                "donationId", "D-401",
                "title", "Partida de Alimentos: Fideos y Arroz",
                "donor", "Supermercados Dia",
                "category", "Alimentos",
                "isPerishable", true,
                "goods", List.of(
                    mapOf("nombre", "Fideos secos Matarazzo", "cantidad", "30 kg", "vencimiento", "15/12/2026"),
                    mapOf("nombre", "Arroz blanco largo fino", "cantidad", "20 kg", "vencimiento", "20/01/2027")
                ),
                "handlingNotes", "Mantener en lugar seco. Estibar sobre pallets.",
                "status", "En camino",
                "urgency", "Próximo a llegar (< 15 min)",
                "isImminent", true,
                "eta", "Hoy, 15:30",
                "etaMinutes", 15,
                "truck", mapOf(
                    "plate", "AB 123 CD",
                    "model", "Mercedes-Benz Sprinter 516",
                    "driver", "Miguel Ángel",
                    "driverPhone", "+54 11 1234-5678",
                    "currentSpeed", 38.5,
                    "lastPing", "Hace 20 segundos",
                    "coords", mapOf("lat", -34.6050, "lng", -58.4250)
                )
            ),
            mapOf(
                "id", "inc-2",
                "donationId", "D-408",
                "title", "Lote de Abrigo: Frazadas y Ropa",
                "donor", "Juan Pérez",
                "category", "Vestimenta",
                "isPerishable", false,
                "goods", List.of(
                    mapOf("nombre", "Frazadas térmicas polares", "cantidad", "15 unidades", "vencimiento", "N/A"),
                    mapOf("nombre", "Camperas de abrigo infantiles", "cantidad", "10 unidades", "vencimiento", "N/A")
                ),
                "handlingNotes", "Bultos embalados en plástico sellado.",
                "status", "En camino",
                "urgency", "En tránsito hoy (en 45 min)",
                "isImminent", false,
                "eta", "Hoy, 16:15",
                "etaMinutes", 45,
                "truck", mapOf(
                    "plate", "EF 456 GH",
                    "model", "Iveco Daily 70C17",
                    "driver", "Carlos Ruiz",
                    "driverPhone", "+54 11 8765-4321",
                    "currentSpeed", 45.0,
                    "lastPing", "Hace 1 minuto",
                    "coords", mapOf("lat", -34.6190, "lng", -58.4350)
                )
            )
        );
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

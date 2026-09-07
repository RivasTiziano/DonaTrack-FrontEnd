package ar.edu.utn.donatrack.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class WebController {

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
        List<Map<String, Object>> donations = List.of(
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
        model.addAttribute("user", mapOf(
            "name", "Juan Pérez",
            "email", "juan.perez@donatrack.org",
            "category", "Sostenedor Plata",
            "type", "Humana",
            "document", "38450123"
        ));
        
        // 1. Historial de Donaciones con Categorías, Subcategorías y Estados de Dominio
        List<Map<String, Object>> userDonations = List.of(
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
            ),
            mapOf(
                "id", "103",
                "title", "Sillas de Estudio Ergonómicas",
                "category", "Mobiliario",
                "subcategory", "Sillas de estudio",
                "item_type", "Durable",
                "entity_name", "Escuela Rural N°10",
                "entity_id", "e2",
                "status", "Asignación realizada",
                "delivery_date", "Planificada para mañana",
                "total_units", 6,
                "unit", "unidades",
                "image_url", "https://images.unsplash.com/photo-1503602642458-232111445657?w=400&h=300&fit=crop"
            ),
            mapOf(
                "id", "104",
                "title", "Leche Larga Vida Entera",
                "category", "Alimentos",
                "subcategory", "Leche larga vida",
                "item_type", "Perecedero",
                "entity_name", "Depósito Central",
                "entity_id", "dep",
                "status", "En depósito",
                "delivery_date", "Clasificado en depósito",
                "total_units", 30,
                "unit", "litros",
                "image_url", "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&h=300&fit=crop"
            ),
            mapOf(
                "id", "105",
                "title", "Frazadas Térmicas Polares",
                "category", "Vestimenta",
                "subcategory", "Frazadas térmicas",
                "item_type", "Durable",
                "entity_name", "Hogar San Francisco",
                "entity_id", "e4",
                "status", "En traslado",
                "delivery_date", "Hoy, 17:15",
                "total_units", 15,
                "unit", "unidades",
                "truck_plate", "EF 456 GH",
                "image_url", "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=400&h=300&fit=crop"
            ),
            mapOf(
                "id", "106",
                "title", "Arroz Blanco Grano Largo (Bolsas 5kg)",
                "category", "Alimentos",
                "subcategory", "Arroz blanco",
                "item_type", "Perecedero",
                "entity_name", "Centro Comunitario Esperanza",
                "entity_id", "e5",
                "status", "Lista para entregar",
                "delivery_date", "Ruta asignada",
                "total_units", 40,
                "unit", "kg",
                "image_url", "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&h=300&fit=crop"
            ),
            mapOf(
                "id", "107",
                "title", "Kits de Cuadernos y Cartucheras",
                "category", "Educación",
                "subcategory", "Útiles escolares",
                "item_type", "Durable",
                "entity_name", "Escuela Rural N°10",
                "entity_id", "e2",
                "status", "Entregada",
                "delivery_date", "05 Mar 2026",
                "total_units", 20,
                "unit", "kits",
                "image_url", "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=400&h=300&fit=crop"
            ),
            mapOf(
                "id", "108",
                "title", "Pupitres Escolares Dobles",
                "category", "Mobiliario",
                "subcategory", "Pupitres escolares",
                "item_type", "Durable",
                "entity_name", "Escuela Puentes",
                "entity_id", "e6",
                "status", "Asignación realizada",
                "delivery_date", "Planificada",
                "total_units", 4,
                "unit", "unidades",
                "image_url", "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=400&h=300&fit=crop"
            )
        );
        model.addAttribute("userDonations", userDonations);

        // 2. Directorio Detallado de Entidades Beneficiarias (diagramaDonaciones.puml)
        List<Map<String, Object>> beneficiaryEntities = List.of(
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
                    mapOf("nombre", "Laura Domínguez", "cargo", "Directora Ejecutiva", "telefono", "+54 11 4862-1100"),
                    mapOf("nombre", "Carlos Morales", "cargo", "Coordinador de Donaciones", "telefono", "+54 11 4862-1101")
                ),
                "contactChannels", List.of(
                    mapOf("type", "EMAIL", "value", "contacto@fundaciondespierta.org", "isDefault", true),
                    mapOf("type", "WHATSAPP", "value", "+54 9 11 5544-3322", "isDefault", false),
                    mapOf("type", "TELEFONO", "value", "+54 11 4862-1100", "isDefault", false)
                ),
                "activeNeeds", List.of(
                    mapOf("title", "Alimentos no perecederos (Fideos y Arroz)", "type", "Recurrente", "target", 150, "current", 90, "unit", "kg", "urgency", "Alta"),
                    mapOf("title", "Leche entera en polvo", "type", "Recurrente", "target", 80, "current", 45, "unit", "kg", "urgency", "Media")
                ),
                "recentDonations", List.of(
                    mapOf("item", "25 camperas térmicas", "date", "10 Mar 2026", "donor", "Juan Pérez", "verified", true)
                )
            ),
            mapOf(
                "id", "e2",
                "name", "Escuela Rural N°10 'Alas de Futuro'",
                "cuit", "30-68994512-1",
                "type", "INSTITUCION",
                "category", "Institución Educativa",
                "rubro", "Educación rural y jornada extendida",
                "description", "Escuela rural de jornada completa con comedor escolar, huerta educativa y biblioteca comunitaria abierta al pueblo.",
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
                    mapOf("type", "EMAIL", "value", "escuela10.navarro@abc.gob.ar", "isDefault", true),
                    mapOf("type", "WHATSAPP", "value", "+54 9 2272 61-9988", "isDefault", false)
                ),
                "activeNeeds", List.of(
                    mapOf("title", "Mobiliario escolar (Sillas y pupitres)", "type", "Extraordinaria", "target", 20, "current", 6, "unit", "unidades", "urgency", "Crítica"),
                    mapOf("title", "Kits de útiles escolares de primaria", "type", "Recurrente", "target", 50, "current", 20, "unit", "kits", "urgency", "Media")
                ),
                "recentDonations", List.of(
                    mapOf("item", "20 kits de útiles", "date", "05 Mar 2026", "donor", "Juan Pérez", "verified", true)
                )
            ),
            mapOf(
                "id", "e3",
                "name", "Comedor Social 'Los Niños de San Telmo'",
                "cuit", "30-71678234-9",
                "type", "ONG",
                "category", "Comedor Social",
                "rubro", "Seguridad alimentaria y viandas comunitarias",
                "description", "Reparto diario de 250 viandas calientes y meriendas para familias en situación de vulnerabilidad extrema.",
                "verified", true,
                "contactsCount", 930,
                "rating", 5.0,
                "image", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&h=300&fit=crop",
                "location", mapOf(
                    "address", "Defensa 1100, San Telmo, CABA",
                    "calle", "Defensa", "numero", "1100", "ciudad", "CABA", "provincia", "Buenos Aires",
                    "lat", -34.6172, "lng", -58.3714
                ),
                "representatives", List.of(
                    mapOf("nombre", "Marta Gómez", "cargo", "Coordinadora General", "telefono", "+54 11 4307-5511")
                ),
                "contactChannels", List.of(
                    mapOf("type", "WHATSAPP", "value", "+54 9 11 6789-0123", "isDefault", true),
                    mapOf("type", "EMAIL", "value", "comedorlosninos@gmail.com", "isDefault", false)
                ),
                "activeNeeds", List.of(
                    mapOf("title", "Ropa de abrigo y frazadas de invierno", "type", "Recurrente", "target", 80, "current", 25, "unit", "unidades", "urgency", "Alta")
                ),
                "recentDonations", List.of(
                    mapOf("item", "25 camperas de invierno", "date", "10 Mar 2026", "donor", "Juan Pérez", "verified", true)
                )
            ),
            mapOf(
                "id", "e4",
                "name", "Hogar de Ancianos San Francisco",
                "cuit", "30-65239918-2",
                "type", "INSTITUCION",
                "category", "Hogar de Tercera Edad",
                "rubro", "Cuidado integral y abrigo a personas mayores",
                "description", "Residencia comunitaria sin fines de lucro para 60 adultos mayores sin red de contención familiar.",
                "verified", true,
                "contactsCount", 340,
                "rating", 4.7,
                "image", "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=400&h=300&fit=crop",
                "location", mapOf(
                    "address", "Av. San Juan 3200, Boedo, CABA",
                    "calle", "Av. San Juan", "numero", "3200", "ciudad", "CABA", "provincia", "Buenos Aires",
                    "lat", -34.6289, "lng", -58.4123
                ),
                "representatives", List.of(
                    mapOf("nombre", "Lic. Roberto Paz", "cargo", "Director Médico", "telefono", "+54 11 4931-8822")
                ),
                "contactChannels", List.of(
                    mapOf("type", "EMAIL", "value", "hogarsanfrancisco@redsolidaria.org", "isDefault", true),
                    mapOf("type", "TELEFONO", "value", "+54 11 4931-8822", "isDefault", false)
                ),
                "activeNeeds", List.of(
                    mapOf("title", "Frazadas térmicas polares", "type", "Recurrente", "target", 40, "current", 15, "unit", "unidades", "urgency", "Alta")
                ),
                "recentDonations", List.of(
                    mapOf("item", "15 frazadas térmicas", "date", "Hoy", "donor", "Juan Pérez", "verified", false)
                )
            ),
            mapOf(
                "id", "e5",
                "name", "Centro Comunitario Esperanza",
                "cuit", "30-78112345-0",
                "type", "ONG",
                "category", "Centro Comunitario",
                "rubro", "Inclusión social y capacitación barrial",
                "description", "Espacio barrial de formación laboral, merendero y entrega periódica de bolsones de alimentos a familias del barrio.",
                "verified", true,
                "contactsCount", 760,
                "rating", 4.9,
                "image", "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=400&h=300&fit=crop",
                "location", mapOf(
                    "address", "Warnes 700, Villa Crespo, CABA",
                    "calle", "Warnes", "numero", "700", "ciudad", "CABA", "provincia", "Buenos Aires",
                    "lat", -34.5946, "lng", -58.4434
                ),
                "representatives", List.of(
                    mapOf("nombre", "Lucía Benítez", "cargo", "Coordinadora Barrial", "telefono", "+54 11 4855-0909")
                ),
                "contactChannels", List.of(
                    mapOf("type", "EMAIL", "value", "contacto@esperanzacaba.org", "isDefault", true),
                    mapOf("type", "WHATSAPP", "value", "+54 9 11 3322-1144", "isDefault", false)
                ),
                "activeNeeds", List.of(
                    mapOf("title", "Legumbres secas y arroz blanco", "type", "Recurrente", "target", 100, "current", 40, "unit", "kg", "urgency", "Alta")
                ),
                "recentDonations", List.of()
            ),
            mapOf(
                "id", "e6",
                "name", "Escuela Puentes Solidarios",
                "cuit", "30-54129933-7",
                "type", "GUBERNAMENTAL",
                "category", "Institución Educativa",
                "rubro", "Educación pública e inclusión",
                "description", "Colegio técnico estatal de formación en oficios y bachillerato orientado con más de 400 alumnos becados.",
                "verified", true,
                "contactsCount", 610,
                "rating", 4.6,
                "image", "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=400&h=300&fit=crop",
                "location", mapOf(
                    "address", "Av. Rivadavia 5800, Caballito, CABA",
                    "calle", "Av. Rivadavia", "numero", "5800", "ciudad", "CABA", "provincia", "Buenos Aires",
                    "lat", -34.6179, "lng", -58.4471
                ),
                "representatives", List.of(
                    mapOf("nombre", "Ing. Gustavo Pereyra", "cargo", "Rector", "telefono", "+54 11 4432-8877")
                ),
                "contactChannels", List.of(
                    mapOf("type", "EMAIL", "value", "puentes@educacion.gob.ar", "isDefault", true)
                ),
                "activeNeeds", List.of(
                    mapOf("title", "Pupitres escolares dobles", "type", "Extraordinaria", "target", 15, "current", 4, "unit", "unidades", "urgency", "Alta")
                ),
                "recentDonations", List.of()
            )
        );
        model.addAttribute("beneficiaryEntities", beneficiaryEntities);

        // 3. Sistema Completo de Incentivos (diagramaIncentivos.puml)
        // Categorías en cadena
        List<Map<String, Object>> incentiveCategories = List.of(
            mapOf("id", 1, "nombre", "Iniciador Bronce", "nivel", 1, "status", "Completada", "misionesTotal", 3, "misionesCumplidas", 3, "descripcion", "Primeros pasos en la red solidaria."),
            mapOf("id", 2, "nombre", "Sostenedor Plata", "nivel", 2, "status", "Actual", "misionesTotal", 4, "misionesCumplidas", 3, "descripcion", "Donante recurrente con compromiso activo."),
            mapOf("id", 3, "nombre", "Protector Oro", "nivel", 3, "status", "Siguiente Nivel", "misionesTotal", 4, "misionesCumplidas", 0, "descripcion", "Alto impacto comunitario y diversidad de rubros."),
            mapOf("id", 4, "nombre", "Embajador Platino", "nivel", 4, "status", "Bloqueada", "misionesTotal", 5, "misionesCumplidas", 0, "descripcion", "Líder solidario referente de la plataforma.")
        );
        model.addAttribute("incentiveCategories", incentiveCategories);

        // Misiones Concretas (MisionRacha, MisionCompletitud, MisionHabilDonador, MisionDonacionesExitosas)
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
            ),
            mapOf(
                "id", 203,
                "orden", 3,
                "categoriaId", 2,
                "categoriaNombre", "Sostenedor Plata",
                "tipo", "MisionCompletitud",
                "tipoLabel", "Completitud de Necesidad",
                "tipoBadgeClass", "badge-completitud",
                "tipoIcon", "target",
                "nombre", "Misión Abrigo 100%",
                "descripcion", "Cubrir por completo una necesidad extraordinaria de abrigo o vestimenta de una institución educativa rural.",
                "valorObjetivo", 1,
                "valorActual", 1,
                "unidad", "necesidad satisfecha",
                "progresoPorcentaje", 100,
                "completada", true,
                "fechaCompletada", "10/03/2026",
                "insigniaPremio", mapOf("nombre", "Guardián del Invierno", "descripcion", "Abasteció al 100% una necesidad de abrigo", "icon", "snowflake", "color", "#0284c7")
            ),
            mapOf(
                "id", 204,
                "orden", 4,
                "categoriaId", 2,
                "categoriaNombre", "Sostenedor Plata",
                "tipo", "MisionHabilDonador",
                "tipoLabel", "Hábil Donador Multirubro",
                "tipoBadgeClass", "badge-multirubro",
                "tipoIcon", "layers",
                "nombre", "Alcance Comunitario Diverso (3 Rubros)",
                "descripcion", "Realizar donaciones a entidades de al menos 3 rubros diferentes (ej: Comedor, Escuela Rural y Hogar de Ancianos).",
                "valorObjetivo", 3,
                "valorActual", 2,
                "unidad", "rubros ayudados (Comedor, Escuela)",
                "progresoPorcentaje", 66,
                "completada", false,
                "fechaCompletada", "En curso",
                "insigniaPremio", mapOf("nombre", "Puente Solidario", "descripcion", "Apoyó a entidades de 3 rubros distintos", "icon", "heart-handshake", "color", "#7c3aed")
            ),
            // Misión del siguiente nivel para preview
            mapOf(
                "id", 301,
                "orden", 1,
                "categoriaId", 3,
                "categoriaNombre", "Protector Oro",
                "tipo", "MisionHabilDonador",
                "tipoLabel", "Hábil Donador Multirubro",
                "tipoBadgeClass", "badge-multirubro",
                "tipoIcon", "layers",
                "nombre", "Gran Red Solidaria (5 Entidades)",
                "descripcion", "Colaborar con al menos 5 organizaciones beneficiarias verificadas distintas en la plataforma.",
                "valorObjetivo", 5,
                "valorActual", 3,
                "unidad", "organizaciones asistidas",
                "progresoPorcentaje", 60,
                "completada", false,
                "fechaCompletada", "Bloqueada (Requiere Nivel Oro)",
                "insigniaPremio", mapOf("nombre", "Corona de Oro Solidaria", "descripcion", "Asistió a más de 5 organizaciones de la red", "icon", "crown", "color", "#eab308")
            )
        );
        model.addAttribute("missions", missions);

        // Insignias del Donante (Insignias Obtenidas e Insignias por Desbloquear)
        List<Map<String, Object>> badges = List.of(
            mapOf("id", "b1", "name", "Primer Paso Solidario", "description", "Realizaste tu primera donación física verificada en depósito", "earnedDate", "12/01/2026", "icon", "star", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b2", "name", "Llama Solidaria", "description", "Racha ininterrumpida de 3 meses continuos realizando donaciones", "earnedDate", "01/03/2026", "icon", "flame", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b3", "name", "Sello de Confianza", "description", "5 donaciones con entrega y recepción fotográfica validada", "earnedDate", "10/03/2026", "icon", "award", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b4", "name", "Guardián del Invierno", "description", "Abasteciste al 100% una necesidad extraordinaria de abrigo escolar", "earnedDate", "10/03/2026", "icon", "snowflake", "status", "Obtenida", "isPublic", true),
            mapOf("id", "b5", "name", "Puente Solidario", "description", "Apoya a entidades de 3 rubros diferentes (Comedor, Escuela, Hogar)", "earnedDate", "Por desbloquear (Misión #4)", "icon", "heart-handshake", "status", "Bloqueada", "isPublic", false),
            mapOf("id", "b6", "name", "Corona de Oro Solidaria", "description", "Alcanza la categoría Protector Oro y ayuda a 5 instituciones", "earnedDate", "Por desbloquear (Nivel Oro)", "icon", "crown", "status", "Bloqueada", "isPublic", false)
        );
        model.addAttribute("badges", badges);

        // Auditoría de Categorías (diagramaIncentivos.puml -> AuditoriaCategoria)
        List<Map<String, Object>> categoryAudits = List.of(
            mapOf("fecha", "15 Feb 2026, 11:30", "categoriaAnterior", "Iniciador Bronce", "categoriaNueva", "Sostenedor Plata", "motivo", "Completó las 3 misiones de iniciación y alcanzó 4 donaciones efectivas"),
            mapOf("fecha", "12 Ene 2026, 09:00", "categoriaAnterior", "Registro Inicial", "categoriaNueva", "Iniciador Bronce", "motivo", "Primera donación registrada y clasificada en Depósito Central")
        );
        model.addAttribute("categoryAudits", categoryAudits);

        // Métricas Mensuales e Históricas (diagramaIncentivos.puml -> MetricaMensual)
        Map<String, Object> donorMetrics = mapOf(
            "totalDonacionesHistoricas", 8,
            "impactoAcumulado", 285,
            "organizacionesAyudadas", 3,
            "rachaMeses", 3,
            "puestoRankingMes", 3
        );
        model.addAttribute("donorMetrics", donorMetrics);

        // 4. Notificaciones del Donante con Variedad Completa (diagramaNotificaciones.puml)
        List<Map<String, Object>> notifications = List.of(
            mapOf(
                "id", "n-101",
                "eventType", "ENTREGA_CONFIRMADA",
                "typeLabel", "Recepción Confirmada",
                "title", "Donación #101 Recibida con Éxito",
                "message", "Comedor Los Niños confirmó la recepción física de 25 camperas térmicas. El equipo subió 2 fotografías de evidencia pública al portal de transparencia.",
                "channel", "WhatsApp (+54 9 11 3845-0123)",
                "channelIcon", "message-circle",
                "status", "COMPLETADA",
                "date", "Hoy, 16:15",
                "unread", true,
                "badgeType", "success",
                "hasEvidence", true,
                "evidenceUrl", "/explorar-donaciones/donation-1"
            ),
            mapOf(
                "id", "n-102",
                "eventType", "MISION_CUMPLIDA",
                "typeLabel", "Misión Cumplida",
                "title", "¡Misión Cumplida! 'Misión Abrigo 100%'",
                "message", "Completaste la MisionCompletitud al abastecer la necesidad de abrigo de la Escuela Rural N°10. ¡Desbloqueaste la insignia 'Guardián del Invierno'!",
                "channel", "Email (juan.perez@donatrack.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "Hoy, 14:00",
                "unread", true,
                "badgeType", "reward",
                "badgeName", "Guardián del Invierno"
            ),
            mapOf(
                "id", "n-103",
                "eventType", "DONACION_ASIGNADA",
                "typeLabel", "Donación Asignada",
                "title", "Donación #102 Asignada por Algoritmo Semántico",
                "message", "Tu aporte de 50 kg de alimentos (fideos secos y harina) fue emparejado con la necesidad urgente de Fundación Despierta con un Score Semántico de 96%.",
                "channel", "Email (juan.perez@donatrack.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "Hoy, 10:30",
                "unread", false,
                "badgeType", "info"
            ),
            mapOf(
                "id", "n-104",
                "eventType", "ASCENSO_CATEGORIA",
                "typeLabel", "Ascenso de Categoría",
                "title", "¡Ascendiste a Sostenedor Plata!",
                "message", "El Servicio de Incentivos auditó tu trayectoria y promovió tu cuenta de Bronce a 'Sostenedor Plata'. Tus donaciones ahora tienen prioridad en rutas logísticas.",
                "channel", "SMS (+54 11 3845-0123)",
                "channelIcon", "smartphone",
                "status", "COMPLETADA",
                "date", "15 Feb 2026",
                "unread", false,
                "badgeType", "warning"
            ),
            mapOf(
                "id", "n-105",
                "eventType", "DONACION_ASIGNADA",
                "typeLabel", "Donación Asignada",
                "title", "Donación #103 de Mobiliario Asignada",
                "message", "Se asignaron 6 sillas ergonómicas a Escuela Rural N°10 mediante el Algoritmo de Subatendidos, priorizando la zona con menor cobertura histórica.",
                "channel", "Email (juan.perez@donatrack.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "Ayer, 18:20",
                "unread", false,
                "badgeType", "info"
            ),
            mapOf(
                "id", "n-106",
                "eventType", "EN_RUTA",
                "typeLabel", "Camión en Traslado",
                "title", "Camión AB 123 CD en Tránsito",
                "message", "El vehículo asignado a tu donación #102 inició el viaje hacia Fundación Despierta. Conductor: Miguel Ángel. Hora estimada de llegada: 15:30.",
                "channel", "WhatsApp (+54 9 11 3845-0123)",
                "channelIcon", "message-circle",
                "status", "COMPLETADA",
                "date", "Hoy, 14:30",
                "unread", false,
                "badgeType", "info"
            )
        );
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", 2);

        // 5. Entregas Activas con Telemetría GPS y Múltiples Camiones (diagramaLogistica.puml)
        List<Map<String, Object>> activeDonorDeliveries = List.of(
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
            ),
            mapOf(
                "id", "del-105",
                "donationId", "105",
                "donationTitle", "Frazadas Térmicas Polares (15 u)",
                "category", "Vestimenta",
                "destinationEntity", "Hogar San Francisco",
                "destinationAddress", "Av. San Juan 3200, Boedo, CABA",
                "destinationCoords", mapOf("lat", -34.6289, "lng", -58.4123),
                "status", "En traslado",
                "eta", "Hoy, 17:15 (en 1h 45 mins)",
                "totalDistanceKm", 7.8,
                "progressPercent", 40,
                "truck", mapOf(
                    "plate", "EF 456 GH",
                    "model", "Iveco Daily 70C17",
                    "driver", "Carlos Ruiz",
                    "driverPhone", "+54 11 8765-4321",
                    "capacityKg", 3500,
                    "currentLoadKg", 1800,
                    "gps", mapOf(
                        "lat", -34.6190,
                        "lng", -58.4350,
                        "speed", 44.0,
                        "lastUpdate", "Hace 1 minuto"
                    )
                ),
                "stops", List.of(
                    mapOf("orden", 1, "direccion", "Depósito Central UTN", "hora", "15:45", "completada", true),
                    mapOf("orden", 2, "direccion", "Av. San Juan 3200 (Hogar San Francisco)", "hora", "17:15", "completada", false)
                )
            ),
            mapOf(
                "id", "del-103",
                "donationId", "103",
                "donationTitle", "Sillas de Estudio Ergonómicas (6 u)",
                "category", "Mobiliario",
                "destinationEntity", "Escuela Rural N°10",
                "destinationAddress", "Ruta Prov. 41 Km 12, Navarro",
                "destinationCoords", mapOf("lat", -34.9812, "lng", -59.2745),
                "status", "Planificada",
                "eta", "Mañana, 10:00",
                "totalDistanceKm", 95.0,
                "progressPercent", 0,
                "truck", mapOf(
                    "plate", "MN 234 OP",
                    "model", "Ford Cargo 1722",
                    "driver", "Jorge Blanco",
                    "driverPhone", "+54 11 5566-7788",
                    "capacityKg", 8000,
                    "currentLoadKg", 0,
                    "gps", mapOf(
                        "lat", -34.6179,
                        "lng", -58.4471,
                        "speed", 0.0,
                        "lastUpdate", "En base (Depósito Central)"
                    )
                ),
                "stops", List.of(
                    mapOf("orden", 1, "direccion", "Depósito Central UTN", "hora", "Mañana 07:30", "completada", false),
                    mapOf("orden", 2, "direccion", "Ruta Prov. 41 Km 12 (Navarro)", "hora", "Mañana 10:00", "completada", false)
                )
            )
        );
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
        model.addAttribute("user", mapOf(
            "name", "Fundación Vida",
            "cuit", "30-71234567-8",
            "address", "Av. San Martín 1234, CABA",
            "representative", "Laura Domínguez",
            "email", "contacto@fundacionvida.org"
        ));
        
        List<Map<String, Object>> needs = List.of(
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
                Map.entry("description", "Reposición urgente tras rotura por temporal e inundación en el salón de estudio."),
                Map.entry("priority", "Crítica"),
                Map.entry("status", "En progreso"),
                Map.entry("quantity", 30),
                Map.entry("unit", "unidades"),
                Map.entry("deadline", "30/03/2026"),
                Map.entry("progress", 40),
                Map.entry("image_url", "https://images.unsplash.com/photo-1503602642458-232111445657?w=200&h=150&fit=crop")
            ),
            Map.ofEntries(
                Map.entry("id", "need-3"),
                Map.entry("title", "Camperas de Abrigo para Niños"),
                Map.entry("type", "Recurrente"),
                Map.entry("subcategory", "Ropa infantil"),
                Map.entry("category", "Vestimenta"),
                Map.entry("description", "Preparación para campaña de invierno para niños en situación de vulnerabilidad."),
                Map.entry("priority", "Media"),
                Map.entry("status", "Activa"),
                Map.entry("quantity", 50),
                Map.entry("unit", "unidades"),
                Map.entry("deadline", "01/05/2026"),
                Map.entry("progress", 20),
                Map.entry("image_url", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=200&h=150&fit=crop")
            )
        );
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

        // 6. Notificaciones de Entidad Beneficiaria con Variedad Completa (diagramaNotificaciones.puml)
        List<Map<String, Object>> beneficiaryNotifications = List.of(
            mapOf(
                "id", "bn-01",
                "eventType", "ASIGNACION_SEMANTICA",
                "title", "Nueva Donación Asignada: 50 kg de Alimentos",
                "message", "El Algoritmo Semántico asignó la Donación #D-401 (50 kg de fideos y harina) de Supermercados Dia a su necesidad recurrente de Comedor Comunitario con afinidad del 98%.",
                "channel", "Email (contacto@fundacionvida.org)",
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
                "title", "Camión AB 123 CD en Camino hacia su Sede",
                "message", "El Camión AB 123 CD inició viaje desde el Depósito Central con destino a Av. San Martín 1234. Chofer: Miguel Ángel (+54 11 1234-5678). Horario previsto de arribo: 15:30 hs.",
                "channel", "WhatsApp institucional",
                "channelIcon", "message-circle",
                "status", "COMPLETADA",
                "date", "Hoy, 14:00",
                "unread", true,
                "badgeType", "warning",
                "actionUrl", "/entidad/dashboard/entregas"
            ),
            mapOf(
                "id", "bn-03",
                "eventType", "PENDIENTE_FOTOS",
                "title", "Recordatorio: Evidencia Fotográfica Pendiente",
                "message", "Se registró la descarga física de la Donación #D-302 en su institución. Recuerde cargar al menos una fotografía clara para validar la recepción pública.",
                "channel", "SMS (+54 11 7123-4567)",
                "channelIcon", "smartphone",
                "status", "COMPLETADA",
                "date", "Ayer, 18:00",
                "unread", true,
                "badgeType", "danger",
                "actionUrl", "/entidad/dashboard/confirmar"
            ),
            mapOf(
                "id", "bn-04",
                "eventType", "RECEPCION_CONFIRMADA",
                "title", "Recepción y Fotos Validadas con Éxito",
                "message", "Su confirmación fotográfica de la Donación #D-201 (30 camperas de invierno) fue aprobada por auditoría y ya está visible en el Mapa de Transparencia de DonaTrack.",
                "channel", "Email (contacto@fundacionvida.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "10 Mar 2026",
                "unread", false,
                "badgeType", "success",
                "actionUrl", "/explorar-donaciones"
            ),
            mapOf(
                "id", "bn-05",
                "eventType", "ASIGNACION_SUBATENDIDOS",
                "title", "Asignación Prioritaria de Mobiliario",
                "message", "El Algoritmo de Subatendidos priorizó a su entidad para la entrega de 12 sillas de estudio de la empresa Arcos Plateados S.A.",
                "channel", "Email (contacto@fundacionvida.org)",
                "channelIcon", "mail",
                "status", "COMPLETADA",
                "date", "09 Mar 2026",
                "unread", false,
                "badgeType", "info",
                "actionUrl", "/entidad/dashboard/donaciones"
            ),
            mapOf(
                "id", "bn-06",
                "eventType", "ENTREGA_PLANIFICADA",
                "title", "Aviso de Ruta Programada para Mañana",
                "message", "El Camión EF 456 GH tiene programada una parada en su sede mañana entre las 10:00 y las 11:30 hs. Coordine al personal para la descarga.",
                "channel", "WhatsApp institucional",
                "channelIcon", "message-circle",
                "status", "COMPLETADA",
                "date", "Ayer, 16:30",
                "unread", false,
                "badgeType", "info",
                "actionUrl", "/entidad/dashboard/entregas"
            )
        );
        model.addAttribute("beneficiaryNotifications", beneficiaryNotifications);

        // 7. Entregas Entrantes en Tiempo Real con Múltiples Camiones (diagramaLogistica.puml)
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
            ),
            mapOf(
                "id", "inc-3",
                "donationId", "D-412",
                "title", "Mobiliario Escolar: Sillas y Bancos",
                "donor", "Arcos Plateados S.A.",
                "category", "Mobiliario",
                "isPerishable", false,
                "goods", List.of(
                    mapOf("nombre", "Sillas escolares reforzadas", "cantidad", "12 unidades", "vencimiento", "N/A")
                ),
                "handlingNotes", "Descarga pesada. Se requieren 2 personas para recepción.",
                "status", "Planificada",
                "urgency", "Planificado para mañana",
                "isImminent", false,
                "eta", "Mañana, 10:00",
                "etaMinutes", 1200,
                "truck", mapOf(
                    "plate", "MN 234 OP",
                    "model", "Ford Cargo 1722",
                    "driver", "Jorge Blanco",
                    "driverPhone", "+54 11 5566-7788",
                    "currentSpeed", 0.0,
                    "lastPing", "En alistamiento en Depósito Central",
                    "coords", mapOf("lat", -34.6179, "lng", -58.4471)
                )
            )
        );
        model.addAttribute("incomingDeliveries", incomingDeliveries);
    }

    // =========================================================================
    // ADMINISTRADOR DASHBOARD
    // =========================================================================
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        populateAdminModel(model);
        return "dashboard-admin";
    }

    @GetMapping("/admin/dashboard/donantes")
    public String adminDonors(Model model) {
        populateAdminModel(model);
        return "dashboard-admin-donantes";
    }

    @GetMapping("/admin/dashboard/donaciones")
    public String adminDonations(Model model) {
        populateAdminModel(model);
        return "dashboard-admin-donaciones";
    }

    @GetMapping("/admin/dashboard/asignar")
    public String adminAssign(Model model) {
        populateAdminModel(model);
        return "dashboard-admin-asignar";
    }

    @GetMapping("/admin/dashboard/camiones")
    public String adminTrucks(Model model) {
        populateAdminModel(model);
        return "dashboard-admin-camiones";
    }

    @GetMapping("/admin/dashboard/rankings")
    public String adminRankings(Model model) {
        populateAdminModel(model);
        return "dashboard-admin-rankings";
    }

    @GetMapping("/admin/dashboard/importar")
    public String adminImport(Model model) {
        populateAdminModel(model);
        return "dashboard-admin-importar";
    }

    private void populateAdminModel(Model model) {
        model.addAttribute("user", mapOf(
            "name", "Administrador Central",
            "email", "admin@donatrack.org",
            "role", "Administrador de Depósito"
        ));

        List<Map<String, Object>> donorsList = List.of(
            Map.ofEntries(
                Map.entry("id", "1"), Map.entry("type", "Humana"), Map.entry("name", "Juan Pérez"),
                Map.entry("docType", "DNI"), Map.entry("doc", "38450123"), Map.entry("email", "juan@ejemplo.com"),
                Map.entry("phone", "+54 11 1234-5678"), Map.entry("category", "Sostenedor Plata"),
                Map.entry("donationsCount", 12), Map.entry("regDate", "10/01/2026")
            ),
            Map.ofEntries(
                Map.entry("id", "2"), Map.entry("type", "Humana"), Map.entry("name", "María García"),
                Map.entry("docType", "DNI"), Map.entry("doc", "34987654"), Map.entry("email", "maria@ejemplo.com"),
                Map.entry("phone", "+54 11 7654-3210"), Map.entry("category", "Iniciador Bronce"),
                Map.entry("donationsCount", 8), Map.entry("regDate", "15/02/2026")
            ),
            Map.ofEntries(
                Map.entry("id", "3"), Map.entry("type", "Jurídica"), Map.entry("name", "Arcos Plateados S.A."),
                Map.entry("docType", "CUIT"), Map.entry("doc", "30-12345678-9"), Map.entry("email", "contacto@empresa.com"),
                Map.entry("phone", "+54 11 4444-4444"), Map.entry("category", "Empresa Solidaria"),
                Map.entry("donationsCount", 24), Map.entry("regDate", "05/01/2026")
            ),
            Map.ofEntries(
                Map.entry("id", "4"), Map.entry("type", "Jurídica"), Map.entry("name", "Molinos del Río"),
                Map.entry("docType", "CUIT"), Map.entry("doc", "30-98765432-1"), Map.entry("email", "donaciones@molinos.com"),
                Map.entry("phone", "+54 11 5555-6666"), Map.entry("category", "Empresa Solidaria"),
                Map.entry("donationsCount", 19), Map.entry("regDate", "12/02/2026")
            )
        );
        model.addAttribute("donorsList", donorsList);

        List<Map<String, Object>> warehouseDonations = List.of(
            Map.ofEntries(
                Map.entry("id", "don-01"),
                Map.entry("title", "Fideos Secos Matarazzo (100 paquetes)"),
                Map.entry("category", "Alimentos"),
                Map.entry("subcategory", "Fideos secos"),
                Map.entry("donor", "Molinos del Río"),
                Map.entry("status", "En depósito"),
                Map.entry("perishable", true),
                Map.entry("expirationDate", "01/01/2027"),
                Map.entry("condition", "Nuevo"),
                Map.entry("quantity", 100),
                Map.entry("unit", "paquetes (50 kg)"),
                Map.entry("storageLocation", "Estantería A-12"),
                Map.entry("receivedDate", "2026-04-10")
            ),
            Map.ofEntries(
                Map.entry("id", "don-02"),
                Map.entry("title", "Sillas de Oficina Ergonómicas"),
                Map.entry("category", "Mobiliario"),
                Map.entry("subcategory", "Sillas"),
                Map.entry("donor", "Arcos Plateados S.A."),
                Map.entry("status", "En depósito"),
                Map.entry("perishable", false),
                Map.entry("expirationDate", "N/A"),
                Map.entry("condition", "Usado (Excelente)"),
                Map.entry("quantity", 12),
                Map.entry("unit", "unidades"),
                Map.entry("storageLocation", "Sector B-04"),
                Map.entry("receivedDate", "2026-04-12")
            ),
            Map.ofEntries(
                Map.entry("id", "don-03"),
                Map.entry("title", "Camperas de Abrigo Térmicas"),
                Map.entry("category", "Vestimenta"),
                Map.entry("subcategory", "Camperas de abrigo"),
                Map.entry("donor", "Juan Pérez"),
                Map.entry("status", "Asignada"),
                Map.entry("perishable", false),
                Map.entry("expirationDate", "N/A"),
                Map.entry("condition", "Usado (Limpio)"),
                Map.entry("quantity", 20),
                Map.entry("unit", "unidades"),
                Map.entry("storageLocation", "Sector C-01"),
                Map.entry("receivedDate", "2026-04-14")
            )
        );
        model.addAttribute("warehouseDonations", warehouseDonations);

        // 8. Flota Completa de Camiones para Administrador (diagramaLogistica.puml)
        List<Map<String, Object>> truckFleet = List.of(
            mapOf(
                "plate", "AB 123 CD",
                "brand", "Mercedes-Benz Sprinter 516",
                "driver", "Miguel Ángel",
                "phone", "+54 11 1234-5678",
                "capacity", 5000,
                "currentLoad", 2500,
                "volumeCapacity", 14.5,
                "height", 2.8,
                "fuel", 80,
                "status", "En ruta",
                "activeRoute", "Ruta CABA-Norte (2 paradas)",
                "nextService", "10/06/2026",
                "gpsId", "GPS-TRK-001",
                "gpsCoords", "-34.6050, -58.4250",
                "speed", 38.5,
                "lastUpdate", "Hace 35 seg"
            ),
            mapOf(
                "plate", "EF 456 GH",
                "brand", "Iveco Daily 70C17",
                "driver", "Carlos Ruiz",
                "phone", "+54 11 8765-4321",
                "capacity", 3500,
                "currentLoad", 1800,
                "volumeCapacity", 12.0,
                "height", 2.6,
                "fuel", 92,
                "status", "En ruta",
                "activeRoute", "Ruta CABA-Sur (Hogar San Francisco)",
                "nextService", "15/07/2026",
                "gpsId", "GPS-TRK-002",
                "gpsCoords", "-34.6190, -58.4350",
                "speed", 44.0,
                "lastUpdate", "Hace 1 min"
            ),
            mapOf(
                "plate", "MN 234 OP",
                "brand", "Ford Cargo 1722",
                "driver", "Jorge Blanco",
                "phone", "+54 11 5566-7788",
                "capacity", 8000,
                "currentLoad", 0,
                "volumeCapacity", 28.0,
                "height", 3.4,
                "fuel", 95,
                "status", "Disponible",
                "activeRoute", "Asignada a Ruta Navarro (Mañana 07:30)",
                "nextService", "22/08/2026",
                "gpsId", "GPS-TRK-003",
                "gpsCoords", "-34.6179, -58.4471",
                "speed", 0.0,
                "lastUpdate", "En Base (Depósito Central)"
            ),
            mapOf(
                "plate", "IJ 789 KL",
                "brand", "Mercedes-Benz Accelo 815",
                "driver", "Roberto Gómez",
                "phone", "+54 11 9988-7766",
                "capacity", 6000,
                "currentLoad", 0,
                "volumeCapacity", 20.0,
                "height", 3.1,
                "fuel", 60,
                "status", "Fuera de servicio",
                "activeRoute", "En taller mecánico (Cambio de pastillas de freno)",
                "nextService", "En taller",
                "gpsId", "GPS-TRK-004",
                "gpsCoords", "-34.6300, -58.4600",
                "speed", 0.0,
                "lastUpdate", "Taller Oficial Mercedes"
            ),
            mapOf(
                "plate", "QR 345 ST",
                "brand", "Renault Master Chasis",
                "driver", "Fernando Soto",
                "phone", "+54 11 4433-2211",
                "capacity", 3000,
                "currentLoad", 0,
                "volumeCapacity", 11.0,
                "height", 2.5,
                "fuel", 88,
                "status", "Disponible",
                "activeRoute", "Sin ruta asignada (En base)",
                "nextService", "05/09/2026",
                "gpsId", "GPS-TRK-005",
                "gpsCoords", "-34.6179, -58.4471",
                "speed", 0.0,
                "lastUpdate", "En Base (Depósito Central)"
            ),
            mapOf(
                "plate", "UV 678 WX",
                "brand", "Scania P250",
                "driver", "Mariano Castro",
                "phone", "+54 11 3344-5566",
                "capacity", 12000,
                "currentLoad", 0,
                "volumeCapacity", 42.0,
                "height", 3.8,
                "fuel", 100,
                "status", "Disponible",
                "activeRoute", "Sin ruta asignada (Reserva gran volumen)",
                "nextService", "12/10/2026",
                "gpsId", "GPS-TRK-006",
                "gpsCoords", "-34.6179, -58.4471",
                "speed", 0.0,
                "lastUpdate", "En Base (Depósito Central)"
            )
        );
        model.addAttribute("truckFleet", truckFleet);

        // Flota KPIs
        int totalTrucks = truckFleet.size();
        long availableTrucks = truckFleet.stream().filter(t -> "Disponible".equals(t.get("status"))).count();
        long onRouteTrucks = truckFleet.stream().filter(t -> "En ruta".equals(t.get("status"))).count();
        long maintenanceTrucks = truckFleet.stream().filter(t -> "Fuera de servicio".equals(t.get("status"))).count();
        int totalCapacityKg = truckFleet.stream().mapToInt(t -> (Integer) t.get("capacity")).sum();
        model.addAttribute("totalTrucks", totalTrucks);
        model.addAttribute("availableTrucks", availableTrucks);
        model.addAttribute("onRouteTrucks", onRouteTrucks);
        model.addAttribute("maintenanceTrucks", maintenanceTrucks);
        model.addAttribute("totalCapacityKg", totalCapacityKg);

        List<Map<String, Object>> monthlyRankings = List.of(
            mapOf("rank", 1, "name", "Arcos Plateados S.A.", "type", "Jurídica", "totalUnits", 540, "donationsCount", 8, "badge", "Top 1 Solidario"),
            mapOf("rank", 2, "name", "Molinos del Río", "type", "Jurídica", "totalUnits", 420, "donationsCount", 6, "badge", "Gran Impacto"),
            mapOf("rank", 3, "name", "Juan Pérez", "type", "Humana", "totalUnits", 180, "donationsCount", 5, "badge", "Donante Estrella"),
            mapOf("rank", 4, "name", "María García", "type", "Humana", "totalUnits", 120, "donationsCount", 3, "badge", "Colaborador")
        );
        model.addAttribute("monthlyRankings", monthlyRankings);
    }

    private static Map<String, Object> mapOf(Object... kvs) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((String) kvs[i], kvs[i + 1]);
        }
        return map;
    }

}
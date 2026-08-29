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
            Map.of("label", "Términos de servicio", "href", "#"),
            Map.of("label", "Política de privacidad", "href", "#"),
            Map.of("label", "Aviso legal", "href", "#")
        ));

        return "landing";
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
        model.addAttribute("user", Map.of(
            "name", "Juan Pérez",
            "email", "juan.perez@donatrack.org",
            "category", "Sostenedor",
            "type", "Humana",
            "document", "38450123"
        ));
        
        List<Map<String, Object>> userDonations = List.of(
            Map.of(
                "id", "101",
                "title", "Camperas y Ropa de Invierno",
                "subcategory", "Camperas de abrigo",
                "category", "Vestimenta",
                "entity_name", "Comedor Los Niños",
                "status", "Entregada",
                "delivery_date", "10 Mar 2026",
                "total_units", 25,
                "image_url", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=300&h=200&fit=crop"
            ),
            Map.of(
                "id", "102",
                "title", "Fideos Secos y Harina",
                "subcategory", "Fideos secos",
                "category", "Alimentos",
                "entity_name", "Fundación Despierta",
                "status", "En traslado",
                "delivery_date", "Hoy, 15:30",
                "total_units", 50,
                "image_url", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=300&h=200&fit=crop"
            ),
            Map.of(
                "id", "103",
                "title", "Sillas de Estudio",
                "subcategory", "Sillas",
                "category", "Mobiliario",
                "entity_name", "Escuela Rural N°10",
                "status", "Asignación realizada",
                "delivery_date", "Planificada",
                "total_units", 6,
                "image_url", "https://images.unsplash.com/photo-1503602642458-232111445657?w=300&h=200&fit=crop"
            ),
            Map.of(
                "id", "104",
                "title", "Leche Larga Vida",
                "subcategory", "Lácteos",
                "category", "Alimentos",
                "entity_name", "Depósito Central",
                "status", "En depósito",
                "delivery_date", "Pendiente de matchmaking",
                "total_units", 30,
                "image_url", "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300&h=200&fit=crop"
            )
        );
        model.addAttribute("userDonations", userDonations);

        List<Map<String, Object>> badges = List.of(
            Map.of("id", "b1", "name", "Primer Paso", "description", "Realizaste tu primera donación verificada", "earnedDate", "01/01/2026", "icon", "star"),
            Map.of("id", "b2", "name", "Solidaridad Activa", "description", "Alcanzaste 5 entregas exitosas", "earnedDate", "15/02/2026", "icon", "award"),
            Map.of("id", "b3", "name", "Impacto Comunitario", "description", "Apoyaste a más de 100 familias", "earnedDate", "10/03/2026", "icon", "heart-handshake")
        );
        model.addAttribute("badges", badges);

        List<Map<String, Object>> beneficiaryEntities = List.of(
            Map.of(
                "id", "e1",
                "name", "Fundación Despierta",
                "category", "Comedor Comunitario",
                "description", "Brindamos asistencia nutricional y apoyo escolar a más de 200 niños y familias.",
                "verified", true,
                "contacts", 1250,
                "rating", 4.9,
                "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=100&h=100&fit=crop",
                "location", Map.of("address", "Av. Corrientes 4500, Almagro, CABA")
            ),
            Map.of(
                "id", "e2",
                "name", "Escuela Rural N°10",
                "category", "Institución Educativa",
                "description", "Escuela rural de jornada completa con comedor y biblioteca comunitaria.",
                "verified", true,
                "contacts", 480,
                "rating", 4.8,
                "image", "https://images.unsplash.com/photo-1509062522246-3755977927d7?w=100&h=100&fit=crop",
                "location", Map.of("address", "Ruta Prov. 41 Km 12, Navarro")
            ),
            Map.of(
                "id", "e3",
                "name", "Comedor Los Niños",
                "category", "Comedor Social",
                "description", "Reparto diario de viandas y meriendas para familias en situación de vulnerabilidad.",
                "verified", true,
                "contacts", 930,
                "rating", 5.0,
                "image", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=100&h=100&fit=crop",
                "location", Map.of("address", "Defensa 1100, San Telmo, CABA")
            )
        );
        model.addAttribute("beneficiaryEntities", beneficiaryEntities);

        List<Map<String, Object>> notifications = List.of(
            Map.of("id", "n1", "title", "Donación Entregada", "message", "Comedor Los Niños confirmó la recepción de 25 camperas con fotos verificadas.", "date", "Hace 2 horas", "unread", true, "type", "success"),
            Map.of("id", "n2", "title", "Donación en Camino", "message", "El camión AB 123 CD inició el traslado de 50 kg de alimentos a Fundación Despierta.", "date", "Hace 4 horas", "unread", true, "type", "info"),
            Map.of("id", "n3", "title", "¡Nueva Insignia Desbloqueada!", "message", "Obtuviste la insignia 'Impacto Comunitario' por alcanzar 100 familias ayudadas.", "date", "Ayer", "unread", false, "type", "reward")
        );
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", 2);
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
        model.addAttribute("user", Map.of(
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
                Map.entry("description", "Preparación para campaña de invierno para niños en situación de calle."),
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
            Map.of(
                "id", "d1",
                "title", "Donación de Fideos y Arroz",
                "donor_name", "Supermercados Dia",
                "status", "En camino",
                "items_count", 50,
                "expected_delivery", "Hoy, 15:30",
                "driver", "Miguel Ángel",
                "truck_plate", "AB 123 CD"
            ),
            Map.of(
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
            Map.of("id", "bn1", "title", "Asignación Confirmada", "message", "Se asignó una donación de 50 kg de alimentos desde Supermercados Dia a tu necesidad activa.", "date", "Hoy 11:20", "unread", true),
            Map.of("id", "bn2", "title", "Camión en Recorrido", "message", "El camión AB 123 CD está en camino a tu dirección con fecha de arribo estimada 15:30.", "date", "Hoy 14:00", "unread", true),
            Map.of("id", "bn3", "title", "Recordatorio de Recepción", "message", "Recuerda tomar fotos al recibir la donación para completar la verificación pública.", "date", "Ayer", "unread", false)
        );
        model.addAttribute("beneficiaryNotifications", beneficiaryNotifications);
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
        model.addAttribute("user", Map.of(
            "name", "Administrador Central",
            "email", "admin@donatrack.org",
            "role", "Administrador de Depósito"
        ));

        List<Map<String, Object>> donorsList = List.of(
            Map.ofEntries(
                Map.entry("id", "1"), Map.entry("type", "Humana"), Map.entry("name", "Juan Pérez"),
                Map.entry("docType", "DNI"), Map.entry("doc", "38450123"), Map.entry("email", "juan@ejemplo.com"),
                Map.entry("phone", "+54 11 1234-5678"), Map.entry("category", "Sostenedor"),
                Map.entry("donationsCount", 12), Map.entry("regDate", "10/01/2026")
            ),
            Map.ofEntries(
                Map.entry("id", "2"), Map.entry("type", "Humana"), Map.entry("name", "María García"),
                Map.entry("docType", "DNI"), Map.entry("doc", "34987654"), Map.entry("email", "maria@ejemplo.com"),
                Map.entry("phone", "+54 11 7654-3210"), Map.entry("category", "Colaborador"),
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

        List<Map<String, Object>> truckFleet = List.of(
            Map.of("plate", "AB 123 CD", "driver", "Miguel Ángel", "phone", "+54 11 1234-5678", "capacity", 5000, "currentLoad", 2500, "fuel", 80, "status", "En ruta", "nextService", "10/06/2026"),
            Map.of("plate", "EF 456 GH", "driver", "Carlos Ruiz", "phone", "+54 11 8765-4321", "capacity", 3500, "currentLoad", 0, "fuel", 95, "status", "Disponible", "nextService", "15/07/2026"),
            Map.of("plate", "IJ 789 KL", "driver", "Roberto Gómez", "phone", "+54 11 9988-7766", "capacity", 6000, "currentLoad", 0, "fuel", 60, "status", "Mantenimiento", "nextService", "En taller")
        );
        model.addAttribute("truckFleet", truckFleet);

        List<Map<String, Object>> monthlyRankings = List.of(
            Map.of("rank", 1, "name", "Arcos Plateados S.A.", "type", "Jurídica", "totalUnits", 540, "donationsCount", 8, "badge", "Top 1 Solidario"),
            Map.of("rank", 2, "name", "Molinos del Río", "type", "Jurídica", "totalUnits", 420, "donationsCount", 6, "badge", "Gran Impacto"),
            Map.of("rank", 3, "name", "Juan Pérez", "type", "Humana", "totalUnits", 180, "donationsCount", 5, "badge", "Donante Estrella"),
            Map.of("rank", 4, "name", "María García", "type", "Humana", "totalUnits", 120, "donationsCount", 3, "badge", "Colaborador")
        );
        model.addAttribute("monthlyRankings", monthlyRankings);
    }
}

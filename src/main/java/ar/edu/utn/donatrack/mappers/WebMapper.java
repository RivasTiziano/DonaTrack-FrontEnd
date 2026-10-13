package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.EntregaMapaDto;
import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
import org.springframework.stereotype.Component;
import java.util.*;
/**
 * Adaptación de datos públicos a las plantillas; no consulta servicios ni realiza HTTP.
 * Cuando la API pública no da un dato, el modelo lo lleva en null o en lista vacía y la plantilla muestra un estado vacío.
 */
@Component
public class WebMapper {
    // Foto decorativa del hero (no representa una donación real).
    private static final String IMAGEN_INSTITUCIONAL = "/img/hero-donaciones.jpg";

    /** Landing: las destacadas del último mes (la API ya filtra por desde) y la galería con las entregas que tienen imagen. */
    public Map<String,Object> landing(List<EntregaPublicaDto> entregas) {
        Map<String,Object> model = contenidoLanding();
        var recientes = entregas.stream().filter(e -> e.fechaEntrega() != null)
            .sorted(Comparator.comparing(EntregaPublicaDto::fechaEntrega).reversed()).toList();
        model.put("featuredDonations", recientes.stream().limit(3).map(this::tarjeta).toList());
        model.put("recentDeliveries", recientes.stream().filter(e -> hayTexto(e.imagen())).limit(6).map(e -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", e.id()); m.put("image", e.imagen()); m.put("title", e.descripcion()); return m;
        }).toList());
        return model;
    }

    /** Contenido fijo de la landing: hero, características y enlaces. No depende de la API. */
    private Map<String,Object> contenidoLanding() {
        Map<String,Object> model = new LinkedHashMap<>();
        model.put("heroContent", Map.of(
            "kicker", "Donaciones transparentes y verificables",
            "title", "Cada ayuda cuenta.",
            "description", "DonaTrack conecta personas donantes con entidades beneficiarias y permite consultar las entregas habilitadas para publicación.",
            "ctaLabel", "Ver cómo funciona",
            "heroImage", IMAGEN_INSTITUCIONAL
        ));

        // Sin métricas reales: la plantilla oculta el bloque de estadísticas.
        model.put("heroStats", List.of());

        model.put("landingFeatures", List.of(
            Map.of("title", "Trazabilidad Total", "description", "Seguimiento en tiempo real del estado de cada bien donado desde el depósito hasta su destino final.", "icon", "check-circle"),
            Map.of("title", "Segmentación Inteligente", "description", "Conectamos los bienes donados con las necesidades de las entidades.", "icon", "users"),
            Map.of("title", "Entregas publicadas", "description", "Conocé las entregas compartidas públicamente; las imágenes de prueba se identifican como ilustrativas.", "icon", "camera")
        ));

        model.put("footerQuickLinks", List.of(
            Map.of("label", "Acerca de", "href", "/#about"),
            Map.of("label", "Donaciones", "href", "/#donations"),
            Map.of("label", "Galería", "href", "/#gallery")
        ));

        model.put("footerAccessLinks", List.of(
            Map.of("label", "Iniciar sesión", "href", "/login"),
            Map.of("label", "Registro donantes", "href", "/registro"),
            Map.of("label", "Registro beneficiarios", "href", "/registro/entidad-beneficiaria")
        ));

        model.put("footerLegalLinks", List.of(
            Map.of("label", "Información legal y de privacidad", "href", "/legal"),
            Map.of("label", "Política de privacidad", "href", "/legal#privacidad"),
            Map.of("label", "Términos del servicio", "href", "/legal#terminos"),
            Map.of("label", "Transparencia pública", "href", "/legal#transparencia")
        ));
        return model;
    }

    private Map<String,Object> tarjeta(EntregaPublicaDto e) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", e.id());
        m.put("item", e.descripcion());
        m.put("image", fotoPrincipal(e));
        m.put("date", PanelFormato.fecha(e.fechaEntrega()));
        m.put("category", e.categoria());
        m.put("entity", e.entidad() == null ? null : e.entidad().nombre());
        m.put("locality", e.entidad() == null ? null : e.entidad().localidad());
        return m;
    }

    /** Detalle público: datos de la entrega y sus fotos revisadas. Si la API no la da, donation = null. */
    public Map<String,Object> entregaDetalle(EntregaPublicaDto e) {
        Map<String,Object> model = new LinkedHashMap<>();
        if (e == null) { model.put("donation", null); return model; }
        Map<String,Object> d = new LinkedHashMap<>();
        d.put("id", e.id());
        d.put("title", e.descripcion());
        d.put("description", e.descripcion());
        d.put("category", e.categoria());
        d.put("subcategory", e.subcategoria());
        d.put("delivery_date", PanelFormato.fecha(e.fechaEntrega()));
        d.put("total_units", e.cantidad());
        d.put("image_url", fotoPrincipal(e));
        d.put("photos", fotos(e));
        d.put("status", "ENTREGADA");
        d.put("entity_name", e.entidad() == null ? null : e.entidad().nombre());
        d.put("location", PanelFormato.mapa("locality", e.entidad() == null ? null : e.entidad().localidad()));
        String cantidad = (e.cantidad() == null ? "" : e.cantidad() + " ") + (e.unidad() == null ? "" : e.unidad());
        d.put("items", List.of(PanelFormato.mapa("name", e.descripcion(), "quantity", cantidad.trim())));
        model.put("donation", d);
        return model;
    }

    public Map<String,Object> legal() {
        Map<String,Object> model = new LinkedHashMap<>();
        model.put("footerQuickLinks", List.of(
            Map.of("label", "Acerca de", "href", "/#about"),
            Map.of("label", "Donaciones", "href", "/#donations"),
            Map.of("label", "Galería", "href", "/#gallery")
        ));
        model.put("footerAccessLinks", List.of(
            Map.of("label", "Iniciar sesión", "href", "/login"),
            Map.of("label", "Registro donantes", "href", "/registro"),
            Map.of("label", "Registro beneficiarios", "href", "/registro/entidad-beneficiaria")
        ));
        model.put("footerLegalLinks", List.of(
            Map.of("label", "Información legal y de privacidad", "href", "/legal"),
            Map.of("label", "Política de privacidad", "href", "/legal#privacidad"),
            Map.of("label", "Términos del servicio", "href", "/legal#terminos"),
            Map.of("label", "Transparencia pública", "href", "/legal#transparencia")
        ));
        return model;
    }

    /**
     * Mapa público: una entrega por marcador, en la ubicación de la entidad que la recibió (sin calle ni número).
     * Los contadores son reales: entregas y entidades distintas. No se suman cantidades con unidades distintas (kg, unidades) en un solo número.
     */
    public Map<String,Object> map(List<EntregaMapaDto> entregas) {
        Map<String,Object> model = new LinkedHashMap<>();
        List<Map<String,Object>> donations = new ArrayList<>();
        Set<String> entidades = new HashSet<>();
        for (var e : entregas) {
            donations.add(punto(e));
            if (e.entidad() != null) entidades.add(e.entidad().nombre() + "|" + e.entidad().localidad() + "|" + e.entidad().provincia());
        }
        model.put("donations", donations);
        model.put("donationsCount", donations.size());
        model.put("beneficiariesCount", entidades.size());
        return model;
    }

    private Map<String,Object> punto(EntregaMapaDto e) {
        var entidad = e.entidad();
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", e.id());
        m.put("category", e.categoria());
        m.put("subcategory", e.subcategoria());
        m.put("total_units", e.cantidad());
        m.put("unit", e.unidad());
        m.put("entity_name", entidad == null ? null : entidad.nombre());
        m.put("locality", entidad == null ? null : entidad.localidad());
        m.put("delivery_date", PanelFormato.fecha(e.fechaEntrega()));
        m.put("location", PanelFormato.mapa("lat", entidad == null ? null : entidad.latitud(),
                "lng", entidad == null ? null : entidad.longitud()));
        return m;
    }

    /** Primera foto revisada por el admin; si no hay, la imagen del bien. */
    private static String fotoPrincipal(EntregaPublicaDto e) {
        if (e.fotos() != null && !e.fotos().isEmpty() && hayTexto(e.fotos().get(0))) return e.fotos().get(0);
        return hayTexto(e.imagen()) ? e.imagen() : null;
    }

    private static List<String> fotos(EntregaPublicaDto e) {
        if (e.fotos() != null && !e.fotos().isEmpty()) return e.fotos().stream().filter(WebMapper::hayTexto).toList();
        return hayTexto(e.imagen()) ? List.of(e.imagen()) : List.of();
    }

    private static boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}

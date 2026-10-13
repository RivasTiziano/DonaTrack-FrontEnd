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
    /**
     * Landing (req. 1 y 2): destacadas de los últimos 30 días en el carrusel y, en el mapa embebido,
     * todas las entregas ya entregadas ubicadas en la entidad que las recibió. El detalle se ve en el popup del marcador.
     */
    public Map<String,Object> landing(List<EntregaPublicaDto> destacadas, List<EntregaMapaDto> mapa) {
        Map<String,Object> model = contenidoLanding();
        var recientes = destacadas.stream().filter(e -> e.fechaEntrega() != null)
            .sorted(Comparator.comparing(EntregaPublicaDto::fechaEntrega).reversed()).toList();
        model.put("featuredDonations", recientes.stream().limit(3).map(this::tarjeta).toList());
        Set<String> fotosVistas = new HashSet<>();
        model.put("recentDeliveries", recientes.stream()
            .filter(e -> hayTexto(fotoPrincipal(e)) && fotosVistas.add(fotoPrincipal(e))).limit(6).map(e -> {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id", e.id()); m.put("image", fotoPrincipal(e)); m.put("title", e.descripcion()); return m;
            }).toList());
        model.put("publicDeliveries", mapa.stream().map(this::marcador).toList());
        return model;
    }

    /** Datos de un marcador del mapa embebido, con las claves que usa landing.html. */
    private Map<String,Object> marcador(EntregaMapaDto e) {
        var entidad = e.entidad();
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", e.id());
        m.put("latitud", entidad == null ? null : entidad.latitud());
        m.put("longitud", entidad == null ? null : entidad.longitud());
        m.put("imagen", null);
        String rubro = hayTexto(e.subcategoria()) ? e.categoria() + " / " + e.subcategoria() : e.categoria();
        String destino = entidad == null ? null : entidad.nombre() + (hayTexto(entidad.localidad()) ? " (" + entidad.localidad() + ")" : "");
        m.put("descripcion", destino == null ? rubro : rubro + " — " + destino);
        m.put("fechaEntrega", e.fechaEntrega());
        m.put("cantidad", e.cantidad());
        m.put("unidad", e.unidad());
        return m;
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

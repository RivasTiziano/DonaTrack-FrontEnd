package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.ApiDtos;
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

    public Map<String,Object> landing(List<ApiDtos.Donacion> destacadas) {
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

        List<Map<String, Object>> featured = new ArrayList<>();
        for (var d : destacadas) {
            if (featured.size() >= 3) break;
            featured.add(tarjeta(d));
        }
        model.put("featuredDonations", featured);

        // Sin una API pública de fotografías: no presentar stock como entregas reales.
        model.put("recentDeliveries", List.of());

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

    public Map<String,Object> entregasLanding(List<ar.edu.utn.donatrack.dto.EntregaPublicaDto> entregas) {
        var model = landing(List.of());
        var recientes = entregas.stream().filter(e -> e.fechaEntrega() != null)
            .sorted(Comparator.comparing(ar.edu.utn.donatrack.dto.EntregaPublicaDto::fechaEntrega).reversed()).toList();
        var mes = java.time.YearMonth.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires"));
        model.put("featuredDonations", recientes.stream().filter(e -> java.time.YearMonth.from(e.fechaEntrega()).equals(mes)).limit(3).map(this::publicCard).toList());
        model.put("recentDeliveries", recientes.stream().filter(e -> e.imagen() != null && !e.imagen().isBlank()).limit(6).map(e -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",e.id()); m.put("image",e.imagen()); m.put("title",e.descripcion()); return m;
        }).toList());
        return model;
    }

    private Map<String,Object> publicCard(ar.edu.utn.donatrack.dto.EntregaPublicaDto e) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id",e.id());m.put("item",e.descripcion());m.put("image",e.imagen());
        m.put("date",e.fechaEntrega().toLocalDate().toString());
        m.put("donorName",null);m.put("beneficiary",null);
        return m;
    }

    public Map<String,Object> entregaDetalle(ar.edu.utn.donatrack.dto.EntregaPublicaDto e) {
        Map<String,Object> model = new LinkedHashMap<>();
        if(e == null) { model.put("donation",null);return model; }
        Map<String,Object> d = new LinkedHashMap<>();
        d.put("id",e.id());d.put("title",e.descripcion());d.put("description",e.descripcion());
        d.put("delivery_date",e.fechaEntrega().toLocalDate().toString());d.put("total_units",e.cantidad());
        d.put("image_url",e.imagen());d.put("status","ENTREGADA");d.put("entity_name",null);
        d.put("location",Map.of("address",""));
        d.put("items",List.of(Map.of("name",e.descripcion(),"quantity",e.cantidad()+" "+e.unidad())));
        model.put("donation",d);return model;
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

    /** Mapa público: sin coordenadas fijas. Un punto solo aparece si la entrega trae ubicación real. */
    public Map<String,Object> map(List<ApiDtos.Donacion> donaciones) {
        Map<String,Object> model = new LinkedHashMap<>();
        List<Map<String, Object>> donations = new ArrayList<>();
        int itemsCount = 0;
        for (var d : donaciones) {
            donations.add(punto(d));
            if (d.bien() != null && d.bien().cantidad() != null) itemsCount += d.bien().cantidad().intValue();
        }
        model.put("donations", donations);
        model.put("donationsCount", donations.size());
        // PROVISORIO: el contador de beneficiarios depende de GET /publico/entregas (pedido backend D1)
        model.put("beneficiariesCount", 0);
        model.put("itemsCount", itemsCount);
        return model;
    }

    /** Detalle público. Si la donación no está disponible, el modelo lleva donation = null. */
    public Map<String,Object> donationDetail(ApiDtos.Donacion d) {
        Map<String,Object> model = new LinkedHashMap<>();
        if (d == null) {
            model.put("donation", null);
            return model;
        }
        var bien = d.bien();
        Map<String,Object> donation = new LinkedHashMap<>();
        donation.put("id", String.valueOf(d.id()));
        donation.put("title", d.descripcionGeneral() != null ? d.descripcionGeneral() : descripcionBien(bien));
        donation.put("delivery_date", d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : null);
        donation.put("total_units", bien != null && bien.cantidad() != null ? bien.cantidad().intValue() : null);
        donation.put("description", d.descripcionGeneral());
        donation.put("image_url", fotoDe(bien));
        donation.put("status", d.estado());
        // La ubicación pública todavía no llega desde la API: el detalle no muestra dirección.
        donation.put("location", new LinkedHashMap<String,Object>(Map.of("address", "")));
        donation.put("entity_name", null);
        List<Map<String,Object>> items = new ArrayList<>();
        if (descripcionBien(bien) != null) {
            String cantidad = bien.cantidad() != null ? String.valueOf(bien.cantidad().intValue()) : "";
            String unidad = bien.unidadMedida() != null ? bien.unidadMedida() : "";
            items.add(Map.of("name", descripcionBien(bien), "quantity", (cantidad + " " + unidad).trim()));
        }
        donation.put("items", items);
        model.put("donation", donation);
        return model;
    }

    private Map<String,Object> tarjeta(ApiDtos.Donacion d) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", String.valueOf(d.id()));
        m.put("item", descripcionBien(d.bien()) != null ? descripcionBien(d.bien()) : d.descripcionGeneral());
        m.put("date", d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : null);
        m.put("image", fotoDe(d.bien()));
        return m;
    }

    private Map<String,Object> punto(ApiDtos.Donacion d) {
        var bien = d.bien();
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", String.valueOf(d.id()));
        m.put("title", descripcionBien(bien) != null ? descripcionBien(bien) : d.descripcionGeneral());
        m.put("category", bien != null && bien.categoria() != null ? bien.categoria().nombreCategoria() : null);
        m.put("total_units", bien != null && bien.cantidad() != null ? bien.cantidad().intValue() : null);
        m.put("status", d.estado());
        m.put("delivery_date", d.fechaRecepcion() != null ? d.fechaRecepcion().toLocalDate().toString() : null);
        m.put("image_url", fotoDe(bien));
        // Sin ubicación pública: lat y lng quedan en null y el mapa no dibuja el punto.
        Map<String,Object> ubicacion = new LinkedHashMap<>();
        ubicacion.put("lat", null);
        ubicacion.put("lng", null);
        ubicacion.put("address", null);
        m.put("location", ubicacion);
        return m;
    }

    private String descripcionBien(ApiDtos.Bien bien) {
        return bien != null && bien.descripcion() != null && !bien.descripcion().isBlank() ? bien.descripcion() : null;
    }

    private String fotoDe(ApiDtos.Bien bien) {
        return bien != null && bien.foto() != null && !bien.foto().isBlank() ? bien.foto() : null;
    }
}

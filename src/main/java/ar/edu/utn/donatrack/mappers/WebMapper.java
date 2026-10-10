package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.models.WebDatos;
import org.springframework.stereotype.Component;
import java.util.*;
/** Adaptación de los datos a las plantillas existentes; no consulta servicios ni realiza HTTP.
 * Los dashboards personales conservan su maquetado provisional hasta conectar identidad.
 */
@Component
public class WebMapper {
    public Map<String,Object> landing(WebDatos datos) {
        Map<String,Object> model = new java.util.LinkedHashMap<>();
        model.put("heroContent", Map.of(
            "kicker", "Donaciones transparentes y verificables",
            "title", "Cada ayuda cuenta, y cada entrega se puede comprobar.",
            "description", "DonaTrack conecta personas donantes con entidades beneficiarias de forma transparente, con trazabilidad de punta a punta y evidencia fotográfica pública.",
            "ctaLabel", "Ver cómo funciona",
            "heroImage", "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=900&h=700&fit=crop"
        ));

        model.put("heroStats", List.of(
            Map.of("value", "100%", "label", "trazabilidad verificada"),
            Map.of("value", "REST", "label", "microservicios conectados"),
            Map.of("value", "UTN", "label", "Diseño de Sistemas")
        ));

        model.put("landingFeatures", List.of(
            Map.of("title", "Trazabilidad Total", "description", "Seguimiento en tiempo real del estado de cada bien donado desde el depósito hasta su destino final.", "icon", "check-circle"),
            Map.of("title", "Segmentación Inteligente", "description", "Algoritmos de compatibilidad semántica para emparejar donaciones con necesidades reales.", "icon", "users"),
            Map.of("title", "Verificación Fotográfica", "description", "Cada entrega queda documentada con fotos públicas subidas por la entidad receptora.", "icon", "camera")
        ));

        List<Map<String, Object>> featured = new ArrayList<>();
        {
            var realDonations = datos.donaciones();
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
        }
        model.put("featuredDonations", featured);

        model.put("recentDeliveries", List.of(
            Map.of("title", "Entrega de útiles escolares", "image", "https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=300&h=300&fit=crop"),
            Map.of("title", "Distribución de alimentos", "image", "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=300&h=300&fit=crop"),
            Map.of("title", "Donación de ropa de abrigo", "image", "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=300&h=300&fit=crop")
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

public Map<String,Object> legal(WebDatos datos) {
        Map<String,Object> model = new java.util.LinkedHashMap<>();
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

public Map<String,Object> map(WebDatos datos) {
        Map<String,Object> model = new java.util.LinkedHashMap<>(); 
        List<Map<String, Object>> donations = new ArrayList<>();
        {
            var realDonations = datos.donaciones();
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
        }

        int beneficiariesCount = donations.stream().mapToInt(d -> (Integer) d.getOrDefault("beneficiaries_count", 0)).sum();
        int itemsCount = donations.stream().mapToInt(d -> (Integer) d.getOrDefault("total_units", 0)).sum();

        model.put("donations", donations);
        model.put("donationsCount", donations.size());
        model.put("beneficiariesCount", beneficiariesCount);
        model.put("itemsCount", itemsCount);

        return model; 
    }

public Map<String,Object> donationDetail(String id, WebDatos datos) {
        Map<String,Object> model = new java.util.LinkedHashMap<>();
        {
            var d = datos.donacion();
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
                model.put("donation", donation);
                return model;
            }
        }

        model.put("donation", Map.ofEntries(
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
        return model;
    }

}

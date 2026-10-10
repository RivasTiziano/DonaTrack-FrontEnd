package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.models.WebDatos;
import org.springframework.stereotype.Component;
import java.util.*;

/** Adapta la maqueta del dashboard; no consulta APIs ni determina identidad autenticada. */
@Component
public class BeneficiarioDashboardMapper {
    public Map<String,Object> mapear(WebDatos datos) {
        Map<String,Object> model = new java.util.LinkedHashMap<>();
        String entityName = "Entidad Beneficiaria";
        String entityCuit = "—";
        String entityEmail = "contacto@entidad.org";

        {
            var beneficiaries = datos.beneficiarios();
            if (beneficiaries != null && !beneficiaries.isEmpty()) {
                var b = beneficiaries.get(0);
                entityName = b.razonSocial();
                entityCuit = "CUIT-" + b.id();
                entityEmail = "contacto@" + b.razonSocial().toLowerCase().replaceAll("[^a-z0-9]", "") + ".org";
            }
        }

        model.put("user", mapOf(
            "name", entityName,
            "cuit", entityCuit,
            "address", "Sede Comunitaria",
            "representative", "Responsable Institucional",
            "email", entityEmail
        ));
        
        List<Map<String, Object>> needs = new ArrayList<>();
        {
            var realNeeds = datos.necesidades();
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
        }
        model.put("needs", needs);

        List<Map<String, Object>> assignedDonations = new ArrayList<>();
        {
            var realDonations = datos.donaciones();
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
        }
        model.put("assignedDonations", assignedDonations);

        List<Map<String, Object>> beneficiaryNotifications = List.of();
        model.put("beneficiaryNotifications", beneficiaryNotifications);

        List<Map<String, Object>> incomingDeliveries = new ArrayList<>();
        {
            var realDeliveries = datos.entregas();
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
        }
        model.put("incomingDeliveries", incomingDeliveries);
            return model;
    }
    private static Map<String, Object> mapOf(Object... kvs) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((String) kvs[i], kvs[i + 1]);
        }
        return map;
    }
}

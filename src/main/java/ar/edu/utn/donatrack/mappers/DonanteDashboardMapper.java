package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.models.WebDatos;
import org.springframework.stereotype.Component;
import java.util.*;

/** Adapta la maqueta del dashboard; no consulta APIs ni determina identidad autenticada. */
@Component
public class DonanteDashboardMapper {
    public Map<String,Object> mapear(WebDatos datos) {
        Map<String,Object> model = new java.util.LinkedHashMap<>();
        String donorName = "Donante";
        String donorEmail = "contacto@donatrack.org";
        String donorDoc = "—";
        String donorType = "Humana";
        String donorCategory = "Iniciador Bronce";

        {
            var humanos = datos.humanos();
            if (humanos != null && !humanos.isEmpty()) {
                var h = humanos.get(0);
                donorName = (h.nombre() != null ? h.nombre() : "") + " " + (h.apellido() != null ? h.apellido() : "");
                donorDoc = h.numeroDocumento() != null ? h.numeroDocumento() : "—";
                donorType = "Humana";
                if (h.mediosDeContacto() != null && !h.mediosDeContacto().isEmpty()) {
                    donorEmail = h.mediosDeContacto().get(0).formaContacto();
                }
            } else {
                var juridicos = datos.juridicos();
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
        }

        {
            var rankings = datos.ranking();
            if (rankings != null && !rankings.isEmpty()) {
                donorCategory = "Sostenedor Plata";
            }
        }

        model.put("user", mapOf(
            "name", donorName.trim().isEmpty() ? "Donante" : donorName.trim(),
            "email", donorEmail,
            "category", donorCategory,
            "type", donorType,
            "document", donorDoc
        ));
        
        // 1. Historial de Donaciones desde la API real
        List<Map<String, Object>> userDonations = new ArrayList<>();
        {
            var realDonations = datos.donaciones();
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
        }
        model.put("userDonations", userDonations);

        // 2. Directorio Detallado de Entidades Beneficiarias desde la API real
        List<Map<String, Object>> beneficiaryEntities = new ArrayList<>();
        {
            var realBeneficiaries = datos.beneficiarios();
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
        }
        model.put("beneficiaryEntities", beneficiaryEntities);

        // 3. Sistema de Incentivos
        List<Map<String, Object>> incentiveCategories = List.of(
            mapOf("id", 1, "nombre", "Iniciador Bronce", "nivel", 1, "status", "Completada", "misionesTotal", 3, "misionesCumplidas", 3, "descripcion", "Primeros pasos en la red solidaria."),
            mapOf("id", 2, "nombre", "Sostenedor Plata", "nivel", 2, "status", "Actual", "misionesTotal", 4, "misionesCumplidas", 3, "descripcion", "Donante recurrente con compromiso activo."),
            mapOf("id", 3, "nombre", "Protector Oro", "nivel", 3, "status", "Siguiente Nivel", "misionesTotal", 4, "misionesCumplidas", 0, "descripcion", "Alto impacto comunitario y diversidad de rubros."),
            mapOf("id", 4, "nombre", "Embajador Platino", "nivel", 4, "status", "Bloqueada", "misionesTotal", 5, "misionesCumplidas", 0, "descripcion", "Líder solidario referente de la plataforma.")
        );
        model.put("incentiveCategories", incentiveCategories);

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
        model.put("missions", missions);

        List<Map<String, Object>> badges = List.of(
            mapOf("id", "b1", "name", "Primer Paso Solidario", "description", "Realizaste tu primera donación física verificada", "earnedDate", "Verificada", "icon", "star", "status", userDonations.isEmpty() ? "Bloqueada" : "Obtenida", "isPublic", !userDonations.isEmpty())
        );
        model.put("badges", badges);

        List<Map<String, Object>> categoryAudits = List.of(
            mapOf("fecha", "Reciente", "categoriaAnterior", "Registro Inicial", "categoriaNueva", donorCategory, "motivo", "Evaluación automática del Servicio de Incentivos")
        );
        model.put("categoryAudits", categoryAudits);

        Map<String, Object> donorMetrics = mapOf(
            "totalDonacionesHistoricas", userDonations.size(),
            "impactoAcumulado", userDonations.size() * 30,
            "organizacionesAyudadas", beneficiaryEntities.size(),
            "rachaMeses", userDonations.isEmpty() ? 0 : 1,
            "puestoRankingMes", 1
        );
        model.put("donorMetrics", donorMetrics);

        // 4. Notificaciones
        List<Map<String, Object>> notifications = List.of();
        model.put("notifications", notifications);
        model.put("unreadNotifications", 0);

        // 5. Entregas Activas desde Logística real
        List<Map<String, Object>> activeDonorDeliveries = new ArrayList<>();
        {
            var realDeliveries = datos.entregas();
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
        }
        model.put("activeDonorDeliveries", activeDonorDeliveries);
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

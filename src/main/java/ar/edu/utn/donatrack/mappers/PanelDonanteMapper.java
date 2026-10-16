package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.DonanteRequests;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.dto.LogisticaDetalles;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import static ar.edu.utn.donatrack.mappers.PanelFormato.*;

/** Adapta los DTO de la API a las claves que usan las plantillas del panel del donante. Puro: no hace HTTP. */
@Component
public class PanelDonanteMapper {

    // ---------- Perfil ----------

    public Map<String, Object> perfilHumano(DonacionesDetalles.Humano h, String categoria) {
        String nombre = ((h.nombre() == null ? "" : h.nombre()) + " " + (h.apellido() == null ? "" : h.apellido())).trim();
        return mapa("name", nombre.isEmpty() ? null : nombre,
                "email", email(h.medioDeContactoPredeterminado()),
                "type", "Humana",
                "document", h.numeroDocumento(),
                "category", categoria);
    }

    public Map<String, Object> perfilJuridico(DonacionesDetalles.Juridico j, String categoria) {
        return mapa("name", notBlank(j.razonSocial()) ? j.razonSocial() : null,
                "email", email(j.medioDeContactoPredeterminado()),
                "type", "Jurídica",
                "document", j.numeroDocumento(),
                "category", categoria);
    }

    public Map<String, Object> metricas(IncentivosDtos.Metricas m) {
        if (m == null) return mapa("totalDonacionesHistoricas", null, "impactoAcumulado", null,
                "organizacionesAyudadas", null, "desgloseMensual", List.of());
        return mapa("totalDonacionesHistoricas", m.totalDonacionesHistoricas(),
                "impactoAcumulado", m.impactoAcumulado(),
                "organizacionesAyudadas", m.totalOrganizacionesAyudadas(),
                "desgloseMensual", m.desgloseMensual() == null ? List.of() : m.desgloseMensual());
    }

    // ---------- Donaciones ----------

    /**
     * Estado de E2 que ve el donante: Vencida si el depósito la venció; si no, el de la asignación más reciente
     * (PENDIENTE_RUTA es 'Asignación realizada'); sin asignaciones, el de la donación.
     */
    public static String estadoEfectivo(ApiDtos.Donacion d, List<ApiDtos.Asignacion> asignaciones) {
        if ("VENCIDA".equals(d.estado())) return "VENCIDA";
        var reciente = masReciente(asignaciones, ApiDtos.Asignacion::fechaAsignacion);
        if (reciente != null && reciente.estado() != null) return estadoAsignacion(reciente);
        return d.estado();
    }

    /** Estados de E2 que se filtran por la donación misma; los otros cinco, por sus asignaciones. */
    private static final Set<String> ESTADOS_DONACION = Set.of("EN_DEPOSITO", "VENCIDA");

    /**
     * Si la donación entra en el filtro de estado (INTERPRETACIÓN: el back asigna en partes). En depósito: no vencida y con
     * cantidad sin asignar (o sin asignaciones). Vencida: la donación. Los otros cinco: alguna asignación en ese estado
     * (PENDIENTE_RUTA es 'Asignación realizada').
     */
    public static boolean coincideFiltro(ApiDtos.Donacion d, List<ApiDtos.Asignacion> asignaciones, String estado) {
        if (estado == null) return true;
        if ("VENCIDA".equals(estado)) return "VENCIDA".equals(d.estado());
        if ("EN_DEPOSITO".equals(estado)) {
            if ("VENCIDA".equals(d.estado())) return false;
            Float libre = d.bien() == null ? null : d.bien().cantidadDisponible();
            return asignaciones.isEmpty() || (libre != null ? libre > 0 : "EN_DEPOSITO".equals(d.estado()));
        }
        return asignaciones.stream().anyMatch(a -> estado.equals(estadoAsignacion(a)));
    }

    private static String estadoAsignacion(ApiDtos.Asignacion a) {
        return "PENDIENTE_RUTA".equals(a.estado()) ? "ASIGNACION_REALIZADA" : a.estado();
    }

    /**
     * Una donación con sus asignaciones. El estado es el efectivo (ver estadoEfectivo) y la fecha sale de la asignación
     * más reciente. Sin filtro, el desglose muestra todas las asignaciones, plegado.
     */
    public Map<String, Object> donacion(ApiDtos.Donacion d, List<ApiDtos.Asignacion> asignaciones,
                                        Map<Long, ApiDtos.Beneficiario> entidades) {
        return donacion(d, asignaciones, entidades, null);
    }

    /**
     * Ver la sobrecarga de tres parámetros. estadoFiltro solo decide qué asignaciones muestra el desglose y si queda
     * desplegado; el estado de la tarjeta no cambia.
     */
    public Map<String, Object> donacion(ApiDtos.Donacion d, List<ApiDtos.Asignacion> asignaciones,
                                        Map<Long, ApiDtos.Beneficiario> entidades, String estadoFiltro) {
        ApiDtos.Bien bien = d.bien();
        ApiDtos.CategoriaBien categoria = bien == null ? null : bien.categoria();
        String titulo = bien != null && notBlank(bien.descripcion()) ? bien.descripcion() : d.descripcionGeneral();
        String cat = categoria == null ? null : categoria.nombreCategoria();
        String sub = categoria == null || categoria.subCategoria() == null ? null : categoria.subCategoria().nombreSubCategoria();

        var primera = asignaciones.isEmpty() ? null : asignaciones.get(0);
        var reciente = masReciente(asignaciones, ApiDtos.Asignacion::fechaAsignacion);
        String estado = estadoEfectivo(d, asignaciones);
        var base = reciente != null ? reciente : primera;
        Long entidadId = base == null ? null : base.entidadBeneficiariaId();
        ApiDtos.Beneficiario entidad = entidadId == null ? null : entidades.get(entidadId);
        String entidadNombre = entidadId == null ? null
                : (entidad != null ? entidad.razonSocial() : "Entidad no disponible");
        String entrega = reciente != null && reciente.fechaEntrega() != null
                ? fecha(reciente.fechaEntrega()) : fecha(d.fechaRecepcion());

        Float disponible = bien == null ? null : bien.cantidadDisponible();
        Object sinAsignar = disponible != null && disponible > 0 && !"VENCIDA".equals(d.estado())
                && (estadoFiltro == null || ESTADOS_DONACION.contains(estadoFiltro)) ? numero(disponible) : null;

        List<Map<String, Object>> desglose = new ArrayList<>();
        asignaciones.stream()
                .filter(a -> estadoFiltro == null || ESTADOS_DONACION.contains(estadoFiltro) || estadoFiltro.equals(estadoAsignacion(a)))
                .sorted(Comparator.comparing(ApiDtos.Asignacion::fechaAsignacion, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(a -> desglose.add(asignacionFila(a, entidades)));

        return mapa("id", d.id(),
                "title", titulo,
                "category", cat,
                "subcategory", sub,
                "item_type", bien == null ? null : bien.tipoBien(),
                "entity_name", entidadNombre,
                "entity_id", entidadId,
                "status", estado,
                "status_label", etiquetaEstado(estado),
                "status_badge", claseEstado(estado),
                "estado_donacion", d.estado(),
                "estado_donacion_label", etiquetaEstado(d.estado()),
                "estado_donacion_badge", claseEstado(d.estado()),
                "delivery_date", entrega,
                "total_units", bien == null ? null : numero(bien.cantidad()),
                "unit", bien == null ? null : minusculas(bien.unidadMedida()),
                "sin_asignar", sinAsignar,
                "sin_asignar_deposito", disponible != null && disponible > 0 && !"VENCIDA".equals(d.estado()) ? numero(disponible) : null,
                "asignaciones", desglose,
                "desglose_abierto", estadoFiltro != null && !ESTADOS_DONACION.contains(estadoFiltro),
                "image_url", bien != null && notBlank(bien.foto()) ? bien.foto() : null,
                "evidence_photos", List.of(),
                "fecha_recepcion", fecha(d.fechaRecepcion()));
    }

    /** Pasos de E2 de una asignación, en orden. ENTREGA_FALLIDA reemplaza al último (sale de En traslado). */
    private static final List<String> PASOS = List.of("EN_DEPOSITO", "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR",
            "EN_TRASLADO", "ENTREGADA");
    private static final Map<String, String> ICONOS = Map.of("EN_DEPOSITO", "package", "ASIGNACION_REALIZADA", "git-merge",
            "LISTA_PARA_ENTREGAR", "clipboard-check", "EN_TRASLADO", "truck", "ENTREGADA", "check-circle",
            "ENTREGA_FALLIDA", "x-circle");

    /**
     * Stepper de una asignación: anteriores completed, actual active (Entregada: completed), fallida failed.
     * No hay historial de estados en el back: solo se fechan Asignación realizada y Entregada.
     */
    private static List<Map<String, Object>> pasos(ApiDtos.Asignacion a) {
        String estado = estadoAsignacion(a);
        boolean fallida = "ENTREGA_FALLIDA".equals(estado);
        int actual = fallida ? PASOS.size() - 1 : PASOS.indexOf(estado);
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < PASOS.size(); i++) {
            String paso = fallida && i == actual ? "ENTREGA_FALLIDA" : PASOS.get(i);
            String clase = i < actual || (i == actual && "ENTREGADA".equals(paso)) ? "completed"
                    : i == actual ? (fallida ? "failed" : "active") : "";
            String fechaPaso = "ASIGNACION_REALIZADA".equals(paso) ? fecha(a.fechaAsignacion())
                    : "ENTREGADA".equals(paso) ? fecha(a.fechaEntrega()) : null;
            out.add(mapa("estado", paso, "label", etiquetaEstado(paso), "icono", ICONOS.get(paso),
                    "clase", clase, "fecha", fechaPaso));
        }
        return out;
    }

    /**
     * Una parte de la donación: cantidad, entidad, estado de la asignación y su fecha (entrega si ya se entregó).
     * Incluye el stepper (pasos) y la justificación de la fallida, para el modal de detalle.
     */
    private Map<String, Object> asignacionFila(ApiDtos.Asignacion a, Map<Long, ApiDtos.Beneficiario> entidades) {
        Long entidadId = a.entidadBeneficiariaId();
        ApiDtos.Beneficiario entidad = entidadId == null ? null : entidades.get(entidadId);
        String entidadNombre = entidadId == null ? null
                : (entidad != null ? entidad.razonSocial() : "Entidad no disponible");
        String estado = estadoAsignacion(a);
        boolean entregada = "ENTREGADA".equals(a.estado()) && a.fechaEntrega() != null;
        return mapa("id", a.id(),
                "cantidad", numero(a.cantidadAsignada()),
                "entidad", entidadNombre,
                "estado", estado,
                "estado_label", etiquetaEstado(estado),
                "estado_badge", claseEstado(estado),
                "fecha_label", entregada ? "Entrega" : "Asignación",
                "fecha", fecha(entregada ? a.fechaEntrega() : a.fechaAsignacion()),
                "pasos", pasos(a),
                "justificacion", "ENTREGA_FALLIDA".equals(a.estado()) && notBlank(a.justificacionFallo()) ? a.justificacionFallo() : null);
    }

    /** Categorías y subcategorías activas del catálogo, con id, para los combos de filtro. */
    public Map<String, Object> filtrosCatalogo(List<ApiDtos.Categoria> categorias) {
        List<Map<String, Object>> cats = new ArrayList<>();
        List<Map<String, Object>> subcategorias = new ArrayList<>();
        for (var c : categorias) {
            if (!c.activo()) continue;
            cats.add(mapa("id", c.id(), "nombre", c.nombre()));
            if (c.subCategorias() == null) continue;
            for (var s : c.subCategorias())
                if (s.activo()) subcategorias.add(mapa("id", s.id(), "nombre", s.nombre(), "categoria", c.nombre(), "categoriaId", c.id()));
        }
        return mapa("categoriasFiltro", cats, "subcategoriasFiltro", subcategorias);
    }

    // ---------- Entidades ----------

    /** Una entidad con sus representantes, medio de contacto y necesidades. */
    public Map<String, Object> entidad(ApiDtos.Beneficiario b, List<ApiDtos.Necesidad> necesidades) {
        List<DonacionesDetalles.Representante> reps = b.representantes() == null ? List.of() : b.representantes();
        var medio = b.medioDeContactoPredeterminado();
        var dir = b.direccion();

        List<Map<String, Object>> representatives = new ArrayList<>();
        for (var r : reps) representatives.add(mapa("nombre", r.nombre(), "telefono", r.telefono()));

        List<Map<String, Object>> channels = medio != null && notBlank(medio.formaContacto())
                ? List.of(mapa("type", medio.tipoMedioContacto(), "value", medio.formaContacto(), "isDefault", true))
                : List.of();

        List<Map<String, Object>> activeNeeds = new ArrayList<>();
        for (var n : necesidades) {
            if ("SATISFECHA".equals(n.estado())) continue;
            String unidad = n.bien() == null ? null : n.bien().unidadMedida();
            String nombreBien = n.bien() != null && notBlank(n.bien().descripcion()) ? n.bien().descripcion() : subcategoria(n.bien());
            activeNeeds.add(mapa("title", notBlank(nombreBien) ? nombreBien : (notBlank(n.descripcion()) ? n.descripcion() : "Bien no especificado"),
                    "cantidadSolicitadaLabel", cantidadConUnidad(n.bien() == null ? null : n.bien().cantidad(), unidad),
                    "cantidadRestanteLabel", cantidadConUnidad(n.cantidadRestante(), unidad),
                    "subcategory", subcategoria(n.bien()),
                    "cantidadRestante", numero(n.cantidadRestante()),
                    "unit", n.bien() == null ? null : minusculas(n.bien().unidadMedida())));
        }

        Map<String, Object> location = mapa("address", direccion(dir),
                "calle", dir == null ? null : dir.calle(),
                "numero", dir == null ? null : dir.numero(),
                "ciudad", dir == null ? null : dir.ciudad(),
                "provincia", dir == null ? null : dir.provincia(),
                "lat", dir == null ? null : dir.latitud(),
                "lng", dir == null ? null : dir.longitud());

        return mapa("id", b.id(),
                "name", b.razonSocial(),
                "type", b.tipoEntidad(),
                "category", b.rubro(),
                "rubro", b.rubro(),
                "verified", b.activo(),
                "contactsCount", reps.size(),
                "location", location,
                "representatives", representatives,
                "contactChannels", channels,
                "activeNeeds", activeNeeds,
                "recentDonations", List.of());
    }

    /** Opciones de los filtros del directorio: tipos, rubros y ciudades presentes. */
    public Map<String, Object> filtrosEntidades(List<Map<String, Object>> entidades) {
        Set<String> tipos = new TreeSet<>();
        Set<String> rubros = new TreeSet<>();
        Set<String> ciudades = new TreeSet<>();
        for (var e : entidades) {
            if (e.get("type") != null) tipos.add(e.get("type").toString());
            if (e.get("rubro") != null) rubros.add(e.get("rubro").toString());
            var location = (Map<?, ?>) e.get("location");
            if (location != null && location.get("ciudad") != null) ciudades.add(location.get("ciudad").toString());
        }
        return mapa("tiposEntidad", List.copyOf(tipos), "rubrosEntidad", List.copyOf(rubros),
                "ciudadesEntidad", List.copyOf(ciudades));
    }

    // ---------- Incentivos ----------

    /** Escalafón de categorías: estado relativo a la categoría actual del donante. */
    public List<Map<String, Object>> categorias(List<IncentivosDtos.Categoria> categorias,
                                                List<IncentivosDtos.MisionCompletada> completadas,
                                                String categoriaActual) {
        return categorias(categorias, completadas, categoriaActual, null);
    }

    public List<Map<String, Object>> categorias(List<IncentivosDtos.Categoria> categorias,
                                                List<IncentivosDtos.MisionCompletada> completadas,
                                                String categoriaActual, IncentivosDtos.Progreso progreso) {
        Set<Long> cumplidas = new HashSet<>();
        for (var c : completadas) cumplidas.add(c.idMision());

        int actual = -1;
        for (int i = 0; i < categorias.size(); i++)
            if (Objects.equals(categorias.get(i).nombre(), categoriaActual)) { actual = i; break; }
        int siguiente = actual >= 0 ? actual + 1 : 0;

        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < categorias.size(); i++) {
            var cat = categorias.get(i);
            List<IncentivosDtos.Mision> misiones = misiones(cat);
            long cumplidasCategoria = misiones.stream().filter(m -> cumplidas.contains(m.id())).count();
            double avanceParcial = progreso != null && progreso.idMision() != null
                    && !cumplidas.contains(progreso.idMision())
                    && misiones.stream().anyMatch(m -> Objects.equals(m.id(), progreso.idMision()))
                    && progreso.porcentajeProgreso() != null
                    ? Math.max(0, Math.min(100, progreso.porcentajeProgreso())) / 100d : 0;
            long porcentaje = misiones.isEmpty() ? 0 : Math.round(100d * (cumplidasCategoria + avanceParcial) / misiones.size());
            String status = i == actual ? "Actual"
                    : (actual >= 0 && i < actual) ? "Completada"
                    : i == siguiente ? "Siguiente Nivel"
                    : "Bloqueada";
            out.add(mapa("id", cat.id(), "nombre", cat.nombre(), "nivel", i + 1, "status", status,
                    "misionesTotal", misiones.size(), "misionesCumplidas", cumplidasCategoria,
                    "progresoPorcentaje", porcentaje, "descripcion", null));
        }
        return out;
    }

    /** Misión actual (si no completó todas) seguida de las completadas. */
    public List<Map<String, Object>> misiones(IncentivosDtos.Progreso progreso, String categoriaActual,
                                              List<IncentivosDtos.Categoria> categorias,
                                              List<IncentivosDtos.MisionCompletada> completadas) {
        Map<Long, IncentivosDtos.Mision> catalogo = new HashMap<>();
        Map<Long, String> categoriaDe = new HashMap<>();
        for (var cat : categorias) {
            for (var m : misiones(cat)) {
                catalogo.putIfAbsent(m.id(), m);
                categoriaDe.putIfAbsent(m.id(), cat.nombre());
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        if (progreso != null && progreso.mensaje() == null && progreso.idMision() != null) {
            Long id = progreso.idMision();
            var mision = catalogo.get(id);
            var visual = visual(mision == null ? null : mision.tipo());
            out.add(mapa("id", id,
                    "orden", progreso.orden(),
                    "categoriaNombre", categoriaDe.getOrDefault(id, categoriaActual),
                    "tipo", mision == null ? null : mision.tipo(),
                    "tipoIcon", visual.get("icon"),
                    "tipoLabel", visual.get("label"),
                    "tipoBadgeClass", visual.get("clase"),
                    "nombre", progreso.nombreMision(),
                    "descripcion", progreso.descripcionMision(),
                    "valorObjetivo", progreso.valorObjetivo(),
                    "valorActual", progreso.valorActual(),
                    "progresoPorcentaje", progreso.porcentajeProgreso() == null ? 0L : Math.round(progreso.porcentajeProgreso()),
                    "completada", Boolean.TRUE.equals(progreso.completada()),
                    "insigniaPremio", insignia(mision),
                    "fechaCompletada", null));
        }
        for (var c : completadas) {
            Long id = c.idMision();
            var mision = catalogo.get(id);
            var visual = visual(mision == null ? null : mision.tipo());
            out.add(mapa("id", id,
                    "orden", mision == null ? null : mision.orden(),
                    "categoriaNombre", categoriaDe.get(id),
                    "tipo", mision == null ? null : mision.tipo(),
                    "tipoIcon", visual.get("icon"),
                    "tipoLabel", visual.get("label"),
                    "tipoBadgeClass", visual.get("clase"),
                    "nombre", c.nombreMision(),
                    "descripcion", c.descripcionMision(),
                    "valorObjetivo", mision == null ? null : mision.valorObjetivo(),
                    "valorActual", null,
                    "progresoPorcentaje", 100L,
                    "completada", true,
                    "insigniaPremio", insignia(mision),
                    "fechaCompletada", fecha(c.fechaCompletada())));
        }
        return out;
    }

    public List<Map<String, Object>> insignias(List<IncentivosDtos.Insignia> insignias) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var i : insignias)
            out.add(mapa("name", i.nombre(), "description", i.descripcion(), "imageUrl", i.imagenUrl(),
                    "icon", "award", "status", "Obtenida"));
        return out;
    }

    public List<Map<String, Object>> auditorias(List<IncentivosDtos.Auditoria> auditorias) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var a : auditorias)
            out.add(mapa("fecha", fecha(a.fechaCambio()),
                    "categoriaAnterior", a.categoriaAnterior(),
                    "categoriaNueva", a.categoriaNueva()));
        return out;
    }

    private static String cantidadConUnidad(Float cantidad, String unidad) {
        if (cantidad == null) return "No informada";
        String nombreUnidad = unidad == null ? "" : unidad.toLowerCase(Locale.ROOT);
        if (cantidad != 1f) nombreUnidad = switch (nombreUnidad) {
            case "kilogramo" -> "kilogramos";
            case "gramo" -> "gramos";
            case "litro" -> "litros";
            case "unidad" -> "unidades";
            default -> nombreUnidad;
        };
        String valor = new java.math.BigDecimal(Float.toString(cantidad)).stripTrailingZeros().toPlainString().replace('.', ',');
        return nombreUnidad.isBlank() ? valor : valor + " " + nombreUnidad;
    }

    // ---------- Entregas ----------

    /**
     * Entrega en camino del donante: el seguimiento (destino, camión) más la asignación para el título y la categoría.
     * La asignación puede faltar si el listado no la trae; en ese caso la fila queda con datos mínimos.
     */
    public Map<String, Object> entrega(LogisticaDetalles.SeguimientoEntrega s, ApiDtos.Asignacion a, String entidadNombre,
                                       LocalDateTime ahora) {
        ApiDtos.Bien bien = a == null ? null : a.bien();
        String titulo = a == null ? null
                : notBlank(a.descripcionGeneralDonacion()) ? a.descripcionGeneralDonacion()
                : (bien == null ? null : bien.descripcion());
        String cat = bien == null || bien.categoria() == null ? null : bien.categoria().nombreCategoria();
        return mapa("id", s.entregaId(),
                "asignacionId", s.asignacionId(),
                "donationId", a == null ? null : a.donacionId(),
                "donationTitle", titulo,
                "category", cat,
                "destinationEntity", entidadNombre,
                "destinationAddress", ubicacion(s.destino()),
                "destinationCoords", coordenadas(s.destino()),
                "status", s.estado(),
                "status_label", etiquetaEstado(s.estado()),
                "status_badge", claseEstado(s.estado()),
                "truck", camion(s.camion(), ahora));
    }

    // ---------- Auxiliares ----------

    private static List<IncentivosDtos.Mision> misiones(IncentivosDtos.Categoria cat) {
        return cat.misiones() == null ? List.of() : cat.misiones();
    }

    private static Map<String, Object> insignia(IncentivosDtos.Mision mision) {
        if (mision == null || mision.insigniaPremio() == null) return null;
        var i = mision.insigniaPremio();
        return mapa("nombre", i.nombre(), "descripcion", i.descripcion(), "imagenUrl", i.imagenUrl());
    }

    private static Map<String, String> visual(String tipo) {
        if (tipo == null) return Map.of("icon", "award", "label", "Misión", "clase", "");
        return switch (tipo) {
            case "RACHA" -> Map.of("icon", "flame", "label", "Racha", "clase", "badge-racha");
            case "COMPLETITUD" -> Map.of("icon", "layers", "label", "Completitud", "clase", "badge-completitud");
            case "DONACIONESEXITOSAS" -> Map.of("icon", "check-circle", "label", "Donaciones Exitosas", "clase", "badge-entregas");
            case "HABILDONADOR" -> Map.of("icon", "award", "label", "Hábil Donador", "clase", "badge-habil");
            default -> Map.of("icon", "award", "label", tipo, "clase", "");
        };
    }

    private static String email(ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto medio) {
        return medio == null ? null : medio.formaContacto();
    }

    private static String subcategoria(ApiDtos.Bien bien) {
        if (bien == null || bien.categoria() == null || bien.categoria().subCategoria() == null) return null;
        return bien.categoria().subCategoria().nombreSubCategoria();
    }

    static boolean notBlank(String valor) { return valor != null && !valor.isBlank(); }
}

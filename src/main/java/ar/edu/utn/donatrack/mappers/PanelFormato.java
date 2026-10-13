package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.dto.DonanteRequests;
import ar.edu.utn.donatrack.dto.LogisticaDetalles;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Formatos y etiquetas compartidos por los mappers de los paneles personales. No consulta APIs. */
public final class PanelFormato {
    private PanelFormato() {}

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "AR"));
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("es", "AR"));

    /** Mapa que admite valores nulos (Map.of no los admite). */
    public static Map<String, Object> mapa(Object... kvs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) map.put((String) kvs[i], kvs[i + 1]);
        return map;
    }

    /** Cantidades enteras cuando no tienen decimales; el resto, tal cual. Null si no hay dato. */
    public static Object numero(Float valor) {
        if (valor == null) return null;
        return valor % 1 == 0 ? (Object) valor.intValue() : (Object) valor;
    }

    public static String fecha(LocalDateTime valor) { return valor == null ? null : valor.toLocalDate().format(FECHA); }
    public static String fecha(LocalDate valor) { return valor == null ? null : valor.format(FECHA); }
    public static String fechaHora(LocalDateTime valor) { return valor == null ? null : valor.format(FECHA_HORA); }

    /** Camión de una entrega en camino. Null si la entrega no tiene camión; lat/lng null si todavía no reportó GPS. */
    public static Map<String, Object> camion(LogisticaDetalles.CamionPosicion c, LocalDateTime ahora) {
        if (c == null) return null;
        return mapa("plate", c.patente(), "lat", c.latitud(), "lng", c.longitud(),
                "lastUpdate", fechaHora(c.ultimaActualizacion()), "lastUpdateAgo", hace(c.ultimaActualizacion(), ahora));
    }

    /** Coordenadas del destino, o null si la API no las informa. */
    public static Map<String, Object> coordenadas(LogisticaDetalles.Destino d) {
        return d != null && d.latitud() != null && d.longitud() != null ? mapa("lat", d.latitud(), "lng", d.longitud()) : null;
    }

    /** Ciudad y provincia del destino, sin calle ni número. */
    public static String ubicacion(LogisticaDetalles.Destino d) {
        if (d == null) return null;
        String texto = Stream.of(d.ciudad(), d.provincia()).filter(v -> v != null && !v.isBlank()).collect(Collectors.joining(", "));
        return texto.isEmpty() ? null : texto;
    }

    /** Tiempo transcurrido desde un momento hasta ahora, en español ("hace 5 min"). Null si no hay momento. */
    public static String hace(LocalDateTime momento, LocalDateTime ahora) {
        if (momento == null || ahora == null) return null;
        long minutos = Math.max(0, Duration.between(momento, ahora).toMinutes());
        if (minutos < 1) return "hace menos de 1 min";
        if (minutos < 60) return "hace " + minutos + " min";
        return "hace " + (minutos / 60) + " h";
    }

    public static String minusculas(String valor) { return valor == null ? null : valor.toLowerCase(Locale.ROOT); }

    /** "calle numero, ciudad, provincia", omitiendo las partes vacías. Null si no hay ninguna. */
    public static String direccion(DonanteRequests.Direccion d) {
        if (d == null) return null;
        String calle = unir(" ", d.calle(), d.numero());
        return unir(", ", calle, d.ciudad(), d.provincia());
    }

    private static String unir(String separador, String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (parte == null || parte.isBlank()) continue;
            if (sb.length() > 0) sb.append(separador);
            sb.append(parte.trim());
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** Texto del estado de una donación o de una asignación. */
    public static String etiquetaEstado(String estado) {
        if (estado == null) return "Sin estado";
        return switch (estado) {
            case "EN_DEPOSITO" -> "En depósito";
            case "ASIGNACION_REALIZADA" -> "Asignada";
            case "PENDIENTE_RUTA" -> "Pendiente de ruta";
            case "LISTA_PARA_ENTREGAR" -> "Lista para entregar";
            case "PLANIFICADA" -> "Planificada";
            case "EN_TRASLADO" -> "En traslado";
            case "ENTREGADA" -> "Entregada";
            case "ENTREGA_FALLIDA" -> "Entrega fallida";
            case "VENCIDA" -> "Vencida";
            case "SATISFECHA" -> "Satisfecha";
            case "EN_PROGRESO" -> "En progreso";
            case "PENDIENTE" -> "Pendiente";
            default -> estado;
        };
    }

    /** Clase CSS de la insignia de estado (existentes en la hoja de estilos). */
    public static String claseEstado(String estado) {
        if (estado == null) return "badge-deposito";
        return switch (estado) {
            case "ENTREGADA" -> "badge-entregada";
            case "EN_TRASLADO" -> "badge-traslado";
            case "ASIGNACION_REALIZADA", "PENDIENTE_RUTA", "PLANIFICADA" -> "badge-asignada";
            case "LISTA_PARA_ENTREGAR" -> "badge-lista";
            case "ENTREGA_FALLIDA", "VENCIDA" -> "badge-vencida";
            default -> "badge-deposito";
        };
    }

    /** Elemento con la fecha más reciente; si ninguno tiene fecha, el último de la lista. Null si la lista está vacía. */
    public static <T> T masReciente(List<T> elementos, Function<T, LocalDateTime> fecha) {
        if (elementos == null || elementos.isEmpty()) return null;
        T mejor = null;
        LocalDateTime mejorFecha = null;
        for (T e : elementos) {
            LocalDateTime f = fecha.apply(e);
            if (f == null) continue;
            if (mejorFecha == null || f.isAfter(mejorFecha)) { mejor = e; mejorFecha = f; }
        }
        return mejor != null ? mejor : elementos.get(elementos.size() - 1);
    }
}

package ar.edu.utn.donatrack.mappers;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Locale;
/** Etiquetas visuales; los valores originales del contrato permanecen intactos. */
@Component("adminEstados")
public class AdminEstados {
    private static final Map<String,String> ETIQUETAS = Map.ofEntries(
        Map.entry("EN_DEPOSITO", "En depósito"),
        Map.entry("ASIGNACION_REALIZADA", "Asignada"),
        Map.entry("VENCIDA", "Vencida"),
        Map.entry("DISPONIBLE", "Disponible"),
        Map.entry("EN_RUTA", "En ruta"),
        Map.entry("FUERA_DE_SERVICIO", "Fuera de servicio"),
        Map.entry("EN_TRASLADO", "En traslado"),
        Map.entry("NO_RECIBIDA", "No recibida"),
        Map.entry("ENTREGADA", "Entregada"),
        Map.entry("ENTREGA_FALLIDA", "Entrega fallida"),
        Map.entry("PENDIENTE", "Pendiente"),
        Map.entry("PENDIENTE_RUTA", "Pendiente de ruta"),
        Map.entry("LISTA_PARA_ENTREGAR", "Lista para entregar"),
        Map.entry("PLANIFICADA", "Planificada"),
        Map.entry("EN_CURSO", "En curso"),
        Map.entry("FINALIZADA", "Finalizada"),
        Map.entry("CANCELADA", "Cancelada"),
        Map.entry("SATISFECHA", "Satisfecha"),
        Map.entry("ACTIVA", "Activa"),
        Map.entry("EN_PROGRESO", "En progreso"),
        Map.entry("RACHA", "Racha"),
        Map.entry("COMPLETITUD", "Completitud"),
        Map.entry("DONACIONESEXITOSAS", "Donaciones exitosas"),
        Map.entry("HABILDONADOR", "Hábil donador"),
        Map.entry("PENDIENTE_REGISTRO", "Registro pendiente"),
        Map.entry("COMPLETO", "Completo"),
        Map.entry("RECURRENTE", "Recurrente"),
        Map.entry("EXTRAORDINARIA", "Extraordinaria"));
    private static final Map<String,String> TONOS = Map.ofEntries(
        Map.entry("EN_DEPOSITO", "chip-neutral"),
        Map.entry("ASIGNACION_REALIZADA", "chip-info"),
        Map.entry("VENCIDA", "chip-danger"),
        Map.entry("DISPONIBLE", "chip-success"),
        Map.entry("EN_RUTA", "chip-info"),
        Map.entry("FUERA_DE_SERVICIO", "chip-danger"),
        Map.entry("EN_TRASLADO", "chip-info"),
        Map.entry("NO_RECIBIDA", "chip-danger"),
        Map.entry("ENTREGADA", "chip-success"),
        Map.entry("ENTREGA_FALLIDA", "chip-danger"),
        Map.entry("PENDIENTE", "chip-neutral"),
        Map.entry("PENDIENTE_RUTA", "chip-neutral"),
        Map.entry("LISTA_PARA_ENTREGAR", "chip-info"),
        Map.entry("PLANIFICADA", "chip-info"),
        Map.entry("EN_CURSO", "chip-info"),
        Map.entry("FINALIZADA", "chip-success"),
        Map.entry("CANCELADA", "chip-danger"),
        Map.entry("SATISFECHA", "chip-success"),
        Map.entry("ACTIVA", "chip-success"),
        Map.entry("EN_PROGRESO", "chip-info"),
        Map.entry("PENDIENTE_REGISTRO", "chip-neutral"),
        Map.entry("COMPLETO", "chip-success"),
        Map.entry("RECURRENTE", "chip-neutral"),
        Map.entry("EXTRAORDINARIA", "chip-info"));
    public String etiqueta(String valor) {
        if (valor == null) return "—";
        String texto = ETIQUETAS.getOrDefault(valor, valor.replace('_', ' ').toLowerCase(Locale.ROOT));
        return texto.isEmpty() ? "—" : texto.substring(0,1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
    public String tono(String valor) { return valor == null ? "chip-neutral" : TONOS.getOrDefault(valor, "chip-neutral"); }
}

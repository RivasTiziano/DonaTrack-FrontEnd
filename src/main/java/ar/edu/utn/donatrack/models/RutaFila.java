package ar.edu.utn.donatrack.models;

import ar.edu.utn.donatrack.dto.ApiDtos;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Fila de la tabla de rutas del admin: número, patente del camión y entregas que lleva. */
public record RutaFila(
        Long id,
        String chofer,
        LocalDate fecha,
        String estado,
        String camionPatente,
        List<Long> entregaIds,
        Double totalDistanciaKm,
        Integer totalDuracionMins
) {
    public static RutaFila de(ApiDtos.Ruta ruta, String patente) {
        List<Long> ids = ruta.paradas() == null ? List.of() : ruta.paradas().stream()
                .filter(Objects::nonNull)
                .flatMap(p -> p.entregas() == null ? java.util.stream.Stream.empty() : p.entregas().stream())
                .filter(Objects::nonNull).map(ApiDtos.Entrega::id).filter(Objects::nonNull)
                .distinct().toList();
        return new RutaFila(ruta.id(), ruta.chofer(), ruta.fecha(), ruta.estado(),
                patente == null || patente.isBlank() ? "—" : patente, ids,
                ruta.totalDistanciaKm(), ruta.totalDuracionMins());
    }

    /** Ej.: "2 · #13, #14"; "0" si la ruta no tiene entregas. */
    public String entregasTexto() {
        if (entregaIds.isEmpty()) return "0";
        return entregaIds.size() + " · " + entregaIds.stream().map(id -> "#" + id).collect(Collectors.joining(", "));
    }
}

package ar.edu.utn.donatrack.models;

import ar.edu.utn.donatrack.dto.ApiDtos;
import java.util.Objects;
import java.util.stream.Collectors;

/** Fila de la tabla de entregas del admin: número, entidad destino y bienes, además de los datos de la entrega. */
public record EntregaFila(
        Long id,
        Long asignacionId,
        String estado,
        String justificacion,
        String camionPatente,
        String destino,
        String bienes
) {
    public static EntregaFila de(ApiDtos.Entrega entrega, String entidad) {
        return new EntregaFila(entrega.id(), entrega.asignacionId(), entrega.estado(), entrega.justificacion(),
                entrega.camionPatente(),
                entidad == null || entidad.isBlank() ? "—" : entidad,
                bienesTexto(entrega));
    }

    /** Ej.: "Fideos (5), Arroz (2,5)"; "—" si la entrega no informa bienes. */
    private static String bienesTexto(ApiDtos.Entrega entrega) {
        if (entrega.bienes() == null) return "—";
        String texto = entrega.bienes().stream().filter(Objects::nonNull)
                .filter(b -> b.descripcion() != null && !b.descripcion().isBlank())
                .map(b -> b.descripcion() + (b.cantidad() == null ? "" : " (" + cantidad(b.cantidad()) + ")"))
                .collect(Collectors.joining(", "));
        return texto.isEmpty() ? "—" : texto;
    }

    private static String cantidad(Float cantidad) {
        return cantidad % 1 == 0 ? String.valueOf(cantidad.longValue()) : String.valueOf(cantidad).replace('.', ',');
    }
}

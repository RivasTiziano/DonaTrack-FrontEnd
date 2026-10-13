package ar.edu.utn.donatrack.dto;

import java.time.LocalDateTime;

/**
 * Punto del mapa público (req. 2): una entrega ya entregada, ubicada en la entidad que la recibió.
 * Espejo de EntregaMapa del back: sin descripciones libres, sin donante y sin calle ni número.
 */
public record EntregaMapaDto(Long id, LocalDateTime fechaEntrega, String categoria, String subcategoria,
                             Float cantidad, String unidad, EntidadUbicada entidad) {
    public record EntidadUbicada(String nombre, String localidad, String provincia, Float latitud, Float longitud) {}
}

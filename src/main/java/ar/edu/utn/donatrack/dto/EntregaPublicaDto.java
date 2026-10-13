package ar.edu.utn.donatrack.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entrega pública (req. 1 y 2). Espejo de EntregaPublica del back: sin personas, contactos ni dirección exacta.
 * De la entidad solo el nombre y la localidad; las fotos son las que el admin revisó al publicarla.
 */
public record EntregaPublicaDto(Long id, String descripcion, LocalDateTime fechaEntrega,
                               Float cantidad, String unidad, String imagen,
                               String categoria, String subcategoria, EntidadResumen entidad, List<String> fotos) {
    public record EntidadResumen(String nombre, String localidad) {}
}

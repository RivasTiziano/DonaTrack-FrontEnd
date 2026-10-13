package ar.edu.utn.donatrack.dto;
import java.time.LocalDateTime;
/** Public delivery details without personal or contact information. */
public record EntregaPublicaDto(Long id, String descripcion, LocalDateTime fechaEntrega,
                               Float cantidad, String unidad, String imagen, Double latitud, Double longitud) {
    public EntregaPublicaDto(Long id, String descripcion, LocalDateTime fechaEntrega,
                             Float cantidad, String unidad, String imagen) {
        this(id, descripcion, fechaEntrega, cantidad, unidad, imagen, null, null);
    }
}
package ar.edu.utn.donatrack.dto;
import java.time.LocalDateTime;
/** Public contract: deliberately does not include donor, beneficiary or location. */
public record EntregaPublicaDto(Long id, String descripcion, LocalDateTime fechaEntrega,
                               Float cantidad, String unidad, String imagen) {}

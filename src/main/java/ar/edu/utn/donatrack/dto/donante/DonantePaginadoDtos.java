package ar.edu.utn.donatrack.dto.donante;

import java.util.List;
import java.util.Map;

public final class DonantePaginadoDtos {
    private DonantePaginadoDtos() {}

    public record DonanteResumen(
            Long id,
            String tipoPersona,
            String nombreVisible,
            String tipoDocumento,
            String numeroDocumento,
            String email,
            String telefono,
            String estadoRegistro
    ) {}

    public record PaginaDonantes(
            List<DonanteResumen> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record NombresMap(Map<Long, String> nombres) {}
}

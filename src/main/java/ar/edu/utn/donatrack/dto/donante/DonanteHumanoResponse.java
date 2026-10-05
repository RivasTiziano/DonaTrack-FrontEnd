package ar.edu.utn.donatrack.dto.donante;
import java.util.List;

public record DonanteHumanoResponse(
        Long id,
        String nombre,
        String apellido,
        String tipoDocumento,
        String numeroDocumento,
        List<MedioDeContactoDto> mediosDeContacto,
        String estadoRegistro
) {}
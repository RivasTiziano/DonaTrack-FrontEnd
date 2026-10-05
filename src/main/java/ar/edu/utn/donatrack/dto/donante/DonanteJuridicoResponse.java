package ar.edu.utn.donatrack.dto.donante;

import java.util.List;

public record DonanteJuridicoResponse(
        Long id,
        String razonSocial,
        String numeroDocumento,
        List<MedioDeContactoDto> mediosDeContacto,
        String estadoRegistro
) {}
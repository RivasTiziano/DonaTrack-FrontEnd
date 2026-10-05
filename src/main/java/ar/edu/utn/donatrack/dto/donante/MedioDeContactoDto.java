package ar.edu.utn.donatrack.dto.donante;

// Utilizamos record ya que es una clase inmutable y no necesitamos setters ni getters

public record MedioDeContactoDto(
        String tipoMedioContacto,
        String formaContacto
) {}
package ar.edu.utn.donatrack.dto;

import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public final class DonanteRequests {
    private DonanteRequests() {}
    public record Direccion(String calle, String ciudad, String provincia, String numero, Float latitud, Float longitud) {}
    public record Humano(String nombre, String apellido,
                         @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate fechaNacimiento, String numeroDocumento,
                         String genero, Direccion direccion, List<MedioDeContactoDto> mediosDeContacto,
                         MedioDeContactoDto medioDeContactoPredeterminado) {}
    public record Representante(Long id, String nombre, String apellido, String tipoDocumento,
                               String numeroDocumento,
                               @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate fechaNacimiento, String genero) {}
    public record Juridico(String numeroDocumento, String razonSocial, String tipoEntidad, String rubro,
                           Direccion direccion, List<MedioDeContactoDto> mediosDeContacto,
                           MedioDeContactoDto medioDeContactoPredeterminado,
                           List<Representante> representantesJuridicos) {}
}

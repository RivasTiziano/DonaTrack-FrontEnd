package ar.edu.utn.donatrack.dto;

import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;

/** Datos completos para editar sin descartar contactos o representantes existentes. */
public final class DonacionesDetalles {
    private DonacionesDetalles() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Humano(Long id, String nombre, String apellido, LocalDate fechaNacimiento,
                         String tipoDocumento, String numeroDocumento, String genero,
                         DonanteRequests.Direccion direccion, List<MedioDeContactoDto> mediosDeContacto,
                         MedioDeContactoDto medioDeContactoPredeterminado, String estadoRegistro) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Juridico(Long id, String numeroDocumento, String razonSocial, String tipoEntidad,
                           String rubro, DonanteRequests.Direccion direccion,
                           List<MedioDeContactoDto> mediosDeContacto, MedioDeContactoDto medioDeContactoPredeterminado,
                           List<DonanteRequests.Representante> personasRepresentantes, String estadoRegistro) {}

    public record Representante(Long id, String nombre, String telefono, List<MedioDeContactoDto> correos) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Beneficiario(Long id, String razonSocial, String tipoEntidad, String rubro,
                              DonanteRequests.Direccion direccion, MedioDeContactoDto medioDeContactoPredeterminado,
                              List<Representante> representantes, boolean activo) {}

    public record CrearBeneficiario(String razonSocial, String tipoEntidad, String rubro,
                                    DonanteRequests.Direccion direccion, MedioDeContactoDto medioDeContactoPredeterminado,
                                    List<Representante> representantes) {}

    public record ActualizarBeneficiario(String razonSocial, String tipoEntidad, String rubro,
                                        DonanteRequests.Direccion direccion, MedioDeContactoDto medioDeContactoPredeterminado,
                                        List<Representante> representantes, Boolean activo) {}

    public record Destinatario(String nombre, String email, String telefono) {}
    public record Contacto(Destinatario destinatario, String medioContacto) {}
    public record Contactos(Contacto donante, Contacto entidadBeneficiaria) {}
}

package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.dto.donante.*;
import ar.edu.utn.donatrack.forms.*;
import ar.edu.utn.donatrack.models.DonanteFila;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

/** Transformación de formularios y respuestas; no hace HTTP ni decide reglas del dominio. */
@Component
public class DonacionesMapper {

    public record CamposContacto(String tipo,String valor) {}
    public record CamposRepresentante(String nombre,String apellido,String documento,LocalDate nacimiento,String genero) {}

    public Map<String,Object> camposDonante(DonanteForm f) {
        var contactos=new ArrayList<CamposContacto>();
        for(int i=0;i<Math.max(size(f.contactoTipo()),size(f.contactoValor()));i++)
            contactos.add(new CamposContacto(at(f.contactoTipo(),i),at(f.contactoValor(),i)));
        var representantes=new ArrayList<CamposRepresentante>();
        int rows=Math.max(size(f.repNombre()),Math.max(size(f.repApellido()),Math.max(size(f.repDocumento()),
                Math.max(size(f.repNacimiento()),size(f.repGenero())))));
        for(int i=0;i<rows;i++) representantes.add(new CamposRepresentante(at(f.repNombre(),i),at(f.repApellido(),i),
                at(f.repDocumento(),i),at(f.repNacimiento(),i),at(f.repGenero(),i)));
        return Map.of("contactosExtras",contactos,"representantesExtras",representantes);
    }

    public DonanteRequests.Direccion direccion(DonanteForm f) {
        return new DonanteRequests.Direccion(f.calle(), f.ciudad(), f.provincia(), f.numero(), f.latitud(), f.longitud());
    }

    public List<MedioDeContactoDto> contactos(DonanteForm f) {
        var result = new ArrayList<MedioDeContactoDto>();
        if (!blank(f.email())) result.add(new MedioDeContactoDto("EMAIL", f.email()));
        if (!blank(f.telefono())) result.add(new MedioDeContactoDto("SMS", f.telefono()));
        for (int i = 0; i < size(f.contactoTipo()); i++) {
            String tipo = at(f.contactoTipo(), i), valor = at(f.contactoValor(), i);
            if (!blank(tipo) && !blank(valor)) result.add(new MedioDeContactoDto(tipo, valor));
        }
        return List.copyOf(result);
    }

    public MedioDeContactoDto predeterminado(DonanteForm f) {
        return blank(f.contactoPredeterminadoTipo())
                ? new MedioDeContactoDto("EMAIL", f.email())
                : new MedioDeContactoDto(f.contactoPredeterminadoTipo(), f.contactoPredeterminadoValor());
    }

    public List<DonanteRequests.Representante> representantes(DonanteForm f) {
        var result = new ArrayList<DonanteRequests.Representante>();
        if (!blank(f.representanteNombre())) result.add(new DonanteRequests.Representante(null,
                f.representanteNombre(), f.representanteApellido(), "DNI", f.representanteDocumento(),
                f.representanteNacimiento(), f.representanteGenero()));
        for (int i = 0; i < size(f.repNombre()); i++) {
            if (!blank(at(f.repNombre(), i))) result.add(new DonanteRequests.Representante(null,
                    at(f.repNombre(), i), at(f.repApellido(), i), "DNI", at(f.repDocumento(), i),
                    at(f.repNacimiento(), i), at(f.repGenero(), i)));
        }
        return List.copyOf(result);
    }

    public DonanteRequests.Humano humano(DonanteForm f) {
        return new DonanteRequests.Humano(f.nombre(), f.apellido(), f.fechaNacimiento(), f.numeroDocumento(),
                f.genero(), direccion(f), contactos(f), predeterminado(f));
    }

    public DonanteRequests.Juridico juridico(DonanteForm f) {
        return new DonanteRequests.Juridico(f.numeroDocumento(), f.razonSocial(), f.tipoEntidad(), f.rubro(),
                direccion(f), contactos(f), predeterminado(f), representantes(f));
    }

    public DonanteRequests.Humano humano(PerfilForm f, DonacionesDetalles.Humano actual) {
        return new DonanteRequests.Humano(f.nombre(), f.apellido(), f.fechaNacimiento(), f.numeroDocumento(),
                f.genero(), direccion(f), actual.mediosDeContacto(), actual.medioDeContactoPredeterminado());
    }

    public DonanteRequests.Juridico juridico(PerfilForm f, DonacionesDetalles.Juridico actual) {
        return new DonanteRequests.Juridico(f.numeroDocumento(), f.razonSocial(), f.tipoEntidad(), f.rubro(),
                direccion(f), actual.mediosDeContacto(), actual.medioDeContactoPredeterminado(), actual.personasRepresentantes());
    }

    public DonanteRequests.Direccion direccion(PerfilForm f) {
        return new DonanteRequests.Direccion(f.calle(), f.ciudad(), f.provincia(), f.numero(), f.latitud(), f.longitud());
    }

    public PerfilForm perfil(DonacionesDetalles.Humano p) {
        return perfil(p.nombre(), p.apellido(), p.fechaNacimiento(), p.numeroDocumento(), p.genero(),
                null, null, null, p.direccion(), p.medioDeContactoPredeterminado(), true);
    }

    public PerfilForm perfil(DonacionesDetalles.Juridico p) {
        return perfil(null, null, null, p.numeroDocumento(), null, p.razonSocial(),
                p.tipoEntidad(), p.rubro(), p.direccion(), p.medioDeContactoPredeterminado(), true);
    }

    public PerfilForm perfil(DonacionesDetalles.Beneficiario p) {
        return perfil(null, null, null, null, null, p.razonSocial(), p.tipoEntidad(),
                p.rubro(), p.direccion(), p.medioDeContactoPredeterminado(), p.activo());
    }

    public PerfilForm nuevoBeneficiario() {
        return perfil(null, null, null, null, null, "", "", "", null, new MedioDeContactoDto("EMAIL", ""), true);
    }

    public List<DonanteFila> filas(List<DonanteHumanoResponse> humanos, List<DonanteJuridicoResponse> juridicos) {
        var result = new ArrayList<DonanteFila>();
        humanos.forEach(d -> result.add(new DonanteFila(d.id(), "Humana", nombreCompleto(d.nombre(), d.apellido()),
                d.tipoDocumento(), d.numeroDocumento(), contacto(d.mediosDeContacto(), "EMAIL"),
                contacto(d.mediosDeContacto(), "SMS", "WHATSAPP"), d.estadoRegistro())));
        juridicos.forEach(d -> result.add(new DonanteFila(d.id(), "Jurídica", d.razonSocial(), "CUIT",
                d.numeroDocumento(), contacto(d.mediosDeContacto(), "EMAIL"),
                contacto(d.mediosDeContacto(), "SMS", "WHATSAPP"), d.estadoRegistro())));
        return List.copyOf(result);
    }

    public List<DonacionesDetalles.Representante> representantesBeneficiario(List<String> nombres, List<String> telefonos, List<String> emails) {
        var result = new ArrayList<DonacionesDetalles.Representante>();
        for (int i = 0; i < size(nombres); i++) {
            if (blank(at(nombres, i)) && blank(at(telefonos, i)) && blank(at(emails, i))) continue;
            result.add(new DonacionesDetalles.Representante(null, at(nombres, i), at(telefonos, i),
                    List.of(new MedioDeContactoDto("EMAIL", at(emails, i)))));
        }
        return List.copyOf(result);
    }

    public ApiRequests.Bien bien(BienForm f) {
        return new ApiRequests.Bien(f.descripcion(), f.foto(), f.cantidad(), f.subCategoriaId(), f.unidadMedida(),
                f.tipoBien(), f.fechaDeVencimiento(), f.fueUsado());
    }

    public List<ApiRequests.Bien> bienes(BienForm inicial, BienesAdicionalesForm extra) {
        var result = new ArrayList<ApiRequests.Bien>();
        if (!blank(inicial.descripcion())) result.add(bien(inicial));
        for (int i = 0; i < size(extra.bienDescripcion()); i++) {
            if (blank(at(extra.bienDescripcion(), i))) continue;
            result.add(new ApiRequests.Bien(at(extra.bienDescripcion(), i), at(extra.bienFoto(), i),
                    at(extra.bienCantidad(), i), at(extra.bienSubCategoriaId(), i), at(extra.bienUnidadMedida(), i),
                    at(extra.bienTipoBien(), i), at(extra.bienFechaDeVencimiento(), i), at(extra.bienFueUsado(), i)));
        }
        return List.copyOf(result);
    }

    public PerfilForm perfil(String nombre, String apellido, java.time.LocalDate fecha, String documento,
                            String genero, String razon, String tipo, String rubro, DonanteRequests.Direccion d,
                            MedioDeContactoDto contacto, boolean activo) {
        return new PerfilForm(nombre, apellido, fecha, documento, genero, razon, tipo, rubro,
                d == null ? "" : d.calle(), d == null ? "" : d.numero(), d == null ? "" : d.ciudad(),
                d == null ? "" : d.provincia(), d == null ? null : d.latitud(), d == null ? null : d.longitud(),
                contacto == null ? null : contacto.tipoMedioContacto(),
                contacto == null ? null : contacto.formaContacto(), activo);
    }
    private String nombreCompleto(String nombre, String apellido) {
        return Stream.of(nombre, apellido)
                .filter(Objects::nonNull)
                .filter(valor -> !valor.isBlank())
                .collect(Collectors.joining(" "));
    }

private String contacto(
            List<MedioDeContactoDto> contactos,
            String... tipos
    ) {
        if (contactos == null) {
            return "—";
        }

        return contactos.stream()
                .filter(contacto ->
                        List.of(tipos).contains(contacto.tipoMedioContacto()))
                .map(MedioDeContactoDto::formaContacto)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("—");
    }

    public DonacionesDetalles.CrearBeneficiario beneficiario(PerfilForm f,List<String> nombres,List<String> telefonos,List<String> emails) {
        return new DonacionesDetalles.CrearBeneficiario(f.razonSocial(),f.tipoEntidad(),f.rubro(),direccion(f),
                new MedioDeContactoDto(f.contactoTipo(),f.contactoValor()),representantesBeneficiario(nombres,telefonos,emails));
    }

    public DonacionesDetalles.ActualizarBeneficiario beneficiario(PerfilForm f,DonacionesDetalles.Beneficiario actual) {
        return new DonacionesDetalles.ActualizarBeneficiario(f.razonSocial(),f.tipoEntidad(),f.rubro(),direccion(f),
                new MedioDeContactoDto(f.contactoTipo(),f.contactoValor()),actual.representantes(),f.activo());
    }

    public ApiRequests.Donacion donacion(Long id,String descripcion,List<ApiRequests.Bien> bienes) {
        return new ApiRequests.Donacion(id,descripcion,bienes);
    }

    public ApiRequests.Necesidad necesidad(Long entidadId,String descripcion,String tipo,String periodo,ApiRequests.Bien bien) {
        return new ApiRequests.Necesidad(entidadId,bien,descripcion,tipo,periodo(periodo));
    }

    public ApiRequests.ActualizarNecesidad necesidad(String descripcion,String tipo,String periodo,ApiRequests.Bien bien) {
        return new ApiRequests.ActualizarNecesidad(bien,descripcion,tipo,periodo(periodo));
    }

    public ApiRequests.EstadoDonacion estado(String estado,String justificacion) { return new ApiRequests.EstadoDonacion(null,estado,justificacion); }
    public ApiRequests.Asignacion asignacion(Long donacionId,Long necesidadId,Float cantidad) { return new ApiRequests.Asignacion(donacionId,necesidadId,cantidad); }
    public ApiRequests.Algoritmo algoritmo(String nombre,boolean activo) { return new ApiRequests.Algoritmo(nombre,activo); }
    private String periodo(String value) { return blank(value)?null:value; }

    private boolean blank(String s) { return s == null || s.isBlank(); }
    private int size(List<?> values) { return values == null ? 0 : values.size(); }
    private <T> T at(List<T> values, int i) { return values == null || i >= values.size() ? null : values.get(i); }
}

package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.DonanteRequests;
import ar.edu.utn.donatrack.dto.RegistroDtos;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.forms.RegistroForm;
import ar.edu.utn.donatrack.forms.RegistroForm.Representante;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Convierte el formulario del registro público en los cuerpos del contrato de Donaciones (E1).
 * Teléfono y WhatsApp van como SMS y WHATSAPP. El email de la cuenta es el contacto EMAIL del perfil.
 * Las cadenas en blanco no se envían. Asume que el formulario ya pasó RegistroValidator.
 */
@Component
public class RegistroMapper {

    public RegistroDtos.RegistroHumano humano(RegistroForm f) {
        var perfil = new DonanteRequests.Humano(t(f.getNombre()), t(f.getApellido()), fecha(f.getFechaNacimiento()),
                t(f.getDocumento()), f.getGenero(), direccion(f), contactos(f), predeterminado(f));
        return new RegistroDtos.RegistroHumano(perfil, email(f), f.getPassword());
    }

    public RegistroDtos.RegistroJuridico juridico(RegistroForm f) {
        List<DonanteRequests.Representante> representantes = f.getRepresentantes().stream()
                .filter(RegistroMapper::cargado).map(this::representanteJuridico).toList();
        var perfil = new DonanteRequests.Juridico(t(f.getDocumento()), t(f.getRazonSocial()), f.getTipoEntidad(),
                t(f.getRubro()), direccion(f), contactos(f), predeterminado(f), representantes);
        return new RegistroDtos.RegistroJuridico(perfil, email(f), f.getPassword());
    }

    public RegistroDtos.RegistroEntidad entidad(RegistroForm f) {
        List<DonacionesDetalles.Representante> representantes = f.getRepresentantes().stream()
                .filter(RegistroMapper::cargado).map(this::representanteEntidad).toList();
        var perfil = new DonacionesDetalles.CrearBeneficiario(t(f.getRazonSocial()), f.getTipoEntidad(), t(f.getRubro()),
                direccion(f), new MedioDeContactoDto("EMAIL", email(f)), representantes);
        return new RegistroDtos.RegistroEntidad(perfil, email(f), f.getPassword());
    }

    private DonanteRequests.Representante representanteJuridico(Representante r) {
        return new DonanteRequests.Representante(null, t(r.getNombre()), t(r.getApellido()), t(r.getTipoDocumento()),
                t(r.getDocumento()), fecha(r.getFechaNacimiento()), r.getGenero());
    }

    private DonacionesDetalles.Representante representanteEntidad(Representante r) {
        List<MedioDeContactoDto> correos = blank(r.getCorreo()) ? List.of()
                : List.of(new MedioDeContactoDto("EMAIL", lower(r.getCorreo())));
        return new DonacionesDetalles.Representante(null, t(r.getNombre()), n(r.getTelefono()), correos);
    }

    private static DonanteRequests.Direccion direccion(RegistroForm f) {
        return new DonanteRequests.Direccion(t(f.getCalle()), t(f.getCiudad()), t(f.getProvincia()), t(f.getNumero()), null, null);
    }

    private static List<MedioDeContactoDto> contactos(RegistroForm f) {
        List<MedioDeContactoDto> medios = new ArrayList<>();
        medios.add(new MedioDeContactoDto("EMAIL", email(f)));
        if (!blank(f.getTelefono())) medios.add(new MedioDeContactoDto("SMS", t(f.getTelefono())));
        if (!blank(f.getWhatsapp())) medios.add(new MedioDeContactoDto("WHATSAPP", t(f.getWhatsapp())));
        return medios;
    }

    private static MedioDeContactoDto predeterminado(RegistroForm f) {
        return switch (String.valueOf(f.getPredeterminado())) {
            case "SMS" -> new MedioDeContactoDto("SMS", t(f.getTelefono()));
            case "WHATSAPP" -> new MedioDeContactoDto("WHATSAPP", t(f.getWhatsapp()));
            default -> new MedioDeContactoDto("EMAIL", email(f));
        };
    }

    // Un bloque de representante se envía solo si tiene algún dato cargado.
    private static boolean cargado(Representante r) {
        return !blank(r.getNombre()) || !blank(r.getApellido()) || !blank(r.getDocumento()) || !blank(r.getFechaNacimiento())
                || !blank(r.getGenero()) || !blank(r.getTelefono()) || !blank(r.getCorreo());
    }

    private static String email(RegistroForm f) { return lower(f.getEmail()); }

    private static String lower(String s) { return t(s).toLowerCase(Locale.ROOT); }

    private static LocalDate fecha(String s) { return LocalDate.parse(s.trim()); }

    private static String t(String s) { return s == null ? null : s.trim(); }

    private static String n(String s) { return blank(s) ? null : s.trim(); }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}

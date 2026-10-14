package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.AccesoDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.exceptions.PerfilNoEncontradoException;
import ar.edu.utn.donatrack.validators.AccesoValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import java.util.stream.Stream;

/**
 * Enviar acceso (mail de bienvenida para el primer acceso, Entrega 1) a un donante o entidad ya cargado;
 * el back crea o renueva la cuenta pendiente y decide el estado.
 * El controller lee el perfil (email actual) antes de enviar. El rol sale del tipo de perfil; nunca es ADMIN.
 */
@Service
public class AccesoService {

    /** Perfil con el que se envía el acceso. El email sale del perfil (null si no tiene un email de contacto). */
    public record Acceso(String tipo, Long id, String rol, String nombre, String email) {}

    private final DonacionesService donaciones;
    private final AccesoValidator validator;

    public AccesoService(DonacionesService donaciones, AccesoValidator validator) {
        this.donaciones = donaciones; this.validator = validator;
    }

    /** Lee el perfil en Donaciones. Si no existe, lanza PerfilNoEncontradoException (404). */
    public Acceso perfil(String tipo, Long id) {
        try {
            return switch (tipo) {
                case "humanos" -> humano(id);
                case "juridicos" -> juridico(id);
                case "beneficiarios" -> beneficiaria(id);
                default -> throw new PerfilNoEncontradoException();
            };
        } catch (HttpClientErrorException.NotFound exception) {
            throw new PerfilNoEncontradoException();
        }
    }

    /** Completa el contacto sin cambiar el estado pendiente. Solo para donantes pendientes sin email. */
    public void completarEmail(String tipo,Long id,String email) {
        if("beneficiarios".equals(tipo) || !"PENDIENTE_COMPLETAR".equals(estadoRegistro(tipo, id)))
            throw new FormularioInvalidoException("Este perfil no requiere completar el contacto.");
        if(perfil(tipo,id).email()!=null)
            throw new FormularioInvalidoException("El donante ya tiene un email de contacto.");
        validator.validarEmail(email);
        donaciones.completarEmail(tipo,id,email.trim().toLowerCase(java.util.Locale.ROOT));
    }

    /** Envía (o reenvía) el acceso. Sin email de contacto no se llama a la API. */
    public AccesoDtos.Respuesta enviar(Acceso perfil) {
        if (perfil.email() == null)
            throw new FormularioInvalidoException("El perfil no tiene un email de contacto; cargalo antes de enviar el acceso.");
        return donaciones.enviarAcceso(perfil.tipo(), perfil.id());
    }

    private String estadoRegistro(String tipo, Long id) {
        return "humanos".equals(tipo) ? donaciones.humano(id).estadoRegistro() : donaciones.juridico(id).estadoRegistro();
    }

    private Acceso humano(Long id) {
        DonacionesDetalles.Humano humano = donaciones.humano(id);
        return new Acceso("humanos", id, "DONANTE_HUMANO",
                nombreCompleto(humano.nombre(), humano.apellido()), email(humano.mediosDeContacto(), humano.medioDeContactoPredeterminado()));
    }

    private Acceso juridico(Long id) {
        DonacionesDetalles.Juridico juridico = donaciones.juridico(id);
        return new Acceso("juridicos", id, "DONANTE_JURIDICO", juridico.razonSocial(),
                email(juridico.mediosDeContacto(), juridico.medioDeContactoPredeterminado()));
    }

    private Acceso beneficiaria(Long id) {
        DonacionesDetalles.Beneficiario entidad = donaciones.beneficiario(id);
        return new Acceso("beneficiarios", id, "BENEFICIARIA", entidad.razonSocial(),
                email(entidad.medioDeContactoPredeterminado()));
    }

    private static String email(java.util.List<MedioDeContactoDto> contactos, MedioDeContactoDto predeterminado) {
        String valor = email(predeterminado);
        if (valor != null) return valor;
        return contactos == null ? null : contactos.stream().map(AccesoService::email).filter(java.util.Objects::nonNull).findFirst().orElse(null);
    }

    private static String email(MedioDeContactoDto medio) {
        if (medio == null || !"EMAIL".equalsIgnoreCase(medio.tipoMedioContacto())) return null;
        String forma = medio.formaContacto();
        return forma == null || forma.isBlank() ? null : forma.trim();
    }

    private static String nombreCompleto(String nombre, String apellido) {
        return Stream.of(nombre, apellido).filter(parte -> parte != null && !parte.isBlank()).reduce((a, b) -> a + " " + b).orElse("");
    }
}

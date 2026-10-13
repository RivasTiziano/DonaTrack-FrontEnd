package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.exceptions.PerfilNoEncontradoException;
import ar.edu.utn.donatrack.validators.AccesoValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import java.util.stream.Stream;

/**
 * Alta de acceso (usuario y contraseña inicial) para un donante o una entidad ya existente en Donaciones.
 * El backend de autenticación no valida que el referenciaId exista ni que coincida con el rol, así que el perfil se relee
 * antes de mostrar el formulario y antes de crear el usuario. El rol sale del tipo de perfil; nunca es ADMIN.
 */
@Service
public class AccesoService {

    /** Perfil con el que se crea el acceso. El email sale del perfil (null si no tiene un email de contacto). */
    public record Acceso(String tipo, Long id, String rol, String nombre, String email) {}

    private final DonacionesService donaciones;
    private final AutenticacionApiService auth;
    private final AccesoValidator validator;

    public AccesoService(DonacionesService donaciones, AutenticacionApiService auth, AccesoValidator validator) {
        this.donaciones = donaciones; this.auth = auth; this.validator = validator;
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

    /** Crea el usuario. La contraseña se envía al backend y no se conserva. */
    public void crear(Acceso perfil, String password, String confirmacion) {
        if (perfil.email() == null)
            throw new FormularioInvalidoException("El perfil no tiene un email de contacto; cargalo antes de crear el acceso.");
        validator.validar(password, confirmacion);
        // PROVISORIO: reemplazar por flujo de primer acceso con CuentaCreada (pedido backend E2)
        try {
            auth.crearUsuario(new AuthDtos.CrearUsuario(perfil.email(), password, perfil.rol(), perfil.id()));
        } catch (HttpClientErrorException.Conflict exception) {
            throw new FormularioInvalidoException("Ya existe un usuario con ese email.");
        }
    }

    private Acceso humano(Long id) {
        DonacionesDetalles.Humano humano = donaciones.humano(id);
        return new Acceso("humanos", id, "DONANTE_HUMANO",
                nombreCompleto(humano.nombre(), humano.apellido()), email(humano.medioDeContactoPredeterminado()));
    }

    private Acceso juridico(Long id) {
        DonacionesDetalles.Juridico juridico = donaciones.juridico(id);
        return new Acceso("juridicos", id, "DONANTE_JURIDICO", juridico.razonSocial(),
                email(juridico.medioDeContactoPredeterminado()));
    }

    private Acceso beneficiaria(Long id) {
        DonacionesDetalles.Beneficiario entidad = donaciones.beneficiario(id);
        return new Acceso("beneficiarios", id, "BENEFICIARIA", entidad.razonSocial(),
                email(entidad.medioDeContactoPredeterminado()));
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

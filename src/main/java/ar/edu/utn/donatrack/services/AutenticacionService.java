package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.mappers.AutenticacionMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.Map;

/** Login contra el servicio de autenticación y resolución del nombre que se muestra en el panel. */
@Service
public class AutenticacionService {
    private final AutenticacionMapper mapper;
    private final AutenticacionApiService authApi;
    private final DonacionesApiService donaciones;

    public AutenticacionService(AutenticacionMapper mapper, AutenticacionApiService authApi, DonacionesApiService donaciones) {
        this.mapper = mapper;
        this.authApi = authApi;
        this.donaciones = donaciones;
    }

    public Map<String,Object> register() { return mapper.opcionesRegistro(); }

    /** Pide el token. Las credenciales incorrectas llegan como HttpClientErrorException.Unauthorized. */
    public AuthDtos.Token login(String email, String password) {
        AuthDtos.Token token = authApi.login(email, password);
        if (token == null || token.accessToken() == null)
            throw new IllegalStateException("El servicio de autenticación no devolvió un token.");
        return token;
    }

    /** Primer acceso: guarda la contraseña inicial con el enlace del correo. Los errores llegan como RestClientException. */
    public void definirContrasenia(String token, String contrasenia) {
        authApi.definirContrasenia(token, contrasenia);
    }

    /**
     * Nombre para mostrar: razón social o nombre del donante o de la entidad, Administración para ADMIN.
     * Si el perfil no se puede leer, queda la etiqueta del rol. Necesita que la sesión con el token ya esté guardada.
     */
    public String nombreVisible(String email, AuthDtos.Token token) {
        String fallback = switch (token.rol()) { case "ADMIN" -> "Administración"; case "DONANTE_JURIDICO" -> "Donante jurídico"; case "BENEFICIARIA" -> "Entidad beneficiaria"; default -> "Donante"; };
        if ("ADMIN".equals(token.rol()) || token.referenciaId() == null) return fallback;
        try {
            String nombre = switch (token.rol()) {
                case "DONANTE_HUMANO" -> {
                    var humano = donaciones.humano(token.referenciaId());
                    yield unir(humano.nombre(), humano.apellido());
                }
                case "DONANTE_JURIDICO" -> donaciones.juridico(token.referenciaId()).razonSocial();
                case "BENEFICIARIA" -> donaciones.beneficiario(token.referenciaId()).razonSocial();
                default -> null;
            };
            return nombre == null || nombre.isBlank() ? fallback : nombre;
        } catch (RestClientException | IllegalStateException exception) {
            return fallback;
        }
    }

    private String unir(String nombre, String apellido) {
        return java.util.stream.Stream.of(nombre, apellido)
                .filter(parte -> parte != null && !parte.isBlank())
                .reduce((a, b) -> a + " " + b).orElse(null);
    }
}

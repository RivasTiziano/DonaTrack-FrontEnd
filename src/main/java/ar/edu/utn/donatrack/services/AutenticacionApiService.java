package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Cliente del servicio de autenticación (puerto 8084).
 * Login y refresh usan un RestClient propio, sin el interceptor de token: así el refresco no se llama a sí mismo.
 */
@Service
public class AutenticacionApiService {
    private final RestClient client = RestClient.builder().requestFactory(fabrica()).build();
    private final WebApiCallerService http;
    private final String url;

    public AutenticacionApiService(WebApiCallerService http, @Value("${apis.autenticacion.url}") String url) {
        this.http = http;
        this.url = url.replaceAll("/+$", "");
    }

    public AuthDtos.Token login(String email, String password) {
        return client.post().uri(url + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.Login(email, password)).retrieve().body(AuthDtos.Token.class);
    }

    public AuthDtos.Token refrescar(String refreshToken) {
        return client.post().uri(url + "/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.Refresh(refreshToken)).retrieve().body(AuthDtos.Token.class);
    }

    // Sin timeouts, un servicio de autenticación caído dejaría colgado el login.
    private static JdkClientHttpRequestFactory fabrica() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }

    /** Requiere el token de un ADMIN (GESTIONAR_USUARIOS): por eso usa WebApiCallerService, que agrega el Bearer. */
    public AuthDtos.Usuario crearUsuario(AuthDtos.CrearUsuario request) {
        return http.post(url + "/auth/usuarios", request, AuthDtos.Usuario.class);
    }
}

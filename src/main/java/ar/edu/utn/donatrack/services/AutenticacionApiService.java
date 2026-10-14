package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.AuthDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Cliente del servicio de autenticación (puerto 8084).
 * Login, refresh y primer acceso usan un RestClient propio, sin el interceptor de token: así el refresco no se llama a sí mismo.
 */
@Service
public class AutenticacionApiService {
    private final RestClient client = RestClient.builder().requestFactory(fabrica()).build();
    private final String url;

    public AutenticacionApiService(@Value("${apis.autenticacion.url}") String url) {
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

    /** Guarda la contraseña inicial con el enlace del correo de bienvenida. El back responde 204. */
    public void definirContrasenia(String token, String contrasenia) {
        client.post().uri(url + "/auth/primer-acceso").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.PrimerAcceso(token, contrasenia)).retrieve().toBodilessEntity();
    }

    // Sin timeouts, un servicio de autenticación caído dejaría colgado el login.
    private static JdkClientHttpRequestFactory fabrica() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }
}

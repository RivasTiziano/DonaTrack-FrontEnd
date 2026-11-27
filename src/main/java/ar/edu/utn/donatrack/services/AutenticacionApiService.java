package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.services.internal.IpDelNavegadorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Cliente del servicio de autenticación (puerto 8084).
 * Login, refresh, logout y primer acceso usan un RestClient propio, sin el interceptor de token: así el refresco
 * no se llama a sí mismo. Sí lleva el de la IP del navegador, para que el backend limite el login por visitante.
 */
@Service
public class AutenticacionApiService {
    private final RestClient client;
    private final String url;

    /** Sin IP del navegador: lo usan los tests que llaman a un servidor local. */
    public AutenticacionApiService(String url) {
        this(url, null);
    }

    @Autowired
    public AutenticacionApiService(@Value("${apis.autenticacion.url}") String url, IpDelNavegadorInterceptor ipDelNavegador) {
        this.url = url.replaceAll("/+$", "");
        var builder = RestClient.builder().requestFactory(fabrica());
        if (ipDelNavegador != null) builder.requestInterceptor(ipDelNavegador);
        this.client = builder.build();
    }

    public AuthDtos.Token login(String email, String password) {
        return client.post().uri(url + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.Login(email, password)).retrieve().body(AuthDtos.Token.class);
    }

    public AuthDtos.Token refrescar(String refreshToken) {
        return client.post().uri(url + "/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.Refresh(refreshToken)).retrieve().body(AuthDtos.Token.class);
    }

    /** Revoca en el servidor los refresh tokens de la cuenta (todas sus sesiones). El back responde 204. */
    public void logout(String refreshToken) {
        client.post().uri(url + "/auth/logout").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.Refresh(refreshToken)).retrieve().toBodilessEntity();
    }

    /** Guarda la contraseña inicial con el enlace del correo de bienvenida. El back responde 204. */
    public void definirContrasenia(String token, String contrasenia) {
        client.post().uri(url + "/auth/primer-acceso").contentType(MediaType.APPLICATION_JSON)
                .body(new AuthDtos.PrimerAcceso(token, contrasenia)).retrieve().toBodilessEntity();
    }

    // Sin timeouts, un servicio de autenticación caído dejaría colgado el login.
    private static JdkClientHttpRequestFactory fabrica() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build());
        factory.setReadTimeout(Duration.ofSeconds(35));
        return factory;
    }
}

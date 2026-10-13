package ar.edu.utn.donatrack.services.internal;

import ar.edu.utn.donatrack.exceptions.SesionExpiradaException;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Infraestructura HTTP compartida. No contiene reglas de negocio.
 * Si hay una sesión activa, agrega "Authorization: Bearer" a cada llamada y refresca el access token antes de que venza.
 */
@Service
public class WebApiCallerService {
    private final RestClient client;

    /** Sin sesión: lo usan los tests que llaman a un servidor local sin token. */
    public WebApiCallerService(RestClient.Builder builder) {
        this(builder, null, null);
    }

    @Autowired
    public WebApiCallerService(RestClient.Builder builder, ObjectProvider<SesionActual> sesiones,
                               ObjectProvider<AutenticacionApiService> autenticacion) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        // CSV puede procesar más de 10.000 registros; no se reintentan escrituras.
        factory.setReadTimeout(Duration.ofSeconds(120));
        if (sesiones != null) builder.requestInterceptor(new TokenInterceptor(sesiones, autenticacion));
        client = builder.requestFactory(factory).build();
    }

    public <T> T get(String url, Class<T> type) {
        T result = client.get().uri(url).retrieve().body(type);
        if (result == null) throw new IllegalStateException("La API no devolvió los datos esperados");
        return result;
    }

    /** Para URLs ya armadas y codificadas (por ejemplo con query params que contienen '+' o '@'). */
    public <T> T get(URI url, Class<T> type) {
        T result = client.get().uri(url).retrieve().body(type);
        if (result == null) throw new IllegalStateException("La API no devolvió los datos esperados");
        return result;
    }

    /** PATCH sin cuerpo: la operación se identifica por la URL. */
    public void patch(URI url) {
        client.patch().uri(url).retrieve().toBodilessEntity();
    }

    public <T> List<T> getList(String url, Class<T> type) {
        T[] result = get(url, arrayType(type));
        if (result == null) throw new IllegalStateException("La API no devolvió la lista esperada");
        return List.of(result);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T[]> arrayType(Class<T> type) {
        return (Class<T[]>) java.lang.reflect.Array.newInstance(type, 0).getClass();
    }

    public <T> T post(String url, Object body, Class<T> type) {
        return client.post().uri(url).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(type);
    }

    public <T> T post(String url, Object body, ParameterizedTypeReference<T> type) {
        return client.post().uri(url).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(type);
    }

    public void update(HttpMethod method, String url, Object body) {
        client.method(method).uri(url).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toBodilessEntity();
    }

    public void delete(String url) {
        client.delete().uri(url).retrieve().toBodilessEntity();
    }

    public <T> T upload(String url, MultipartFile file, Class<T> type) {
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("archivo", file.getResource());
        return client.post().uri(url).contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts).retrieve().body(type);
    }

    /** Pone el Bearer de la sesión actual y refresca el token si está por vencer. Sin sesión, no agrega nada. */
    private static final class TokenInterceptor implements ClientHttpRequestInterceptor {
        private final ObjectProvider<SesionActual> sesiones;
        private final ObjectProvider<AutenticacionApiService> autenticacion;

        TokenInterceptor(ObjectProvider<SesionActual> sesiones, ObjectProvider<AutenticacionApiService> autenticacion) {
            this.sesiones = sesiones;
            this.autenticacion = autenticacion;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            SesionActual actual = sesiones.getIfAvailable();
            Optional<SesionUsuario> sesion = actual == null ? Optional.empty() : actual.obtener();
            if (sesion.isPresent()) {
                SesionUsuario usuario = sesion.get();
                if (usuario.vencePronto()) renovar(usuario);
                request.getHeaders().setBearerAuth(usuario.getAccessToken());
            }
            return execution.execute(request, body);
        }

        // Un solo refresco aunque lleguen varias llamadas en paralelo: la segunda revisa de nuevo dentro del bloque.
        private void renovar(SesionUsuario usuario) {
            synchronized (usuario) {
                if (!usuario.vencePronto()) return;
                AutenticacionApiService auth = autenticacion.getIfAvailable();
                if (auth == null) throw new SesionExpiradaException();
                try {
                    usuario.renovar(auth.refrescar(usuario.getRefreshToken()));
                } catch (HttpClientErrorException.Unauthorized exception) {
                    throw new SesionExpiradaException();
                }
            }
        }
    }
}

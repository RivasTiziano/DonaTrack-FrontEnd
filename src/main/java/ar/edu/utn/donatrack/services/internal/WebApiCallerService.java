package ar.edu.utn.donatrack.services.internal;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.net.http.HttpClient;
import java.time.Duration;

/** Infraestructura HTTP compartida. No contiene reglas de negocio ni tokens ficticios. */
@Service
public class WebApiCallerService {
    private final RestClient client;

    public WebApiCallerService(RestClient.Builder builder) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        // CSV puede procesar más de 10.000 registros; no se reintentan escrituras.
        factory.setReadTimeout(Duration.ofSeconds(120));
        client = builder.requestFactory(factory).build();
    }

    public <T> T get(String url, Class<T> type) {
        return client.get().uri(url).retrieve().body(type);
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
}

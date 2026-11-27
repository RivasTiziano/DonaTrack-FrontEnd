package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.RegistroDtos;
import ar.edu.utn.donatrack.services.internal.IpDelNavegadorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.net.http.HttpClient;
import java.time.Duration;

/** Cliente del registro público de Donaciones. Sin Bearer: el alta no tiene sesión. */
@Service
public class RegistroApiService {
    private final RestClient client;
    private final String url;

    /** Sin IP del navegador: lo usan los tests que llaman a un servidor local. */
    public RegistroApiService(String url) {
        this(url, null);
    }

    // La IP del navegador hace que el límite de registros del backend sea por visitante y no por la IP del front
    @Autowired
    public RegistroApiService(@Value("${apis.donaciones.url}") String url, IpDelNavegadorInterceptor ipDelNavegador) {
        this.url = url.replaceAll("/+$", "");
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build());
        factory.setReadTimeout(Duration.ofSeconds(35));
        var builder = RestClient.builder().requestFactory(factory);
        if (ipDelNavegador != null) builder.requestInterceptor(ipDelNavegador);
        this.client = builder.build();
    }

    public RegistroDtos.Registrado registrarHumano(RegistroDtos.RegistroHumano request) {
        return post("/publico/registro/donantes-humanos", request);
    }

    public RegistroDtos.Registrado registrarJuridico(RegistroDtos.RegistroJuridico request) {
        return post("/publico/registro/donantes-juridicos", request);
    }

    public RegistroDtos.Registrado registrarEntidad(RegistroDtos.RegistroEntidad request) {
        return post("/publico/registro/entidades-beneficiarias", request);
    }

    private RegistroDtos.Registrado post(String ruta, Object request) {
        RegistroDtos.Registrado respuesta = client.post().uri(url + ruta)
                .contentType(MediaType.APPLICATION_JSON).body(request)
                .retrieve().body(RegistroDtos.Registrado.class);
        // Una respuesta vacía se trata como servicio no disponible (no se confirmó la cuenta).
        if (respuesta == null) throw new RestClientException("El registro no devolvió confirmación.");
        return respuesta;
    }
}

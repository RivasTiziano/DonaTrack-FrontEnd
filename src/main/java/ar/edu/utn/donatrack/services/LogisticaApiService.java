package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.ApiRequests;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogisticaApiService {
    private final WebApiCallerService http;
    private final String url;

    public LogisticaApiService(WebApiCallerService http, @Value("${apis.logistica.url}") String url) {
        this.http = http;
        this.url = url.replaceAll("/+$", "");
    }


    // Camiones.

    public List<ApiDtos.Camion> camiones() {
        return http.getList(url + "/camiones", ApiDtos.Camion.class);
    }

    public void crear(ApiRequests.Camion request) {
        http.post(url + "/camiones", request, Object.class);
    }

    public void estado(Long id, String estado) {
        http.update(HttpMethod.PATCH, url + "/camiones/" + id + "/estado", new ApiRequests.EstadoCamion(estado));
    }

    public void eliminar(Long id) {
        http.delete(url + "/camiones/" + id);
    }

    // Entregas y rutas.

    public List<ApiDtos.Entrega> entregas() {
        return http.getList(url + "/entregas", ApiDtos.Entrega.class);
    }

    public List<ApiDtos.Ruta> rutas() {
        return http.getList(url + "/rutas", ApiDtos.Ruta.class);
    }

    public void iniciarRuta(Long id) {
        http.post(url + "/rutas/" + id + "/iniciar", java.util.Map.of(), Object.class);
    }

    public void finalizarRuta(Long id) {
        http.post(url + "/rutas/" + id + "/finalizar", java.util.Map.of(), Object.class);
    }

    public void cancelarRuta(Long id) {
        http.post(url + "/rutas/" + id + "/cancelar", java.util.Map.of(), Object.class);
    }
}

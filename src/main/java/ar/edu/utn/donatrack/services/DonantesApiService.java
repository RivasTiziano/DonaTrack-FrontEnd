package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.DonanteRequests;
import ar.edu.utn.donatrack.dto.donante.*;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DonantesApiService {
    
    private final WebApiCallerService http;
    private final String url;
    public DonantesApiService(WebApiCallerService http, @Value("${apis.donaciones.url}") String url) {
        this.http = http; this.url = url.replaceAll("/+$", "");
    }
    public void crearHumano(DonanteRequests.Humano request) {
        http.post(url + "/donantes/humanos", request, DonanteHumanoResponse.class);
    }
    public void crearJuridico(DonanteRequests.Juridico request) {
        http.post(url + "/donantes/juridicos", request, DonanteJuridicoResponse.class);
    }
    public void eliminarHumano(Long id) { http.delete(url + "/donantes/humanos/" + id); }
    public void eliminarJuridico(Long id) { http.delete(url + "/donantes/juridicos/" + id); }
}

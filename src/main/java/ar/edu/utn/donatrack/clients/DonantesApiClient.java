package ar.edu.utn.donatrack.clients;

import ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse;
import ar.edu.utn.donatrack.dto.donante.DonanteJuridicoResponse;
import org.springframework.beans.factory.annotation.Value;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DonantesApiClient {

    private final WebApiCallerService http;
    private final String baseUrl;

    public DonantesApiClient(
            WebApiCallerService http,
            @Value("${apis.donaciones.url}") String baseUrl
    ) {
        this.http = http;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public List<DonanteHumanoResponse> listarHumanos() {
        return http.getList(baseUrl + "/donantes/humanos", DonanteHumanoResponse.class);
    }

    public List<DonanteJuridicoResponse> listarJuridicos() {
        return http.getList(baseUrl + "/donantes/juridicos", DonanteJuridicoResponse.class);
    }
}

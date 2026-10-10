package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncentivosApiService {
    private final WebApiCallerService http;
    private final String url;

    public IncentivosApiService(WebApiCallerService http, @Value("${apis.incentivos.url}") String url) {
        this.http = http;
        this.url = url.replaceAll("/+$", "");
    }

    public List<ApiDtos.Ranking> ranking() {
        return http.getList(url + "/incentivos/rankingMensual", ApiDtos.Ranking.class);
    }
}

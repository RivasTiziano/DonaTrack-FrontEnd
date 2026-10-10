package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import org.springframework.http.HttpMethod;
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

    // Consulta administrativa por donante; no implica identidad autenticada.

    public IncentivosDtos.Metricas metricas(Long id) {
        return http.get(url + "/incentivos/donantes/" + id + "/metricas", IncentivosDtos.Metricas.class);
    }

    public List<IncentivosDtos.MisionCompletada> misionesCompletadas(Long id) {
        return http.getList(url + "/incentivos/donantes/" + id + "/misiones", IncentivosDtos.MisionCompletada.class);
    }

    public List<IncentivosDtos.Insignia> insignias(Long id) {
        return http.getList(url + "/incentivos/donantes/" + id + "/insignias", IncentivosDtos.Insignia.class);
    }

    public List<IncentivosDtos.Auditoria> historialCategorias(Long id) {
        return http.getList(url + "/incentivos/donantes/" + id + "/historial-categorias", IncentivosDtos.Auditoria.class);
    }

    public IncentivosDtos.Progreso progreso(Long id) {
        return http.get(url + "/incentivos/donantes/" + id + "/progreso-mision-actual", IncentivosDtos.Progreso.class);
    }

    public void visibilidad(Long id, boolean visible) {
        http.update(HttpMethod.PATCH, url + "/incentivos/donantes/" + id + "/insignias/visibilidad", new IncentivosDtos.Visibilidad(visible));
    }

    public List<IncentivosDtos.RankingHistorico> historialRankings() {
        return http.getList(url + "/incentivos/rankings/historial", IncentivosDtos.RankingHistorico.class);
    }

    public List<IncentivosDtos.Categoria> categorias() {
        return http.getList(url + "/incentivos/categorias", IncentivosDtos.Categoria.class);
    }

    public IncentivosDtos.Categoria categoria(Long id) {
        return http.get(url + "/incentivos/categorias/" + id, IncentivosDtos.Categoria.class);
    }

    public List<IncentivosDtos.Mision> misiones(Long categoriaId) {
        return http.getList(url + "/incentivos/categorias/" + categoriaId + "/misiones", IncentivosDtos.Mision.class);
    }

    public void crearCategoria(IncentivosDtos.CategoriaRequest request) {
        http.post(url + "/incentivos/categorias", request, IncentivosDtos.Categoria.class);
    }

    public void agregarMision(Long id, IncentivosDtos.MisionRequest request) {
        http.post(url + "/incentivos/categorias/" + id + "/misiones", request, IncentivosDtos.Mision.class);
    }

    public void eliminarMision(Long categoriaId, Long misionId) {
        http.delete(url + "/incentivos/categorias/" + categoriaId + "/misiones/" + misionId);
    }
}

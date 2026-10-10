package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.mappers.IncentivosMapper;
import ar.edu.utn.donatrack.validators.IncentivosValidator;
import ar.edu.utn.donatrack.models.WebDatos;
import ar.edu.utn.donatrack.mappers.*;
import static ar.edu.utn.donatrack.services.internal.ConsultaOpcional.lista;
import java.util.Map;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class IncentivosService {
    private final DonacionesService donaciones;
    private final DonanteDashboardMapper donanteMapper;
    private final IncentivosApiService api;
    private final IncentivosMapper mapper;
    private final IncentivosValidator validator;
    public IncentivosService(IncentivosApiService api,IncentivosMapper mapper,IncentivosValidator validator, DonacionesService donaciones, DonanteDashboardMapper donanteMapper) {
        this.api=api; this.mapper=mapper; this.validator=validator; this.donaciones=donaciones; this.donanteMapper=donanteMapper;
    }
    public void crearCategoria(String nombre,Long siguiente,String tipo,Integer objetivo) {
        validator.categoria(nombre,tipo,objetivo); api.crearCategoria(mapper.categoria(nombre,siguiente,tipo,objetivo));
    }
    public void agregarMision(Long id,String tipo,Integer objetivo) {
        validator.mision(tipo,objetivo); api.agregarMision(id,mapper.mision(tipo,objetivo));
    }
    public List<ApiDtos.Ranking> ranking() {
        return api.ranking();
    }

    public IncentivosDtos.Metricas metricas(Long id) {
        return api.metricas(id);
    }

    public List<IncentivosDtos.MisionCompletada> misionesCompletadas(Long id) {
        return api.misionesCompletadas(id);
    }

    public List<IncentivosDtos.Insignia> insignias(Long id) {
        return api.insignias(id);
    }

    public List<IncentivosDtos.Auditoria> historialCategorias(Long id) {
        return api.historialCategorias(id);
    }

    public IncentivosDtos.Progreso progreso(Long id) {
        return api.progreso(id);
    }

    public void visibilidad(Long id, boolean visible) {
        api.visibilidad(id, visible);
    }

    public List<IncentivosDtos.RankingHistorico> historialRankings() {
        return api.historialRankings();
    }

    public List<IncentivosDtos.Categoria> categorias() {
        return api.categorias();
    }

    public IncentivosDtos.Categoria categoria(Long id) {
        return api.categoria(id);
    }

    public List<IncentivosDtos.Mision> misiones(Long categoriaId) {
        return api.misiones(categoriaId);
    }

    public void eliminarMision(Long categoriaId, Long misionId) {
        api.eliminarMision(categoriaId, misionId);
    }

    public Map<String,Object> incentivosDonante() {
        var d=donaciones.datosDemostracionDonante();
        return donanteMapper.mapear(new WebDatos(lista(donaciones::donaciones),null,d.humanos(),d.juridicos(),List.of(),List.of(),lista(api::ranking),List.of()));
    }
}

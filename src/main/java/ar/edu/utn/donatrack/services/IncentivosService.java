package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.mappers.IncentivosMapper;
import ar.edu.utn.donatrack.validators.IncentivosValidator;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
@Service
public class IncentivosService {
    private final IncentivosApiService api;
    private final IncentivosMapper mapper;
    private final IncentivosValidator validator;
    public IncentivosService(IncentivosApiService api,IncentivosMapper mapper,IncentivosValidator validator) {
        this.api=api; this.mapper=mapper; this.validator=validator;
    }
    public void crearCategoria(String nombre,Long siguiente,String tipo,Integer objetivo) {
        validator.categoria(nombre,tipo,objetivo); api.crearCategoria(mapper.categoria(nombre,siguiente,tipo,objetivo));
    }
    public void agregarMision(Long id,String tipo,Integer objetivo) {
        validator.mision(tipo,objetivo); api.agregarMision(id,mapper.mision(tipo,objetivo));
    }
    public List<IncentivosDtos.FilaRanking> rankingConNombres(Map<Long,String> nombres) {
        return mapper.ranking(api.ranking(), nombres);
    }

    public List<IncentivosDtos.FilaHistorial> historialConNombres(Map<Long,String> nombres) {
        return mapper.historial(api.historialRankings(), nombres);
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
}

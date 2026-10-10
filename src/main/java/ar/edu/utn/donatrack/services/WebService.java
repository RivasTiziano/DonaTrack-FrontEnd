package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.mappers.WebMapper;
import ar.edu.utn.donatrack.mappers.DonanteDashboardMapper;
import ar.edu.utn.donatrack.mappers.BeneficiarioDashboardMapper;
import ar.edu.utn.donatrack.models.WebDatos;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.*;
import java.util.function.Supplier;
/** Coordina las consultas de páginas públicas y dashboards; no recibe objetos MVC. */
@Service
public class WebService {
    private final DonacionesService donaciones;
    private final LogisticaService logistica;
    private final IncentivosService incentivos;
    private final WebMapper mapper;
    private final DonanteDashboardMapper donanteMapper;
    private final BeneficiarioDashboardMapper beneficiarioMapper;
    public WebService(DonacionesService donaciones,LogisticaService logistica,IncentivosService incentivos,WebMapper mapper,
                      DonanteDashboardMapper donanteMapper,BeneficiarioDashboardMapper beneficiarioMapper) {
        this.donaciones=donaciones; this.logistica=logistica; this.incentivos=incentivos; this.mapper=mapper;
        this.donanteMapper=donanteMapper; this.beneficiarioMapper=beneficiarioMapper;
    }
    private WebDatos empty() { return new WebDatos(List.of(),null,List.of(),List.of(),List.of(),List.of(),List.of(),List.of()); }
    private WebDatos publicData() { return new WebDatos(list(donaciones::donaciones),null,List.of(),List.of(),List.of(),List.of(),List.of(),List.of()); }
    public Map<String,Object> landing() { return mapper.landing(publicData()); }
    public Map<String,Object> legal() { return mapper.legal(empty()); }
    public Map<String,Object> donorDashboard() {
        return donanteMapper.mapear(new WebDatos(list(donaciones::donaciones),null,list(donaciones::listarHumanos),
                list(donaciones::listarJuridicos),list(donaciones::beneficiarios),List.of(),list(incentivos::ranking),list(logistica::entregas)));
    }
    public Map<String,Object> beneficiaryDashboard() {
        return beneficiarioMapper.mapear(new WebDatos(list(donaciones::donaciones),null,List.of(),List.of(),
                list(donaciones::beneficiarios),list(donaciones::necesidades),List.of(),list(logistica::entregas)));
    }
    private <T> List<T> list(Supplier<List<T>> query) {
        try { var values=query.get(); return values==null?List.of():values; }
        catch(RestClientException | IllegalStateException e) { return List.of(); }
    }
}

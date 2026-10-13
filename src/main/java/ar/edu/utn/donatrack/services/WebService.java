package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.mappers.WebMapper;
import ar.edu.utn.donatrack.models.WebDatos;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.*;
import java.util.function.Supplier;
/** Coordina las consultas de páginas públicas; no recibe objetos MVC. Los paneles personales usan PanelDonanteService y PanelEntidadService. */
@Service
public class WebService {
    private final DonacionesService donaciones;
    private final WebMapper mapper;
    public WebService(DonacionesService donaciones,WebMapper mapper) {
        this.donaciones=donaciones; this.mapper=mapper;
    }
    private WebDatos empty() { return new WebDatos(List.of(),null,List.of(),List.of(),List.of(),List.of(),List.of(),List.of()); }
    private WebDatos publicData() { return new WebDatos(list(donaciones::donaciones),null,List.of(),List.of(),List.of(),List.of(),List.of(),List.of()); }
    public Map<String,Object> landing() { return mapper.landing(publicData()); }
    public Map<String,Object> legal() { return mapper.legal(empty()); }
    private <T> List<T> list(Supplier<List<T>> query) {
        try { var values=query.get(); return values==null?List.of():values; }
        catch(RestClientException | IllegalStateException e) { return List.of(); }
    }
}

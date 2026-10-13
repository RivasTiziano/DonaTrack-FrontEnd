package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.mappers.WebMapper;
import org.springframework.stereotype.Service;
import java.util.Map;

/** Coordina las páginas públicas. No consulta la API: los datos públicos todavía no existen (pedido D1). Los paneles personales usan PanelDonanteService y PanelEntidadService. */
@Service
public class WebService {
    private final PublicoService publico;
    private final WebMapper mapper;
    public WebService(PublicoService publico, WebMapper mapper) {
        this.publico = publico; this.mapper = mapper;
    }
    public Map<String,Object> landing() { return mapper.landing(publico.donacionesDestacadas()); }
    public Map<String,Object> legal() { return mapper.legal(); }
    public Map<String,Object> mapa() { return mapper.map(publico.entregasEnMapa()); }
    public Map<String,Object> detalle(String id) { return mapper.donationDetail(publico.detallePublico(id)); }
}

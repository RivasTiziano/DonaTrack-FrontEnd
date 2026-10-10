package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.mappers.LogisticaMapper;
import ar.edu.utn.donatrack.validators.LogisticaValidator;
import ar.edu.utn.donatrack.models.WebDatos;
import ar.edu.utn.donatrack.mappers.*;
import static ar.edu.utn.donatrack.services.internal.ConsultaOpcional.lista;
import java.util.Map;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
@Service
public class LogisticaService {
    private final DonacionesService donaciones;
    private final DonanteDashboardMapper donanteMapper;
    private final BeneficiarioDashboardMapper beneficiarioMapper;
    private final LogisticaApiService api;
    private final LogisticaMapper mapper;
    private final LogisticaValidator validator;
    public LogisticaService(LogisticaApiService api,LogisticaMapper mapper,LogisticaValidator validator, DonacionesService donaciones, DonanteDashboardMapper donanteMapper, BeneficiarioDashboardMapper beneficiarioMapper) {
        this.api=api; this.mapper=mapper; this.validator=validator; this.donaciones=donaciones; this.donanteMapper=donanteMapper; this.beneficiarioMapper=beneficiarioMapper;
    }
    public void crearCamion(String patente,Float volumen,Float altura,Float carga) {
        validator.camion(patente,volumen,altura,carga); api.crear(mapper.camion(patente,volumen,altura,carga));
    }
    public void iniciarRuta(Long id,String chofer) { validator.chofer(chofer); api.iniciarRuta(id,chofer); }
    public LogisticaDetalles.Planificacion planificar(LocalDate fecha) {
        validator.fecha(fecha);
        var result=api.planificar(fecha);
        if(result==null) throw new IllegalStateException("Planificación sin respuesta");
        return result;
    }
    public List<ApiDtos.Camion> camiones() {
        return api.camiones();
    }

    public ApiDtos.Camion camion(Long id) {
        return api.camion(id);
    }

    public List<LogisticaDetalles.Seguimiento> ubicaciones() {
        return api.ubicaciones();
    }

    public LogisticaDetalles.Ubicacion ubicacion(String patente) {
        return api.ubicacion(patente);
    }

    public void estado(Long id, String estado) {
        api.estado(id, estado);
    }

    public void eliminar(Long id) {
        api.eliminar(id);
    }

    public List<ApiDtos.Entrega> entregas() {
        return api.entregas();
    }

    public ApiDtos.Entrega entrega(Long id) {
        return api.entrega(id);
    }

    public LogisticaDetalles.Ruta ruta(Long id) {
        return api.ruta(id);
    }

    public List<LogisticaDetalles.Parada> paradas() {
        return api.paradas();
    }

    public LogisticaDetalles.Parada parada(Long id) {
        return api.parada(id);
    }

    public List<ApiDtos.Ruta> rutas() {
        return api.rutas();
    }

    public void finalizarRuta(Long id) {
        api.finalizarRuta(id);
    }

    public void cancelarRuta(Long id) {
        api.cancelarRuta(id);
    }

    public Map<String,Object> entregasDonante() {
        var d=donaciones.datosDemostracionDonante();
        return donanteMapper.mapear(new WebDatos(List.of(),null,d.humanos(),d.juridicos(),List.of(),List.of(),List.of(),lista(api::entregas)));
    }
    public Map<String,Object> entregasBeneficiario() {
        var d=donaciones.datosDemostracionBeneficiario();
        return beneficiarioMapper.mapear(new WebDatos(List.of(),null,List.of(),List.of(),d.beneficiarios(),List.of(),List.of(),lista(api::entregas)));
    }
    /** Conserva la pantalla existente; no confirma una recepción real. */
    public Map<String,Object> confirmarBeneficiario() {
        var d=donaciones.datosDemostracionBeneficiario();
        return beneficiarioMapper.mapear(new WebDatos(lista(donaciones::donaciones),null,List.of(),List.of(),d.beneficiarios(),List.of(),List.of(),List.of()));
    }
}

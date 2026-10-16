package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.mappers.LogisticaMapper;
import ar.edu.utn.donatrack.validators.LogisticaValidator;
import ar.edu.utn.donatrack.validators.RecepcionValidator;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import ar.edu.utn.donatrack.models.EntregaFila;
import ar.edu.utn.donatrack.models.RutaFila;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class LogisticaService {
    private final LogisticaApiService api;
    private final LogisticaMapper mapper;
    private final LogisticaValidator validator;
    private final RecepcionValidator recepcion;
    public LogisticaService(LogisticaApiService api,LogisticaMapper mapper,LogisticaValidator validator,RecepcionValidator recepcion) {
        this.api=api; this.mapper=mapper; this.validator=validator; this.recepcion=recepcion;
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

    /**
     * Entregas para la tabla del admin: la entidad destino se resuelve con una sola llamada
     * (nombres por id, ej. GET /beneficiarios) y sin N+1. Si esa llamada falla, el destino queda "—".
     */
    public List<EntregaFila> entregasTabla(java.util.function.Supplier<Map<Long,String>> nombresEntidades) {
        List<ApiDtos.Entrega> entregas=api.entregas();
        if(entregas==null||entregas.isEmpty()) return List.of();
        Map<Long,String> nombres;
        try { nombres=nombresEntidades.get(); } catch(RuntimeException e) { nombres=null; }
        Map<Long,String> porId=nombres==null?Map.of():nombres;
        return entregas.stream().filter(java.util.Objects::nonNull)
                .map(e->EntregaFila.de(e,e.entidadBeneficiariaId()==null?null:porId.get(e.entidadBeneficiariaId()))).toList();
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

    /** Rutas para la tabla del admin: patente resuelta con una sola llamada a /camiones (sin N+1). */
    public List<RutaFila> rutasTabla() {
        List<ApiDtos.Ruta> rutas=api.rutas();
        Map<Long,String> patentes=new HashMap<>();
        if(rutas!=null&&!rutas.isEmpty()) {
            List<ApiDtos.Camion> camiones;
            try { camiones=api.camiones(); } catch(RuntimeException e) { camiones=List.of(); }
            if(camiones!=null) for(var c:camiones) if(c!=null&&c.id()!=null) patentes.put(c.id(),c.patente());
        }
        return rutas==null?List.of():rutas.stream().map(r->RutaFila.de(r,patentes.get(r.camionId()))).toList();
    }

    public void finalizarRuta(Long id) {
        api.finalizarRuta(id);
    }

    public void cancelarRuta(Long id) {
        api.cancelarRuta(id);
    }

    /**
     * Confirma la recepción de una entrega en tránsito. RECIBIDA exige fotos (URL); NO_RECIBIDA exige justificación.
     * Las fotos solo viajan con RECIBIDA. El entregaId lo resuelve PanelEntidadService a partir de la asignación.
     */
    public void confirmarRecepcion(Long entregaId,String resultado,List<String> fotos,String justificacion) {
        if (entregaId == null) throw new FormularioInvalidoException("Esta entrega ya no está pendiente de confirmación.");
        recepcion.validarContenido(resultado,fotos,justificacion);
        boolean recibida=RecepcionValidator.RECIBIDA.equals(resultado);
        String motivo=justificacion==null||justificacion.isBlank()?null:justificacion.trim();
        api.actualizarEstadoEntrega(entregaId,new LogisticaDetalles.ActualizarEntrega(
                recibida?"ENTREGADA":"NO_RECIBIDA", motivo, recibida?fotos:null));
    }
}

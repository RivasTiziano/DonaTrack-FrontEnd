package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.forms.BienForm;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.exceptions.SesionRequeridaException;
import ar.edu.utn.donatrack.mappers.PanelEntidadMapper;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import ar.edu.utn.donatrack.validators.RecepcionValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Datos del panel de la entidad beneficiaria. El dueño siempre es el referenciaId de la sesión:
 * ninguna operación recibe un id de entidad desde la URL o un formulario.
 */
@Service
public class PanelEntidadService {
    private static final Set<String> EN_CURSO = Set.of("LISTA_PARA_ENTREGAR", "EN_TRASLADO");

    private final SesionActual sesiones;
    private final DonacionesApiService donacionesApi;
    private final DonacionesService donaciones;
    private final LogisticaApiService logisticaApi;
    private final LogisticaService logistica;
    private final RecepcionValidator recepcion;
    private final PanelEntidadMapper mapper;
    private final ApiErrorMessages errores;

    public PanelEntidadService(SesionActual sesiones, DonacionesApiService donacionesApi, DonacionesService donaciones,
                               LogisticaApiService logisticaApi, LogisticaService logistica, RecepcionValidator recepcion,
                               PanelEntidadMapper mapper, ApiErrorMessages errores) {
        this.sesiones = sesiones;
        this.donacionesApi = donacionesApi;
        this.donaciones = donaciones;
        this.logisticaApi = logisticaApi;
        this.logistica = logistica;
        this.recepcion = recepcion;
        this.mapper = mapper;
        this.errores = errores;
    }

    /** Resumen: perfil, necesidades, donaciones asignadas y llegadas en curso. */
    public Map<String, Object> resumen() {
        Map<String, Object> m = new LinkedHashMap<>();
        Long ref = ref();
        m.put("user", perfil(m, ref));
        m.put("needs", necesidades(m, ref));
        var asignaciones = asignaciones(m, ref);
        m.put("assignedDonations", asignaciones.stream().map(mapper::asignacion).toList());
        m.put("incomingDeliveries", enCurso(asignaciones).stream().map(mapper::entrega).toList());
        return m;
    }

    /** Necesidades propias y las categorías para el alta. */
    public Map<String, Object> necesidades() {
        Map<String, Object> m = new LinkedHashMap<>();
        Long ref = ref();
        m.put("user", perfil(m, ref));
        m.put("needs", necesidades(m, ref));
        m.put("categorias", cargar(m, donacionesApi::categorias, List.<ApiDtos.Categoria>of()));
        return m;
    }

    /** Alta de necesidad. La entidad es siempre la de la sesión. */
    public void crearNecesidad(String descripcion, String tipo, String periodo, BienForm form) {
        donaciones.crearNecesidad(ref(), descripcion, tipo, periodo, form);
    }

    /** Baja de necesidad, solo si pertenece a la entidad de la sesión. */
    public void eliminarNecesidad(Long necesidadId) {
        Long ref = ref();
        var necesidad = donacionesApi.necesidad(necesidadId);
        if (necesidad == null || !Objects.equals(necesidad.entidadBeneficiariaId(), ref))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La necesidad no pertenece a tu entidad.");
        // El chequeo de dueño de arriba es una defensa adicional: el back también valida al dueño (GESTIONAR_NECESIDADES).
        donacionesApi.eliminarNecesidad(necesidadId);
    }

    /** Todas las donaciones asignadas a la entidad. */
    public Map<String, Object> donaciones() {
        Map<String, Object> m = new LinkedHashMap<>();
        Long ref = ref();
        m.put("user", perfil(m, ref));
        m.put("assignedDonations", asignaciones(m, ref).stream().map(mapper::asignacion).toList());
        return m;
    }

    /**
     * Asignaciones en traslado, a confirmar por la entidad. Cada fila lleva el entregaId de su entrega en traslado;
     * si no hay una, la fila no muestra acciones.
     */
    public Map<String, Object> confirmar() {
        Map<String, Object> m = new LinkedHashMap<>();
        Long ref = ref();
        m.put("user", perfil(m, ref));
        var enTraslado = cargar(m, () -> donacionesApi.asignacionesEntidad(ref, "EN_TRASLADO"), List.<ApiDtos.Asignacion>of());
        Map<Long, Long> entregas = enTraslado.isEmpty() ? new HashMap<>() : entregasEnTrasladoPorAsignacion(m, ref);
        m.put("assignedDonations", enTraslado.stream().map(a -> {
            Map<String, Object> fila = new LinkedHashMap<>(mapper.asignacion(a));
            fila.put("entregaId", entregas.get(a.id()));
            return fila;
        }).toList());
        return m;
    }

    /**
     * Confirma la recepción de una asignación propia en traslado. El entregaId se resuelve acá, en el servidor,
     * a partir de la asignación y de la entidad de la sesión. Si no hay una entrega en traslado propia, no se llama al PATCH.
     */
    public void confirmarRecepcion(Long asignacionId, String resultado, List<String> fotos, String justificacion) {
        recepcion.validar(asignacionId, resultado, fotos, justificacion);
        Long ref = ref();
        Long entregaId = logisticaApi.entregas(ref, null, asignacionId, "EN_TRASLADO").stream()
                .filter(e -> Objects.equals(e.asignacionId(), asignacionId)
                        && Objects.equals(e.entidadBeneficiariaId(), ref)
                        && "EN_TRASLADO".equals(e.estado()))
                .map(ApiDtos.Entrega::id)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new FormularioInvalidoException("Esta entrega ya no está pendiente de confirmación."));
        try {
            logistica.confirmarRecepcion(entregaId, resultado, fotos, justificacion);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 403)
                throw new FormularioInvalidoException("No podés confirmar esta entrega.");
            if (e.getStatusCode().value() == 400 && e.getResponseBodyAsString().contains("Transición"))
                throw new FormularioInvalidoException("Esta entrega ya no está en traslado; actualizá la página.");
            throw e;
        }
    }

    /** Asignaciones en camino o listas para entregar. */
    public Map<String, Object> entregas() {
        Map<String, Object> m = new LinkedHashMap<>();
        Long ref = ref();
        m.put("user", perfil(m, ref));
        m.put("incomingDeliveries", enCurso(asignaciones(m, ref)).stream().map(mapper::entrega).toList());
        return m;
    }

    // ---------- Auxiliares ----------

    private SesionUsuario sesion() {
        return sesiones.obtener().orElseThrow(SesionRequeridaException::new);
    }

    private Long ref() {
        return sesion().getReferenciaId();
    }

    private Map<String, Object> perfil(Map<String, Object> m, Long ref) {
        var b = cargar(m, () -> donacionesApi.beneficiario(ref), (DonacionesDetalles.Beneficiario) null);
        return b == null ? null : mapper.perfil(b);
    }

    /** Necesidades de la entidad: el back filtra por entidadBeneficiariaId de la sesión. */
    private List<Map<String, Object>> necesidades(Map<String, Object> m, Long ref) {
        return cargar(m, () -> donacionesApi.necesidades(ref, null), List.<ApiDtos.Necesidad>of()).stream()
                .map(mapper::necesidad).toList();
    }

    /** Asignaciones de la entidad: el backend filtra por entidadBeneficiariaId (obligatorio) y no por estado. */
    private List<ApiDtos.Asignacion> asignaciones(Map<String, Object> m, Long ref) {
        return cargar(m, () -> donacionesApi.asignacionesEntidad(ref, ""), List.<ApiDtos.Asignacion>of());
    }

    /** Una sola consulta a /entregas en traslado de la entidad, indexada por asignacionId. */
    private Map<Long, Long> entregasEnTrasladoPorAsignacion(Map<String, Object> m, Long ref) {
        var entregas = cargar(m, () -> logisticaApi.entregas(ref, null, null, "EN_TRASLADO"), List.<ApiDtos.Entrega>of());
        Map<Long, Long> porAsignacion = new HashMap<>();
        for (var e : entregas)
            if (e.asignacionId() != null && e.id() != null && "EN_TRASLADO".equals(e.estado())
                    && Objects.equals(e.entidadBeneficiariaId(), ref))
                porAsignacion.putIfAbsent(e.asignacionId(), e.id());
        return porAsignacion;
    }

    private static List<ApiDtos.Asignacion> enCurso(List<ApiDtos.Asignacion> asignaciones) {
        return asignaciones.stream().filter(a -> EN_CURSO.contains(a.estado())).toList();
    }

    /** Ejecuta una consulta; si falla, deja el mensaje en el modelo y devuelve el valor vacío. */
    private <T> T cargar(Map<String, Object> m, Supplier<T> consulta, T vacio) {
        try {
            T valor = consulta.get();
            return valor == null ? vacio : valor;
        } catch (RestClientException | IllegalStateException e) {
            m.put("apiError", errores.describe(e));
            return vacio;
        }
    }
}

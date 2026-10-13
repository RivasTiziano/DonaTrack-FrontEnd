package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.exceptions.SesionRequeridaException;
import ar.edu.utn.donatrack.mappers.PanelDonanteMapper;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Datos del panel del donante. El dueño siempre es el referenciaId de la sesión:
 * ninguna operación recibe un id de donante desde la URL o un formulario.
 */
@Service
public class PanelDonanteService {
    private static final Set<String> EN_CURSO = Set.of("LISTA_PARA_ENTREGAR", "EN_TRASLADO");

    private final SesionActual sesiones;
    private final DonacionesApiService donacionesApi;
    private final IncentivosApiService incentivosApi;
    private final PanelDonanteMapper mapper;
    private final ApiErrorMessages errores;

    public PanelDonanteService(SesionActual sesiones, DonacionesApiService donacionesApi, IncentivosApiService incentivosApi,
                               PanelDonanteMapper mapper, ApiErrorMessages errores) {
        this.sesiones = sesiones;
        this.donacionesApi = donacionesApi;
        this.incentivosApi = incentivosApi;
        this.mapper = mapper;
        this.errores = errores;
    }

    /** Datos comunes a todas las vistas del donante: el perfil (con categoría) y las métricas. */
    private record Contexto(Long ref, IncentivosDtos.Metricas metricas, Map<String, Object> user) {
        String categoriaActual() { return metricas == null ? null : metricas.categoriaActual(); }
    }

    /** Resumen: perfil, últimas cinco donaciones e insignias visibles. */
    public Map<String, Object> resumen() {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        m.put("user", c.user());
        m.put("donorMetrics", mapper.metricas(c.metricas()));
        var recientes = donacionesPropias(m, c.ref()).stream()
                .sorted(Comparator.comparing(ApiDtos.Donacion::fechaRecepcion, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5).toList();
        m.put("userDonations", donaciones(m, recientes, beneficiariosPorId(m)));
        m.put("badges", mapper.insignias(cargarIncentivos(m, () -> incentivosApi.insignias(c.ref()), List.<IncentivosDtos.Insignia>of())));
        return m;
    }

    /** Historial de donaciones propias. Hoy vacío por el permiso del backend (ver donacionesPropias); los filtros salen del catálogo. */
    public Map<String, Object> donaciones() {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        m.put("user", c.user());
        List<Map<String, Object>> lista = donaciones(m, donacionesPropias(m, c.ref()), beneficiariosPorId(m));
        m.put("userDonations", lista);
        m.putAll(mapper.filtrosCatalogo(cargar(m, donacionesApi::categorias, List.<ApiDtos.Categoria>of())));
        return m;
    }

    /** Directorio de entidades beneficiarias, con sus necesidades activas. */
    public Map<String, Object> entidades() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("user", contexto(m).user());
        var beneficiarios = cargar(m, donacionesApi::beneficiarios, List.<ApiDtos.Beneficiario>of());
        var necesidades = cargar(m, donacionesApi::necesidades, List.<ApiDtos.Necesidad>of());
        List<Map<String, Object>> entidades = new ArrayList<>();
        for (var b : beneficiarios) {
            // PROVISORIO: reemplazar por GET /necesidades?entidadBeneficiariaId (pedido backend C2)
            List<ApiDtos.Necesidad> deEntidad = necesidades.stream()
                    .filter(n -> Objects.equals(n.entidadBeneficiariaId(), b.id())).toList();
            entidades.add(mapper.entidad(b, deEntidad));
        }
        m.put("beneficiaryEntities", entidades);
        m.putAll(mapper.filtrosEntidades(entidades));
        return m;
    }

    /** Incentivos: métricas, escalafón, misiones, insignias e historial de categorías. */
    public Map<String, Object> incentivos() {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        Long ref = c.ref();
        m.put("user", c.user());
        m.put("donorMetrics", mapper.metricas(c.metricas()));

        var progreso = cargarIncentivos(m, () -> incentivosApi.progreso(ref), (IncentivosDtos.Progreso) null);
        var completadas = cargarIncentivos(m, () -> incentivosApi.misionesCompletadas(ref), List.<IncentivosDtos.MisionCompletada>of());
        var insignias = cargarIncentivos(m, () -> incentivosApi.insignias(ref), List.<IncentivosDtos.Insignia>of());
        var historial = cargarIncentivos(m, () -> incentivosApi.historialCategorias(ref), List.<IncentivosDtos.Auditoria>of());
        var categorias = cargar(m, incentivosApi::categorias, List.<IncentivosDtos.Categoria>of());

        m.put("incentiveCategories", mapper.categorias(categorias, completadas, c.categoriaActual()));
        m.put("missions", mapper.misiones(progreso, c.categoriaActual(), categorias, completadas));
        m.put("badges", mapper.insignias(insignias));
        m.put("categoryAudits", mapper.auditorias(historial));
        return m;
    }

    /** Cambia la visibilidad de las insignias del propio donante. */
    public void visibilidad(boolean visible) {
        incentivosApi.visibilidad(sesion().getReferenciaId(), visible);
    }

    /** Asignaciones propias que todavía no terminaron de entregarse. */
    public Map<String, Object> entregas() {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        m.put("user", c.user());
        var entidades = beneficiariosPorId(m);
        List<Map<String, Object>> entregas = new ArrayList<>();
        for (var d : donacionesPropias(m, c.ref())) {
            var asignaciones = cargar(m, () -> donacionesApi.asignaciones(d.id()), List.<ApiDtos.Asignacion>of());
            for (var a : asignaciones) {
                if (!EN_CURSO.contains(a.estado())) continue;
                entregas.add(mapper.entrega(a, entidades.get(a.entidadBeneficiariaId())));
            }
        }
        m.put("activeDonorDeliveries", entregas);
        return m;
    }

    // ---------- Auxiliares ----------

    private SesionUsuario sesion() {
        return sesiones.obtener().orElseThrow(SesionRequeridaException::new);
    }

    private Contexto contexto(Map<String, Object> m) {
        SesionUsuario sesion = sesion();
        Long ref = sesion.getReferenciaId();
        String tipo = sesion.tipoDonante();
        var metricas = cargarIncentivos(m, () -> incentivosApi.metricas(ref), (IncentivosDtos.Metricas) null);
        String categoria = metricas == null ? null : metricas.categoriaActual();
        Map<String, Object> user = null;
        if ("humanos".equals(tipo)) {
            var h = cargar(m, () -> donacionesApi.humano(ref), (DonacionesDetalles.Humano) null);
            user = h == null ? null : mapper.perfilHumano(h, categoria);
        } else if ("juridicos".equals(tipo)) {
            var j = cargar(m, () -> donacionesApi.juridico(ref), (DonacionesDetalles.Juridico) null);
            user = j == null ? null : mapper.perfilJuridico(j, categoria);
        }
        return new Contexto(ref, metricas, user);
    }

    // PROVISORIO: bloqueado hasta GET /donaciones?donanteId (pedido backend C1, bloqueante).
    // GET /donaciones es solo ADMIN desde el commit 035387d: el donante recibe 403, así que no se consulta.
    private List<ApiDtos.Donacion> donacionesPropias(Map<String, Object> m, Long ref) {
        return List.of();
    }

    private Map<Long, ApiDtos.Beneficiario> beneficiariosPorId(Map<String, Object> m) {
        var beneficiarios = cargar(m, donacionesApi::beneficiarios, List.<ApiDtos.Beneficiario>of());
        Map<Long, ApiDtos.Beneficiario> porId = new HashMap<>();
        for (var b : beneficiarios) porId.put(b.id(), b);
        return porId;
    }

    /** Arma una fila por donación, con sus asignaciones (una consulta por donación). */
    private List<Map<String, Object>> donaciones(Map<String, Object> m, List<ApiDtos.Donacion> donaciones,
                                                 Map<Long, ApiDtos.Beneficiario> entidades) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var d : donaciones) {
            var asignaciones = cargar(m, () -> donacionesApi.asignaciones(d.id()), List.<ApiDtos.Asignacion>of());
            out.add(mapper.donacion(d, asignaciones, entidades));
        }
        return out;
    }

    /**
     * Datos del donante en incentivos. El servicio responde 404 hasta que el donante registra su primera
     * donación: eso es "todavía sin actividad", no un error para mostrar.
     */
    private <T> T cargarIncentivos(Map<String, Object> m, Supplier<T> consulta, T vacio) {
        try {
            T valor = consulta.get();
            return valor == null ? vacio : valor;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return vacio;
        } catch (RestClientException | IllegalStateException e) {
            m.put("apiError", errores.describe(e));
            return vacio;
        }
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

package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.dto.LogisticaDetalles;
import ar.edu.utn.donatrack.exceptions.SesionRequeridaException;
import ar.edu.utn.donatrack.mappers.IncentivosMapper;
import ar.edu.utn.donatrack.mappers.PanelDonanteMapper;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * Datos del panel del donante. El dueño siempre es el referenciaId de la sesión:
 * ninguna operación recibe un id de donante desde la URL o un formulario.
 */
@Service
public class PanelDonanteService {
    private final SesionActual sesiones;
    private final DonacionesApiService donacionesApi;
    private final IncentivosApiService incentivosApi;
    private final LogisticaApiService logisticaApi;
    private final PanelDonanteMapper mapper;
    private final ApiErrorMessages errores;
    private final IncentivosMapper incentivosMapper;

    public PanelDonanteService(SesionActual sesiones, DonacionesApiService donacionesApi, IncentivosApiService incentivosApi,
                               LogisticaApiService logisticaApi, PanelDonanteMapper mapper, ApiErrorMessages errores,
                               IncentivosMapper incentivosMapper) {
        this.sesiones = sesiones;
        this.donacionesApi = donacionesApi;
        this.incentivosApi = incentivosApi;
        this.logisticaApi = logisticaApi;
        this.mapper = mapper;
        this.errores = errores;
        this.incentivosMapper = incentivosMapper;
    }

    /** Datos comunes a todas las vistas del donante: el perfil (con categoría) y las métricas. */
    private record Contexto(Long ref, IncentivosDtos.Metricas metricas, Map<String, Object> user) {
        String categoriaActual() { return metricas == null ? null : metricas.categoriaActual(); }
    }

    /**
     * Filtros del historial, ya validados por el controller. El estado es uno de los 7 de E2 y se filtra en memoria:
     * En depósito y Vencida filtran por la donación; los otros cinco, por las donaciones con alguna asignación en ese
     * estado (se muestran solo esas asignaciones). Ver PanelDonanteMapper.coincideFiltro. Categoría y subcategoría las
     * filtra el back. Los nulos no se envían.
     */
    public record FiltroDonaciones(String estado, Long categoriaId, Long subcategoriaId) {
        public static final Set<String> ESTADOS = Set.of("EN_DEPOSITO", "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR",
                "EN_TRASLADO", "ENTREGADA", "ENTREGA_FALLIDA", "VENCIDA");
    }

    /** Resumen: perfil, últimas cinco donaciones e insignias visibles. */
    public Map<String, Object> resumen() {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        m.put("user", c.user());
        m.put("donorMetrics", mapper.metricas(c.metricas()));
        var recientes = cargar(m, () -> donacionesApi.donaciones(c.ref(), null, null, null), List.<ApiDtos.Donacion>of()).stream()
                .sorted(Comparator.comparing(ApiDtos.Donacion::fechaRecepcion, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5).toList();
        m.put("userDonations", filas(m, c.ref(), recientes));
        m.put("badges", mapper.insignias(cargarIncentivos(m, () -> incentivosApi.insignias(c.ref()), List.<IncentivosDtos.Insignia>of())));
        return m;
    }

    /**
     * Historial de donaciones propias, con filtros. Tres llamadas en total: donaciones del donante,
     * asignaciones del donante (una sola consulta) y beneficiarios (para el nombre de la entidad). Más el catálogo de filtros.
     * El estado se filtra en memoria (las asignaciones ya están cargadas: sin llamadas extra): En depósito y Vencida
     * por la donación; los otros cinco, por sus asignaciones.
     */
    public Map<String, Object> donaciones(FiltroDonaciones filtro) {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        m.put("user", c.user());
        var donaciones = cargar(m, () -> donacionesApi.donaciones(c.ref(), null, filtro.categoriaId(), filtro.subcategoriaId()),
                List.<ApiDtos.Donacion>of());
        var filas = filas(m, c.ref(), donaciones, filtro.estado());
        m.put("userDonations", filas);
        m.putAll(mapper.filtrosCatalogo(cargar(m, donacionesApi::categorias, List.<ApiDtos.Categoria>of())));
        m.put("filtroEstado", filtro.estado());
        m.put("filtroCategoriaId", filtro.categoriaId());
        m.put("filtroSubcategoriaId", filtro.subcategoriaId());
        return m;
    }

    /** Directorio de entidades beneficiarias, con sus necesidades activas. */
    public Map<String, Object> entidades() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("user", contexto(m).user());
        var beneficiarios = cargar(m, donacionesApi::beneficiarios, List.<ApiDtos.Beneficiario>of());
        // Una sola llamada para todas las entidades; evita N+1 (una consulta por entidad). Se agrupa en memoria.
        var necesidades = cargar(m, donacionesApi::necesidades, List.<ApiDtos.Necesidad>of());
        Map<Long, List<ApiDtos.Necesidad>> porEntidad = new HashMap<>();
        for (var n : necesidades)
            if (n.entidadBeneficiariaId() != null) porEntidad.computeIfAbsent(n.entidadBeneficiariaId(), k -> new ArrayList<>()).add(n);
        List<Map<String, Object>> entidades = new ArrayList<>();
        for (var b : beneficiarios)
            entidades.add(mapper.entidad(b, porEntidad.getOrDefault(b.id(), List.of())));
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
        // Escalafón en el orden de la cadena categoriaSiguienteId (Colaborador → Sostenedor → Transformador), no por id.
        var categorias = incentivosMapper.ordenarCategorias(cargar(m, incentivosApi::categorias, List.<IncentivosDtos.Categoria>of()));

        m.put("incentiveCategories", mapper.categorias(categorias, completadas, c.categoriaActual(), progreso));
        m.put("missions", mapper.misiones(progreso, c.categoriaActual(), categorias, completadas));
        m.put("badges", mapper.insignias(insignias));
        m.put("categoryAudits", mapper.auditorias(historial));
        return m;
    }

    /** Cambia la visibilidad de las insignias del propio donante. */
    public void visibilidad(boolean visible) {
        incentivosApi.visibilidad(sesion().getReferenciaId(), visible);
    }

    /**
     * Entregas en camino del donante (req. 9). El seguimiento sale del token y ya trae solo las entregas activas.
     * Con cada una se busca su asignación (título y categoría) y la entidad destino: tres consultas en total, sin N+1.
     */
    public Map<String, Object> entregas() {
        Map<String, Object> m = new LinkedHashMap<>();
        Contexto c = contexto(m);
        m.put("user", c.user());
        var seguimiento = cargar(m, logisticaApi::seguimiento, List.<LogisticaDetalles.SeguimientoEntrega>of());
        if (seguimiento.isEmpty()) {
            m.put("activeDonorDeliveries", List.of());
            return m;
        }
        Map<Long, ApiDtos.Asignacion> asignaciones = new HashMap<>();
        for (var a : cargar(m, () -> donacionesApi.asignacionesDonante(c.ref()), List.<ApiDtos.Asignacion>of()))
            if (a.id() != null) asignaciones.put(a.id(), a);
        var entidades = beneficiariosPorId(m);
        LocalDateTime ahora = LocalDateTime.now();
        List<Map<String, Object>> entregas = new ArrayList<>();
        for (var s : seguimiento) {
            var a = s.asignacionId() == null ? null : asignaciones.get(s.asignacionId());
            entregas.add(mapper.entrega(s, a, nombreEntidad(a, entidades), ahora));
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

    /** Nombre de la entidad destino de la asignación; si el directorio no la trae, un rótulo con el id. */
    private static String nombreEntidad(ApiDtos.Asignacion a, Map<Long, ApiDtos.Beneficiario> entidades) {
        if (a == null || a.entidadBeneficiariaId() == null) return null;
        var b = entidades.get(a.entidadBeneficiariaId());
        return b != null ? b.razonSocial() : "Entidad no disponible";
    }

    private Map<Long, ApiDtos.Beneficiario> beneficiariosPorId(Map<String, Object> m) {
        var beneficiarios = cargar(m, donacionesApi::beneficiarios, List.<ApiDtos.Beneficiario>of());
        Map<Long, ApiDtos.Beneficiario> porId = new HashMap<>();
        for (var b : beneficiarios) porId.put(b.id(), b);
        return porId;
    }

    /**
     * Arma una fila por donación. Las asignaciones del donante se piden una sola vez y se agrupan por donacionId
     * (sin N+1); los beneficiarios, también una sola vez.
     */
    private List<Map<String, Object>> filas(Map<String, Object> m, Long ref, List<ApiDtos.Donacion> donaciones) {
        return filas(m, ref, donaciones, null);
    }

    /** Con estadoFiltro, solo las donaciones que lo cumplen (ver PanelDonanteMapper.coincideFiltro). */
    private List<Map<String, Object>> filas(Map<String, Object> m, Long ref, List<ApiDtos.Donacion> donaciones, String estadoFiltro) {
        if (donaciones.isEmpty()) return List.of();
        var entidades = beneficiariosPorId(m);
        var asignaciones = cargar(m, () -> donacionesApi.asignacionesDonante(ref), List.<ApiDtos.Asignacion>of());
        Map<Long, List<ApiDtos.Asignacion>> porDonacion = new HashMap<>();
        for (var a : asignaciones)
            if (a.donacionId() != null) porDonacion.computeIfAbsent(a.donacionId(), k -> new ArrayList<>()).add(a);
        List<Map<String, Object>> out = new ArrayList<>();
        for (var d : donaciones) {
            var asig = porDonacion.getOrDefault(d.id(), List.of());
            if (!PanelDonanteMapper.coincideFiltro(d, asig, estadoFiltro)) continue;
            out.add(mapper.donacion(d, asig, entidades, estadoFiltro));
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

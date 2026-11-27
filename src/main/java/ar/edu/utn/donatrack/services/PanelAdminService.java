package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.mappers.PanelAdminMapper;
import ar.edu.utn.donatrack.models.PanelAdminDatos;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Consultas independientes del tablero; no modifica datos ni reemplaza reglas del backend. */
@Service
public class PanelAdminService {
    private final DonacionesService donaciones;
    private final LogisticaService logistica;
    private final IncentivosService incentivos;
    private final PanelAdminMapper mapper;
    private final ApiErrorMessages errors;

    public PanelAdminService(DonacionesService donaciones, LogisticaService logistica,
                             IncentivosService incentivos, PanelAdminMapper mapper, ApiErrorMessages errors) {
        this.donaciones = donaciones;
        this.logistica = logistica;
        this.incentivos = incentivos;
        this.mapper = mapper;
        this.errors = errors;
    }

    public Map<String,Object> cargar(SesionUsuario sesion) {
        List<String> avisos = Collections.synchronizedList(new ArrayList<>());
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            var fDonaciones = lanzar(executor, attrs, sesion, "REGISTRAR_DONACIONES", "Donaciones", donaciones::donaciones, avisos);
            var fNecesidades = lanzar(executor, attrs, sesion, "ASIGNAR_DONACIONES", "Necesidades", donaciones::necesidades, avisos);
            var fFlota = lanzar(executor, attrs, sesion, "GESTIONAR_CAMIONES", "Flota", logistica::camiones, avisos);
            var fEntregas = lanzar(executor, attrs, sesion, "SEGUIR_ENTREGAS", "Entregas", logistica::entregas, avisos);
            var fRutas = lanzar(executor, attrs, sesion, "GESTIONAR_CAMIONES", "Rutas", logistica::rutas, avisos);
            var fUbicaciones = lanzar(executor, attrs, sesion, "SEGUIR_ENTREGAS", "GPS", logistica::ubicaciones, avisos);
            var fSugerencias = lanzar(executor, attrs, sesion, "ASIGNAR_DONACIONES", "Sugerencias", donaciones::sugerencias, avisos);
            var fRanking = lanzar(executor, attrs, sesion, "VER_RANKINGS", "Ranking",
                    () -> incentivos.rankingConNombres(donaciones::nombresDonantes), avisos);

            var datos = new PanelAdminDatos(
                    fDonaciones.join(),
                    fNecesidades.join(),
                    fFlota.join(),
                    fEntregas.join(),
                    fRutas.join(),
                    fUbicaciones.join(),
                    fSugerencias.join(),
                    fRanking.join(),
                    new ArrayList<>(avisos));
            return mapper.mapear(datos, LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")));
        } finally {
            executor.shutdown();
        }
    }

    private <T> CompletableFuture<List<T>> lanzar(ExecutorService executor, RequestAttributes attrs, SesionUsuario sesion,
                                                  String permiso, String nombre, Supplier<List<T>> source, List<String> avisos) {
        if (sesion == null || !sesion.tiene(permiso)) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            RequestAttributes prev = RequestContextHolder.getRequestAttributes();
            if (attrs != null) RequestContextHolder.setRequestAttributes(attrs);
            try {
                return consulta(sesion, permiso, nombre, source, avisos);
            } finally {
                if (prev != null) RequestContextHolder.setRequestAttributes(prev);
                else RequestContextHolder.resetRequestAttributes();
            }
        }, executor);
    }

    /** null significa no disponible; una lista vacía es una consulta exitosa sin registros. */
    private <T> List<T> consulta(SesionUsuario sesion, String permiso, String nombre,
                                 Supplier<List<T>> source, List<String> avisos) {
        if (sesion == null || !sesion.tiene(permiso)) return null;
        try {
            var result = source.get();
            if (result == null) throw new IllegalStateException("Respuesta incompleta");
            return result;
        } catch (RestClientException | IllegalStateException exception) {
            avisos.add(nombre + ": " + errors.describe(exception));
            return null;
        }
    }
}

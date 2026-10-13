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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
        List<String> avisos = new ArrayList<>();
        var datos = new PanelAdminDatos(
                consulta(sesion, "REGISTRAR_DONACIONES", "Donaciones", donaciones::donaciones, avisos),
                consulta(sesion, "ASIGNAR_DONACIONES", "Necesidades", donaciones::necesidades, avisos),
                consulta(sesion, "GESTIONAR_CAMIONES", "Flota", logistica::camiones, avisos),
                consulta(sesion, "SEGUIR_ENTREGAS", "Entregas", logistica::entregas, avisos),
                consulta(sesion, "GESTIONAR_CAMIONES", "Rutas", logistica::rutas, avisos),
                consulta(sesion, "SEGUIR_ENTREGAS", "GPS", logistica::ubicaciones, avisos),
                consulta(sesion, "ASIGNAR_DONACIONES", "Sugerencias", donaciones::sugerencias, avisos),
                consulta(sesion, "VER_RANKINGS", "Ranking",
                        () -> incentivos.rankingConNombres(donaciones.nombresDonantes()), avisos),
                avisos);
        return mapper.mapear(datos, LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")));
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

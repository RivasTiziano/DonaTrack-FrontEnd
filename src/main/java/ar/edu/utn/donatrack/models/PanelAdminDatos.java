package ar.edu.utn.donatrack.models;
import ar.edu.utn.donatrack.dto.*;
import java.util.*;
/** null significa fuente no disponible; una lista vacía significa consulta exitosa sin registros. */
public record PanelAdminDatos(List<ApiDtos.Donacion> donaciones,List<ApiDtos.Necesidad> necesidades,
 List<ApiDtos.Camion> camiones,List<ApiDtos.Entrega> entregas,List<ApiDtos.Ruta> rutas,
 List<LogisticaDetalles.Seguimiento> ubicaciones,List<ApiDtos.Sugerencia> sugerencias,
 List<IncentivosDtos.FilaRanking> ranking,List<String> avisos) {}

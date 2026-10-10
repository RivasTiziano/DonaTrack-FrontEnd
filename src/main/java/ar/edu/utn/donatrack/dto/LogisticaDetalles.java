package ar.edu.utn.donatrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class LogisticaDetalles {
    private LogisticaDetalles() {}
    public record IniciarRuta(String chofer) {}
    public record PlanificarRuta(@JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate fecha) {}
    public record Planificacion(int cantidadEntregas, int cantidadLotes, List<String> requestIds) {}
    public record Direccion(String calle, String numero, String ciudad, String provincia, Double latitud, Double longitud) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parada(Long id, Integer orden, Direccion direccion, List<ApiDtos.Entrega> entregas,
                         String estimatedArrivalTime) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ruta(Long id, String chofer, LocalDate fecha, String estado, Long camionId,
                       List<Parada> paradas, Double totalDistanciaKm, Integer totalDuracionMins,
                       String estimatedStartTime, String estimatedEndTime) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ubicacion(String patente, Double latitud, Double longitud, Float velocidad,
                            LocalDateTime ultimaActualizacion) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Seguimiento(String patente, Double latitud, Double longitud, Float velocidad,
                              LocalDateTime ultimaActualizacion, Long rutaId, int entregasTotales,
                              int entregasVisitadas, int entregasPendientes, double porcentajeAvance,
                              String proximaParadaDireccion) {}
}

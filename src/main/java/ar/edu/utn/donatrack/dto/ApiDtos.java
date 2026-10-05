package ar.edu.utn.donatrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos de lectura, independientes de las entidades y de la BDD del backend. */
public final class ApiDtos {
    private ApiDtos() {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Categoria(Long id, String nombre, boolean activo, List<SubCategoria> subCategorias) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubCategoria(Long id, String nombre, boolean activo) {}
    public record CategoriaBien(String nombreCategoria, SubCategoriaBien subCategoria) {}
    public record SubCategoriaBien(String nombreSubCategoria) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bien(Long id, String descripcion, String foto, Float cantidad, Float cantidadDisponible,
                       CategoriaBien categoria, String unidadMedida, String tipoBien,
                       LocalDate fechaDeVencimiento, Boolean fueUsado) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Donacion(Long id, Long donanteId, String descripcionGeneral, Bien bien,
                           String estado, LocalDateTime fechaRecepcion) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Necesidad(Long necesidadId, Long entidadBeneficiariaId, Bien bien, String descripcion,
                            String tipoNecesidad, String periodo, Float cantidadRecibida,
                            Float cantidadRestante, String estado) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Beneficiario(Long id, String razonSocial, String tipoEntidad, String rubro, boolean activo) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Camion(Long id, String patente, Float capacidadVolumen, Float altura,
                         Float capacidadCarga, String estado) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entrega(Long id, Long asignacionId, String estado, String justificacion,
                          List<String> fotos, Long camionId, String camionPatente) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ruta(Long id, String chofer, LocalDate fecha, String estado,
                       Long camionId, Double totalDistanciaKm, Integer totalDuracionMins) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ranking(Long idDonante, long misionesResueltas) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Algoritmo(String nombre, boolean activo) {}
    public record Top(String nombreAlgoritmo, List<Necesidad> topDiezNecesidades) {}
    public record Sugerencia(boolean hayInterseccion, List<Necesidad> mejoresNecesidades,
                             List<Top> tops, Long donacionId) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Asignacion(Long id, Long donacionId, Long necesidadId, Long entidadBeneficiariaId,
                             Float cantidadAsignada, String estado, Long camionId) {}
    public record ErrorCsv(long fila, String mensaje) {}
    public record Importacion(int filasProcesadas, int donantesCreados, int donantesActualizados,
                              int filasRechazadas, List<ErrorCsv> errores) {}
}

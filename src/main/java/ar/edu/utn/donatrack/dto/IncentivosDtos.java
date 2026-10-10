package ar.edu.utn.donatrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class IncentivosDtos {
    private IncentivosDtos() {}
    public record MisionRequest(String tipo, Integer valorObjetivo) {}
    public record CategoriaRequest(String nombre, Long categoriaSiguienteId, MisionRequest mision) {}
    public record Visibilidad(boolean insigniasVisibles) {}
    public record Insignia(String nombre, String descripcion, String imagenUrl) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mision(Long id, String tipo, String nombre, String descripcion, Integer valorObjetivo,
                         Integer orden, Insignia insigniaPremio) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Categoria(Long id, String nombre, Long categoriaSiguienteId, List<Mision> misiones) {}
    public record MetricaMensual(String periodo, int totalDonaciones, int impacto, int entidadesAyudadas) {}
    public record Metricas(int totalDonacionesHistoricas, int impactoAcumulado, int totalOrganizacionesAyudadas,
                           String categoriaActual, List<MetricaMensual> desgloseMensual) {}
    public record MisionCompletada(long idMision, String nombreMision, String descripcionMision, LocalDateTime fechaCompletada) {}
    public record Auditoria(Long id, String categoriaAnterior, String categoriaNueva, LocalDateTime fechaCambio) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Progreso(Long idMision, String nombreMision, String descripcionMision, Integer valorActual,
                           Integer valorObjetivo, Double porcentajeProgreso, Boolean completada, Integer orden, String mensaje) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DonanteRanking(Long idReferencia) {}
    public record Posicion(int posicion, int misionesCompletadas, DonanteRanking donante) {}
    public record RankingHistorico(Long id, LocalDate periodo, List<Posicion> posiciones) {}
}

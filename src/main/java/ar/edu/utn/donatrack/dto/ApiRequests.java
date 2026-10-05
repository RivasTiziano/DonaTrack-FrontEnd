package ar.edu.utn.donatrack.dto;

import java.time.LocalDate;
import java.util.List;

public final class ApiRequests {
    private ApiRequests() {}
    public record Nombre(String nombre) {}
    public record Bien(String descripcion, String foto, Float cantidad, Long subCategoriaId,
                       String unidadMedida, String tipoBien, LocalDate fechaDeVencimiento, Boolean fueUsado) {}
    public record Donacion(Long donanteId, String descripcionGeneral, List<Bien> bienes) {}
    public record Necesidad(Long entidadBeneficiariaId, Bien bien, String descripcion,
                            String tipoNecesidad, String periodo) {}
    // El contrato PUT utiliza "tipoDeNecesidad", distinto del POST.
    public record ActualizarNecesidad(Bien bien, String descripcion, String tipoDeNecesidad, String periodo) {}
    public record EstadoDonacion(String descripcionGeneral, String estado, String justificacion) {}
    public record EstadoCamion(String estado) {}
    public record Camion(String patente, Float capacidadVolumen, Float altura, Float capacidadCarga) {}
    public record Algoritmo(String nombre, boolean activo) {}
    public record Asignacion(Long donacionId, Long necesidadId, Float cantidadAAsignar) {}
}

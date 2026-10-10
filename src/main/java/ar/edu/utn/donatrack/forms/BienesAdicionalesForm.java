package ar.edu.utn.donatrack.forms;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
/** Campos repetidos de bienes; el controller solo realiza el binding. */
public record BienesAdicionalesForm(List<String> bienDescripcion, List<String> bienFoto,
        List<Float> bienCantidad, List<Long> bienSubCategoriaId, List<String> bienUnidadMedida,
        List<String> bienTipoBien,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> bienFechaDeVencimiento,
        List<Boolean> bienFueUsado) {}

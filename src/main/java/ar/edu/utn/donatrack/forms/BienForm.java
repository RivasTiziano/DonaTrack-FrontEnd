package ar.edu.utn.donatrack.forms;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/** Binding del formulario; las reglas de negocio siguen en la API. */
public record BienForm(String descripcion, String foto, Float cantidad, Long subCategoriaId,
                       String unidadMedida, String tipoBien,
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDeVencimiento, Boolean fueUsado) {

}

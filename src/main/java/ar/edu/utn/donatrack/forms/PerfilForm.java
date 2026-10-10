package ar.edu.utn.donatrack.forms;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/** Edición de datos básicos: conserva contactos y representantes leídos del backend. */
public record PerfilForm(String nombre, String apellido,
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
                         String numeroDocumento, String genero, String razonSocial, String tipoEntidad,
                         String rubro, String calle, String numero, String ciudad, String provincia,
                         Float latitud, Float longitud, String contactoTipo, String contactoValor, Boolean activo) {

}

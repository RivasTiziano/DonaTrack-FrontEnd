package ar.edu.utn.donatrack.forms;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record DonanteForm(String nombre, String apellido,
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
                          String numeroDocumento, String genero, String razonSocial, String tipoEntidad,
                          String rubro, String calle, String numero, String ciudad, String provincia,
                          Float latitud, Float longitud, String email, String telefono,
                          String representanteNombre, String representanteApellido,
                          String representanteDocumento,
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate representanteNacimiento,
                          String representanteGenero) {}

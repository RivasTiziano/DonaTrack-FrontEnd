package ar.edu.utn.donatrack.models;

import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("unidades")
public class Unidades {
    public String formatear(Number cantidad, String unidad) {
        if (cantidad == null) return "—";
        String nombre = unidad == null ? "" : unidad.toLowerCase(Locale.ROOT);
        if (cantidad.doubleValue() != 1d) nombre = switch (nombre) {
            case "kilogramo" -> "kilogramos";
            case "gramo" -> "gramos";
            case "litro" -> "litros";
            case "unidad" -> "unidades";
            default -> nombre;
        };
        String valor = new BigDecimal(cantidad.toString()).stripTrailingZeros().toPlainString().replace('.', ',');
        return nombre.isBlank() ? valor : valor + " " + nombre;
    }
}

package ar.edu.utn.donatrack.validators;

import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.List;

/** Reglas de la confirmación de recepción que la entidad envía desde su panel. */
@Component
public class RecepcionValidator {
    public static final String RECIBIDA = "RECIBIDA";
    public static final String NO_RECIBIDA = "NO_RECIBIDA";

    /** La entidad indica la asignación; el entregaId lo resuelve el servidor, nunca el formulario. */
    public void validar(Long asignacionId, String resultado, List<String> fotos, String justificacion) {
        if (asignacionId == null) throw new FormularioInvalidoException("Indicá la donación a confirmar.");
        validarContenido(resultado, fotos, justificacion);
    }

    public void validarContenido(String resultado, List<String> fotos, String justificacion) {
        if (!RECIBIDA.equals(resultado) && !NO_RECIBIDA.equals(resultado))
            throw new FormularioInvalidoException("Indicá si la entrega fue recibida o no.");
        if (RECIBIDA.equals(resultado)) {
            if (fotos == null || fotos.isEmpty())
                throw new FormularioInvalidoException("Para confirmar la recepción, cargá al menos una foto (una URL http o https por línea).");
            for (String foto : fotos)
                if (!urlValida(foto))
                    throw new FormularioInvalidoException("La foto \"" + foto + "\" no es una URL http o https válida.");
        } else if (justificacion == null || justificacion.isBlank()) {
            throw new FormularioInvalidoException("Explicá por qué la entrega no fue recibida.");
        }
    }

    static boolean urlValida(String texto) {
        try {
            URI uri = URI.create(texto);
            String esquema = uri.getScheme();
            return ("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema)) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

package ar.edu.utn.donatrack.services.internal;

import org.springframework.web.client.RestClientException;
import java.util.List;
import java.util.function.Supplier;

/** Mantiene la carga opcional del maquetado público; no se usa para confirmar escrituras. */
public final class ConsultaOpcional {
    private ConsultaOpcional() {}

    public static <T> List<T> lista(Supplier<List<T>> query) {
        try {
            List<T> values = query.get();
            return values == null ? List.of() : values;
        } catch (RestClientException | IllegalStateException exception) {
            return List.of();
        }
    }
}

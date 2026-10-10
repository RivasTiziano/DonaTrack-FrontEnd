package ar.edu.utn.donatrack.validators;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import org.springframework.stereotype.Component;
/** Formatos de entrada de las páginas públicas; no autentica usuarios. */
@Component
public class WebValidator {
    public Long identificador(String value) {
        try {
            long id=Long.parseLong(value);
            if(id<1) throw new NumberFormatException();
            return id;
        } catch(NumberFormatException e) { throw new FormularioInvalidoException("El identificador de donación es inválido."); }
    }
}

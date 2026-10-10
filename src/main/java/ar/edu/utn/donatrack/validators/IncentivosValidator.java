package ar.edu.utn.donatrack.validators;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import org.springframework.stereotype.Component;
import java.util.Set;
@Component
public class IncentivosValidator {
    private static final Set<String> TIPOS=Set.of("RACHA","COMPLETITUD","DONACIONESEXITOSAS","HABILDONADOR");
    public void categoria(String nombre,String tipo,Integer objetivo) {
        if(nombre==null || nombre.isBlank()) throw new FormularioInvalidoException("Indicá un nombre para la categoría.");
        mision(tipo,objetivo);
    }
    public void mision(String tipo,Integer objetivo) {
        if(!TIPOS.contains(tipo==null?"":tipo) || objetivo==null || objetivo<1)
            throw new FormularioInvalidoException("Seleccioná un tipo de misión válido y un objetivo mayor que cero.");
    }
}

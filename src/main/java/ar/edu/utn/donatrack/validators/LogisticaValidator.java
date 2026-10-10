package ar.edu.utn.donatrack.validators;
import org.springframework.stereotype.Component;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import java.time.LocalDate;
@Component
public class LogisticaValidator {
    public void chofer(String chofer) { require(chofer!=null && !chofer.isBlank(),"Indicá el nombre del chofer."); }
    public void fecha(LocalDate fecha) { require(fecha!=null,"Indicá la fecha de planificación."); }
    public void camion(String patente,Float volumen,Float altura,Float carga) {
        require(patente!=null && !patente.isBlank() && positivo(volumen) && positivo(altura) && positivo(carga),
                "Completá patente, volumen, altura y carga con valores positivos.");
    }
    private boolean positivo(Float n) { return n!=null && Float.isFinite(n) && n>0; }
    private void require(boolean ok,String message) { if(!ok) throw new FormularioInvalidoException(message); }
}

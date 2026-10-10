package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.ApiRequests;
import org.springframework.stereotype.Component;
@Component
public class LogisticaMapper {
    public ApiRequests.Camion camion(String patente,Float volumen,Float altura,Float carga) {
        return new ApiRequests.Camion(patente,volumen,altura,carga);
    }
}

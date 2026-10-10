package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import org.springframework.stereotype.Component;
@Component
public class IncentivosMapper {
    public IncentivosDtos.MisionRequest mision(String tipo,Integer objetivo) { return new IncentivosDtos.MisionRequest(tipo,objetivo); }
    public IncentivosDtos.CategoriaRequest categoria(String nombre,Long siguiente,String tipo,Integer objetivo) {
        return new IncentivosDtos.CategoriaRequest(nombre,siguiente,mision(tipo,objetivo));
    }
}

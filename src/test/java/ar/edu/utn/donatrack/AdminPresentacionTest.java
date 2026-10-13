package ar.edu.utn.donatrack;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.mappers.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class AdminPresentacionTest {
    @Test void ordenaCategoriasPorSuSiguienteSinPerderCiclos() {
        var mapper=new IncentivosMapper();
        var primera=new IncentivosDtos.Categoria(1L,"Inicial",2L,List.of());
        var segunda=new IncentivosDtos.Categoria(2L,"Avanzada",null,List.of());
        assertEquals(List.of(primera,segunda),mapper.ordenarCategorias(List.of(segunda,primera)));
        var ciclo=new IncentivosDtos.Categoria(2L,"Avanzada",1L,List.of());
        assertEquals(2,mapper.ordenarCategorias(List.of(primera,ciclo)).size());
    }
    @Test void estadosLegiblesConFallbackParaContratosNuevos() {
        var estados=new AdminEstados();
        assertEquals("En depósito",estados.etiqueta("EN_DEPOSITO"));
        assertEquals("chip-danger",estados.tono("NO_RECIBIDA"));
        assertEquals("—",estados.etiqueta(null));
        assertEquals("chip-neutral",estados.tono("NUEVO_ESTADO"));
    }
}

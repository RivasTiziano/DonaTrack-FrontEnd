package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
import ar.edu.utn.donatrack.mappers.WebMapper;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EntregasPublicasMapperTest {
    @Test void destacadasSoloMesActualYGaleriaSoloFotografiasRecientes() {
        var month = YearMonth.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        var data = new ArrayList<EntregaPublicaDto>();
        for (long id=1;id<=7;id++)
            data.add(new EntregaPublicaDto(id,"Prueba "+id,month.atDay((int)id).atStartOfDay(),5f,"UNIDAD","/img/donacion-"+id+".webp"));
        data.add(new EntregaPublicaDto(8L,"Sin foto",month.atDay(8).atStartOfDay(),2f,"UNIDAD",null));
        data.add(new EntregaPublicaDto(9L,"Mes anterior",month.minusMonths(1).atEndOfMonth().atStartOfDay(),2f,"UNIDAD","/img/hero-donaciones.jpg"));
        var model=new WebMapper().entregasLanding(data);
        var featured=(List<Map<String,Object>>)model.get("featuredDonations");
        var gallery=(List<Map<String,Object>>)model.get("recentDeliveries");
        assertEquals(List.of(8L,7L,6L),featured.stream().map(m->m.get("id")).toList());
        assertEquals(List.of(7L,6L,5L,4L,3L,2L),gallery.stream().map(m->m.get("id")).toList());
        assertTrue(featured.stream().allMatch(m->m.get("donorName")==null && m.get("beneficiary")==null));
    }

    @Test void galeriaNoRepiteFotosPeroMapaConservaTodasLasEntregas() {
        var date = LocalDateTime.of(2026,10,10,12,0);
        var data = List.of(
            new EntregaPublicaDto(1L,"Más reciente",date,5f,"UNIDAD","/img/alimentos.webp"),
            new EntregaPublicaDto(2L,"Otra entrega con la misma foto",date.minusDays(1),10f,"UNIDAD","/img/alimentos.webp"),
            new EntregaPublicaDto(3L,"Foto diferente",date.minusDays(2),3f,"UNIDAD","/img/juguetes.webp")
        );
        var model = new WebMapper().entregasLanding(data);
        var gallery = (List<Map<String,Object>>)model.get("recentDeliveries");
        assertEquals(List.of(1L,3L), gallery.stream().map(m -> m.get("id")).toList());
        assertEquals(data,model.get("publicDeliveries"));
    }
}

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
            data.add(new EntregaPublicaDto(id,"Prueba "+id,month.atDay((int)id).atStartOfDay(),5f,"UNIDAD","/img/hero-donaciones.jpg"));
        data.add(new EntregaPublicaDto(8L,"Sin foto",month.atDay(8).atStartOfDay(),2f,"UNIDAD",null));
        data.add(new EntregaPublicaDto(9L,"Mes anterior",month.minusMonths(1).atEndOfMonth().atStartOfDay(),2f,"UNIDAD","/img/hero-donaciones.jpg"));
        var model=new WebMapper().entregasLanding(data);
        var featured=(List<Map<String,Object>>)model.get("featuredDonations");
        var gallery=(List<Map<String,Object>>)model.get("recentDeliveries");
        assertEquals(List.of(8L,7L,6L),featured.stream().map(m->m.get("id")).toList());
        assertEquals(List.of(7L,6L,5L,4L,3L,2L),gallery.stream().map(m->m.get("id")).toList());
        assertTrue(featured.stream().allMatch(m->m.get("donorName")==null && m.get("beneficiary")==null));
    }
}

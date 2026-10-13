package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.EntregaMapaDto;
import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
import ar.edu.utn.donatrack.mappers.WebMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EntregasPublicasMapperTest {
    private static final LocalDateTime HOY = LocalDateTime.of(2026, 10, 13, 10, 0);
    private static final EntregaMapaDto.EntidadUbicada COMEDOR = new EntregaMapaDto.EntidadUbicada("Comedor Sol", "La Plata", "Buenos Aires", -34.92f, -57.95f);

    private static EntregaPublicaDto destacada(long id, LocalDateTime fecha, String imagen, List<String> fotos) {
        return new EntregaPublicaDto(id, "Entrega " + id, fecha, 1f, "UNIDAD", imagen, "Alimentos", "Secos",
                new EntregaPublicaDto.EntidadResumen("Comedor Sol", "La Plata"), fotos);
    }

    @SuppressWarnings("unchecked")
    @Test void destacadasSonLasTresMasRecientesYLaFotoRevisadaVaPrimero() {
        var model = new WebMapper().landing(List.of(
                destacada(1, HOY.minusDays(9), "bien1.jpg", List.of()),
                destacada(2, HOY.minusDays(1), "bien2.jpg", List.of("revisada2.jpg")),
                destacada(3, HOY.minusDays(5), null, List.of()),
                destacada(4, HOY.minusDays(2), "bien4.jpg", List.of())), List.of());
        var cards = (List<Map<String,Object>>) model.get("featuredDonations");
        assertEquals(List.of(2L, 4L, 3L), cards.stream().map(c -> c.get("id")).toList());
        assertEquals("revisada2.jpg", cards.get(0).get("image"));
    }

    @SuppressWarnings("unchecked")
    @Test void galeriaSoloTomaEntregasConFotoYNoRepiteFotos() {
        var model = new WebMapper().landing(List.of(
                destacada(1, HOY.minusDays(1), "misma.jpg", List.of()),
                destacada(2, HOY.minusDays(2), "misma.jpg", List.of()),
                destacada(3, HOY.minusDays(3), null, List.of())), List.of());
        var galeria = (List<Map<String,Object>>) model.get("recentDeliveries");
        assertEquals(1, galeria.size());
        assertEquals("misma.jpg", galeria.get(0).get("image"));
    }

    @SuppressWarnings("unchecked")
    @Test void mapaConservaTodasLasEntregasConLaUbicacionDeLaEntidad() {
        var model = new WebMapper().landing(List.of(), List.of(
                new EntregaMapaDto(1L, HOY, "Alimentos", "Secos", 5f, "KILOGRAMO", COMEDOR),
                new EntregaMapaDto(2L, HOY, "Ropa", null, 3f, "UNIDAD", COMEDOR),
                new EntregaMapaDto(3L, HOY, "Muebles", "Sillas", 2f, "UNIDAD", null)));
        var puntos = (List<Map<String,Object>>) model.get("publicDeliveries");
        assertEquals(3, puntos.size());
        assertEquals(-34.92f, puntos.get(0).get("latitud"));
        assertEquals("Alimentos / Secos — Comedor Sol (La Plata)", puntos.get(0).get("descripcion"));
        assertEquals("Ropa — Comedor Sol (La Plata)", puntos.get(1).get("descripcion"));
        assertNull(puntos.get(2).get("latitud"));
        assertFalse(model.containsKey("itemsCount"), "No se suman cantidades con unidades distintas");
    }
}

package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.EntregaMapaDto;
import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
import ar.edu.utn.donatrack.mappers.WebMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EntregasPublicasMapperTest {
    private static final LocalDateTime HOY = LocalDateTime.of(2026, 10, 10, 12, 0);

    private static EntregaPublicaDto entrega(long id, LocalDateTime fecha, String imagen, List<String> fotos) {
        return new EntregaPublicaDto(id, "Entrega " + id, fecha, 5f, "KILOGRAMO", imagen, "Alimentos", "Secos",
                new EntregaPublicaDto.EntidadResumen("Comedor Sol", "La Plata"), fotos);
    }

    private static List<Long> ids(List<Map<String,Object>> items) {
        return items.stream().map(m -> (Long) m.get("id")).toList();
    }

    @Test void destacadasSonLasTresMasRecientesYLaFotoRevisadaVaPrimero() {
        var data = List.of(
                entrega(1L, HOY.minusDays(5), "/img/a.jpg", List.of()),
                entrega(2L, HOY.minusDays(1), null, List.of("https://fotos.example/2.jpg")),
                entrega(3L, HOY.minusDays(2), "/img/c.jpg", List.of("https://fotos.example/3a.jpg", "https://fotos.example/3b.jpg")),
                entrega(4L, HOY.minusDays(3), "/img/d.jpg", List.of()));
        var model = new WebMapper().landing(data);
        var featured = (List<Map<String,Object>>) model.get("featuredDonations");
        assertEquals(List.of(2L, 3L, 4L), ids(featured));
        assertEquals("https://fotos.example/2.jpg", featured.get(0).get("image"));
        assertEquals("https://fotos.example/3a.jpg", featured.get(1).get("image"));
        assertEquals("/img/d.jpg", featured.get(2).get("image"));
        assertEquals("Alimentos", featured.get(0).get("category"));
        assertEquals("Comedor Sol", featured.get(0).get("entity"));
        assertEquals("La Plata", featured.get(0).get("locality"));
        assertEquals("09/10/2026", featured.get(0).get("date"));
    }

    @Test void galeriaSoloTomaEntregasConImagen() {
        var data = List.of(
                entrega(1L, HOY.minusDays(5), "/img/a.jpg", List.of()),
                entrega(2L, HOY.minusDays(1), null, List.of("https://fotos.example/2.jpg")),
                entrega(3L, HOY.minusDays(2), "/img/c.jpg", List.of()),
                entrega(4L, HOY.minusDays(3), "/img/d.jpg", List.of()));
        var gallery = (List<Map<String,Object>>) new WebMapper().landing(data).get("recentDeliveries");
        assertEquals(List.of(3L, 4L, 1L), ids(gallery));
    }

    @Test void mapaCuentaEntregasEntidadesYUnidadesReales() {
        var comedor = new EntregaMapaDto.EntidadUbicada("Comedor Sol", "La Plata", "Buenos Aires", -34.92f, -57.95f);
        var hogar = new EntregaMapaDto.EntidadUbicada("Hogar Luz", "Quilmes", "Buenos Aires", -34.72f, -58.25f);
        var data = List.of(
                new EntregaMapaDto(1L, HOY, "Alimentos", "Secos", 5f, "KILOGRAMO", comedor),
                new EntregaMapaDto(2L, HOY, "Ropa", "Abrigo", 3f, "UNIDAD", comedor),
                new EntregaMapaDto(3L, HOY, "Alimentos", "Secos", 2f, "KILOGRAMO", hogar));
        var model = new WebMapper().map(data);
        assertEquals(3, model.get("donationsCount"));
        assertEquals(2, model.get("beneficiariesCount"));
        assertFalse(model.containsKey("itemsCount"), "No se suman cantidades con unidades distintas");
        var donations = (List<Map<String,Object>>) model.get("donations");
        assertEquals("Comedor Sol", donations.get(0).get("entity_name"));
        assertEquals(-34.92f, ((Map<String,Object>) donations.get(0).get("location")).get("lat"));
    }

    @Test void puntoSinCoordenadasQuedaSinLocation() {
        var sinCoords = new EntregaMapaDto.EntidadUbicada("Comedor Sol", "La Plata", "Buenos Aires", null, null);
        var model = new WebMapper().map(List.of(new EntregaMapaDto(1L, HOY, "Alimentos", null, 1f, "UNIDAD", sinCoords)));
        var punto = ((List<Map<String,Object>>) model.get("donations")).get(0);
        assertNull(((Map<String,Object>) punto.get("location")).get("lat"));
    }

    @Test void detalleMuestraLasFotosRevisadasOLaImagenDelBien() {
        var conFotos = entrega(7L, HOY, "/img/a.jpg", List.of("https://fotos.example/7a.jpg", "https://fotos.example/7b.jpg"));
        var donation = (Map<String,Object>) new WebMapper().entregaDetalle(conFotos).get("donation");
        assertEquals(List.of("https://fotos.example/7a.jpg", "https://fotos.example/7b.jpg"), donation.get("photos"));
        assertEquals("Comedor Sol", donation.get("entity_name"));

        var sinFotos = entrega(8L, HOY, "/img/b.jpg", List.of());
        var donation2 = (Map<String,Object>) new WebMapper().entregaDetalle(sinFotos).get("donation");
        assertEquals(List.of("/img/b.jpg"), donation2.get("photos"));
    }
}

package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.services.PublicoService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cliente público contra un servidor HTTP local: verifica las rutas reales, el parámetro desde y que
 * ninguna llamada pública lleve Authorization. Si la API no responde, las listas quedan vacías.
 */
class PublicoServiceTest {
    private static final ZoneId BA = ZoneId.of("America/Argentina/Buenos_Aires");
    private HttpServer server;
    private final List<String> rutas = new CopyOnWriteArrayList<>();
    private final List<String> authorizations = new CopyOnWriteArrayList<>();
    private volatile int status = 200;
    private volatile String cuerpo = "[]";

    @BeforeEach void iniciarServidor() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            rutas.add(exchange.getRequestURI().toString());
            authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = cuerpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (var out = exchange.getResponseBody()) { out.write(body); }
        });
        server.start();
    }

    @AfterEach void detenerServidor() {
        server.stop(0);
    }

    private PublicoService servicio() {
        return new PublicoService("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    @Test void destacadasPidenLosUltimos30DiasSinAuthorization() {
        cuerpo = "[{\"id\":5,\"descripcion\":\"Fideos\",\"fechaEntrega\":\"2026-10-01T10:00:00\",\"cantidad\":5,\"unidad\":\"KG\","
                + "\"imagen\":null,\"categoria\":\"Alimentos\",\"subcategoria\":\"Secos\","
                + "\"entidad\":{\"nombre\":\"Comedor Sol\",\"localidad\":\"La Plata\"},\"fotos\":[\"https://fotos.example/5.jpg\"]}]";
        var destacadas = servicio().donacionesDestacadas();

        String esperado = "/publico/entregas?desde=" + LocalDate.now(BA).minusDays(30);
        assertEquals(List.of(esperado), rutas);
        assertEquals(List.of(), authorizations.stream().filter(a -> a != null).toList());
        assertEquals(1, destacadas.size());
        assertEquals("Comedor Sol", destacadas.get(0).entidad().nombre());
        assertEquals(List.of("https://fotos.example/5.jpg"), destacadas.get(0).fotos());
    }

    @Test void mapaPideElHistoricoCompletoConCoordenadasDeLaEntidad() {
        cuerpo = "[{\"id\":1,\"fechaEntrega\":\"2026-01-15T10:00:00\",\"categoria\":\"Alimentos\",\"subcategoria\":\"Secos\","
                + "\"cantidad\":5,\"unidad\":\"KG\",\"entidad\":{\"nombre\":\"Comedor Sol\",\"localidad\":\"La Plata\","
                + "\"provincia\":\"Buenos Aires\",\"latitud\":-34.92,\"longitud\":-57.95}}]";
        var puntos = servicio().entregasEnMapa();

        assertEquals(List.of("/publico/entregas/mapa"), rutas);
        assertEquals(List.of(), authorizations.stream().filter(a -> a != null).toList());
        assertEquals(1, puntos.size());
        assertEquals(-34.92f, puntos.get(0).entidad().latitud(), 0.001f);
    }

    @Test void detalleUsaLaRutaPublicaYDevuelveLasFotos() {
        cuerpo = "{\"id\":9,\"descripcion\":\"Leche\",\"fechaEntrega\":\"2026-10-02T10:00:00\",\"cantidad\":2,\"unidad\":\"L\","
                + "\"imagen\":null,\"categoria\":\"Alimentos\",\"subcategoria\":null,\"entidad\":null,"
                + "\"fotos\":[\"https://fotos.example/9.jpg\"]}";
        var detalle = servicio().detalleEntrega("9");
        assertEquals(List.of("/publico/entregas/9"), rutas);
        assertEquals(List.of("https://fotos.example/9.jpg"), detalle.fotos());
    }

    @Test void apiCaidaDevuelveListasVaciasSinError() {
        status = 500;
        var servicio = servicio();
        assertEquals(List.of(), servicio.donacionesDestacadas());
        assertEquals(List.of(), servicio.entregasEnMapa());
        assertNull(servicio.detalleEntrega("9"));
    }

    @Test void idInvalidoNoLlegaAlServidor() {
        assertNull(servicio().detalleEntrega("../admin"));
        assertTrue(rutas.isEmpty());
    }
}

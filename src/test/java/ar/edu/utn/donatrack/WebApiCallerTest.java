package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.ApiRequests;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;

/** Servidor HTTP local de prueba: no consulta ni modifica los microservicios reales. */
class WebApiCallerTest {
    HttpServer server;
    DonacionesApiService service;
    AtomicReference<String> request = new AtomicReference<>();
    AtomicReference<String> method = new AtomicReference<>();
    AtomicReference<String> contentType = new AtomicReference<>();

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        service = new DonacionesApiService(new WebApiCallerService(RestClient.builder()),
                "http://127.0.0.1:" + server.getAddress().getPort());
    }
    @AfterEach void stop() { server.stop(0); }

    void endpoint(String path, int status, String json) {
        server.createContext(path, exchange -> {
            request.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            method.set(exchange.getRequestMethod()); contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            if (status == 204) exchange.sendResponseHeaders(status, -1);
            else { exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); }
            exchange.close();
        });
    }

    @Test void parsesCurrentDonationContractIncludingUnknownFields() {
        endpoint("/donaciones", 200, """
                [{"id":9,"donanteId":1,"descripcionGeneral":"Real","estado":"EN_DEPOSITO",
                "fechaRecepcion":"2026-10-05T10:00:00","bien":{"id":3,"descripcion":"Arroz",
                "cantidad":80,"cantidadDisponible":50,"unidadMedida":"KILOGRAMO","nuevoCampo":true}}]
                """);
        assertThat(service.donaciones().get(0).bien().cantidadDisponible()).isEqualTo(50f);
        assertThat(method.get()).isEqualTo("GET");
    }
    @Test void assignmentSendsCurrentJsonContract() {
        endpoint("/asignaciones", 201, "{\"id\":7}");
        service.asignar(new ApiRequests.Asignacion(9L, 2L, 30f));
        assertThat(method.get()).isEqualTo("POST");
        assertThat(request.get()).contains("\"cantidadAAsignar\":30.0", "\"donacionId\":9");
    }
    @Test void uploadsRealMultipartFileWithCorrectPartNameAndBoundary() {
        endpoint("/donantes/importar", 200, "{\"filasProcesadas\":100,\"donantesCreados\":100,\"donantesActualizados\":0,\"filasRechazadas\":0,\"errores\":[]}");
        var file = new MockMultipartFile("archivo", "donantes.csv", "text/csv", "TipoPersona,TipoDoc".getBytes(StandardCharsets.UTF_8));
        assertThat(service.importar(file).filasProcesadas()).isEqualTo(100);
        assertThat(contentType.get()).contains("multipart/form-data", "boundary=");
        assertThat(request.get()).contains("name=\"archivo\"", "filename=\"donantes.csv\"", "TipoPersona,TipoDoc");
    }
    @Test void patchAndDeleteUseRealHttpMethodsAndHandle204() {
        endpoint("/donaciones/9/estado", 200, "{}");
        service.estado(9L, new ApiRequests.EstadoDonacion(null, "VENCIDA", "Vencimiento"));
        assertThat(method.get()).isEqualTo("PATCH");
        endpoint("/donaciones/9", 204, "");
        service.eliminarDonacion(9L);
        assertThat(method.get()).isEqualTo("DELETE");
    }
    @Test void errorsAreNotSilentlyConvertedToEmptyLists() {
        endpoint("/donaciones", 400, "{\"message\":\"Datos inválidos\"}");
        assertThatThrownBy(service::donaciones).isInstanceOf(HttpClientErrorException.class);
    }
}

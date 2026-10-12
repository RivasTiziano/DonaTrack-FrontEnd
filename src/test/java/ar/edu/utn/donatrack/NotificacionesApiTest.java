package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;

/** HTTP simulado local: nunca contacta proveedores ni servicios reales. */
class NotificacionesApiTest {
    HttpServer server;
    NotificacionesApiService api;
    AtomicReference<String> request = new AtomicReference<>();
    AtomicReference<String> method = new AtomicReference<>();
    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        api = new NotificacionesApiService(new WebApiCallerService(RestClient.builder()),
                "http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }
    @AfterEach void stop() { server.stop(0); }
    void endpoint(String path, String body, String type) {
        server.createContext(path, exchange -> {
            method.set(exchange.getRequestMethod());
            request.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", type);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }
    Enviar message() { return new Enviar(new Destinatario("Prueba", "prueba@example.com", null),
            new Mensaje("Aviso", "Contenido"), Medio.EMAIL); }
    @Test void sendsCurrentBackendContractAndParsesPending() throws Exception {
        endpoint("/notificaciones/enviar", "{\"retorno\":\"success\",\"datos\":\"PENDIENTE\",\"mensajeError\":null}", "application/json");
        assertThat(api.enviar(message()).datos()).isEqualTo(Estado.PENDIENTE);
        assertThat(method.get()).isEqualTo("POST");
        var json = new ObjectMapper().readTree(request.get());
        assertThat(json.path("medioContacto").asText()).isEqualTo("EMAIL");
        assertThat(json.path("destinatario").path("email").asText()).isEqualTo("prueba@example.com");
        assertThat(json.path("mensaje").path("asunto").asText()).isEqualTo("Aviso");
        assertThat(json.path("mensaje").path("cuerpo").asText()).isEqualTo("Contenido");
    }
    @Test void doesNotDiscardBusinessFailureInsideHttp200() {
        endpoint("/notificaciones/enviar", "{\"retorno\":\"error\",\"datos\":\"FALLIDA\",\"mensajeError\":\"Rechazado\"}", "application/json");
        assertThat(api.enviar(message())).isEqualTo(new Respuesta("error", Estado.FALLIDA, "Rechazado"));
    }
    @Test void rejectsIncompleteResponseRatherThanReportingSuccess() {
        endpoint("/notificaciones/enviar", "{}", "application/json");
        assertThatThrownBy(() -> api.enviar(message())).isInstanceOf(IllegalStateException.class);
    }
    @Test void checksExistingPlainTextHealthEndpoint() {
        endpoint("/health", "Hola desde el servicio de Notificaciones.", "text/plain");
        assertThat(api.disponible()).isTrue();
        assertThat(method.get()).isEqualTo("GET");
    }

    AtomicReference<String> rawQuery = new AtomicReference<>();
    void endpointWithQuery(String path, String body) {
        server.createContext(path, exchange -> {
            method.set(exchange.getRequestMethod());
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }
    @Test void listsHistoryEncodingThePlusOfThePhoneAndParsesTheBackendContract() {
        endpointWithQuery("/notificaciones", "{\"retorno\":\"success\",\"mensajeError\":null,\"datos\":[{\"id\":7,"
                + "\"asunto\":\"Tu donacion fue asignada\",\"cuerpo\":\"Detalle\",\"medio\":\"SMS\","
                + "\"fecha\":\"2026-10-11T18:30:00\",\"leida\":false,\"fechaLectura\":null,\"referencia\":null,\"campoNuevo\":1}]}");
        var result = api.listar("Ana+Perez@Mail.com", "+5491112345678", true);
        assertThat(rawQuery.get()).contains("telefono=%2B5491112345678").contains("email=Ana%2BPerez%40Mail.com").contains("soloNoLeidas=true");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(7L);
        assertThat(result.get(0).medio()).isEqualTo(Medio.SMS);
        assertThat(result.get(0).fecha()).isEqualTo(java.time.LocalDateTime.of(2026, 10, 11, 18, 30));
        assertThat(result.get(0).leida()).isFalse();
    }
    @Test void countsUnreadAndMarksAsReadWithPatch() {
        endpointWithQuery("/notificaciones/no-leidas/cantidad", "{\"retorno\":\"success\",\"datos\":{\"noLeidas\":3}}");
        assertThat(api.contarNoLeidas("ana@mail.com", null)).isEqualTo(3);
        assertThat(rawQuery.get()).isEqualTo("email=ana@mail.com".replace("@", "%40"));
        endpointWithQuery("/notificaciones/9/leida", "{\"retorno\":\"success\",\"datos\":{}}");
        api.marcarLeida(9L, null, "+5491112345678");
        assertThat(method.get()).isEqualTo("PATCH");
        assertThat(rawQuery.get()).isEqualTo("telefono=%2B5491112345678");
        endpointWithQuery("/notificaciones/leidas", "{\"retorno\":\"success\",\"datos\":{\"marcadas\":2}}");
        api.marcarTodasLeidas("ana@mail.com", null);
        assertThat(method.get()).isEqualTo("PATCH");
    }
    @Test void rejectsAnErrorEnvelopeInsteadOfShowingAnEmptyHistory() {
        endpointWithQuery("/notificaciones", "{\"retorno\":\"error\",\"mensajeError\":\"x\",\"datos\":null}");
        assertThatThrownBy(() -> api.listar("ana@mail.com", null, false)).isInstanceOf(IllegalStateException.class);
    }
}

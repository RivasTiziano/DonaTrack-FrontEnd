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
}

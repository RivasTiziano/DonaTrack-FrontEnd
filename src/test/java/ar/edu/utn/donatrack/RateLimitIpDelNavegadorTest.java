package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.services.internal.IpDelNavegadorInterceptor;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rate limiting del backend (E6, req. 4): servidor HTTP local que comprueba los headers con los que
 * el front avisa la IP real del navegador (X-Front-Auth + X-Client-IP).
 */
class RateLimitIpDelNavegadorTest {
    static final String CLAVE = "clave-del-front-de-prueba";

    HttpServer server;
    AtomicReference<String> claveRecibida = new AtomicReference<>();
    AtomicReference<String> ipRecibida = new AtomicReference<>();

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/publico", exchange -> {
            claveRecibida.set(exchange.getRequestHeaders().getFirst(IpDelNavegadorInterceptor.HEADER_FRONT_KEY));
            ipRecibida.set(exchange.getRequestHeaders().getFirst(IpDelNavegadorInterceptor.HEADER_CLIENT_IP));
            byte[] bytes = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach void stop() {
        server.stop(0);
        RequestContextHolder.resetRequestAttributes();
    }

    String url() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/publico/destacadas"; }

    void llamarConInterceptor(String clave, boolean confiarXForwardedFor) {
        new WebApiCallerService(RestClient.builder(), null, null,
                new IpDelNavegadorInterceptor(clave, confiarXForwardedFor)).get(url(), String.class);
    }

    /** Simula el request del navegador que el front está atendiendo. */
    MockHttpServletRequest navegador(String ip) {
        var request = new MockHttpServletRequest("GET", "/");
        request.setRemoteAddr(ip);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    @Test void enviaLaClaveYLaIpDelNavegador() {
        navegador("200.1.2.3");
        llamarConInterceptor(CLAVE, false);
        assertThat(claveRecibida.get()).isEqualTo(CLAVE);
        assertThat(ipRecibida.get()).isEqualTo("200.1.2.3");
    }

    @Test void detrasDelProxyDeRenderUsaLaUltimaIpDeXForwardedFor() {
        // La primera la puede inventar el navegador; la última la agregó el proxy de Render
        navegador("10.0.0.1").addHeader("X-Forwarded-For", "6.6.6.6, 181.20.30.40");
        llamarConInterceptor(CLAVE, true);
        assertThat(ipRecibida.get()).isEqualTo("181.20.30.40");
    }

    @Test void sinProxyIgnoraXForwardedFor() {
        navegador("10.0.0.1").addHeader("X-Forwarded-For", "181.20.30.40");
        llamarConInterceptor(CLAVE, false);
        assertThat(ipRecibida.get()).isEqualTo("10.0.0.1");
    }

    @Test void sinClaveConfiguradaNoEnviaNada() {
        navegador("200.1.2.3");
        llamarConInterceptor("", false);
        assertThat(claveRecibida.get()).isNull();
        assertThat(ipRecibida.get()).isNull();
    }

    @Test void sinRequestDelNavegadorNoEnviaNada() {
        llamarConInterceptor(CLAVE, false);
        assertThat(claveRecibida.get()).isNull();
        assertThat(ipRecibida.get()).isNull();
    }

    @Test void unaIpMalFormadaNoSeReenvia() {
        navegador("10.0.0.1").addHeader("X-Forwarded-For", "<script>");
        llamarConInterceptor(CLAVE, true);
        assertThat(ipRecibida.get()).isEqualTo("10.0.0.1");
    }
}

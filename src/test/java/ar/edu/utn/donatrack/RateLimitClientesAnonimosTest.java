package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.RegistroDtos;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.PublicoService;
import ar.edu.utn.donatrack.services.RegistroApiService;
import ar.edu.utn.donatrack.services.internal.IpDelNavegadorInterceptor;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Login, registro, landing y mapa no pasan por WebApiCallerService: cada uno tiene su propio RestClient.
 * Son justamente las llamadas anónimas, las que el backend limita por IP, así que también tienen que
 * avisar la IP real del navegador (X-Front-Auth + X-Client-IP).
 */
class RateLimitClientesAnonimosTest {
    static final String CLAVE = "clave-del-front-de-prueba";
    static final String IP_NAVEGADOR = "200.1.2.3";

    HttpServer server;
    /** Ruta pedida → IP del navegador que llegó en X-Client-IP (o "sin-ip"). */
    Map<String, String> ipPorRuta = new ConcurrentHashMap<>();
    Map<String, String> clavePorRuta = new ConcurrentHashMap<>();
    IpDelNavegadorInterceptor interceptor = new IpDelNavegadorInterceptor(CLAVE, false);

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String ruta = exchange.getRequestURI().getPath();
            var headers = exchange.getRequestHeaders();
            ipPorRuta.put(ruta, String.valueOf(headers.getFirst(IpDelNavegadorInterceptor.HEADER_CLIENT_IP)));
            clavePorRuta.put(ruta, String.valueOf(headers.getFirst(IpDelNavegadorInterceptor.HEADER_FRONT_KEY)));
            String body = switch (ruta) {
                case "/auth/login" -> "{\"accessToken\":\"a\",\"refreshToken\":\"r\",\"expiraEnSegundos\":900,"
                        + "\"rol\":\"ADMIN\",\"permisos\":[],\"referenciaId\":null}";
                case "/auth/logout" -> null;
                case "/publico/registro/donantes-humanos" -> "{\"id\":1,\"rol\":\"DONANTE_HUMANO\",\"email\":\"ana@example.com\"}";
                default -> "[]";
            };
            if (body == null) {
                exchange.sendResponseHeaders(204, -1);
            } else {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
        var navegador = new MockHttpServletRequest("POST", "/login");
        navegador.setRemoteAddr(IP_NAVEGADOR);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(navegador));
    }

    @AfterEach void stop() {
        server.stop(0);
        RequestContextHolder.resetRequestAttributes();
    }

    String url() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    @Test void loginYLogoutAvisanLaIpDelNavegador() {
        var auth = new AutenticacionApiService(url(), interceptor);
        auth.login("ana@example.com", "clave");
        auth.logout("refresh-de-prueba");

        assertThat(ipPorRuta).containsEntry("/auth/login", IP_NAVEGADOR).containsEntry("/auth/logout", IP_NAVEGADOR);
        assertThat(clavePorRuta).containsEntry("/auth/login", CLAVE);
    }

    @Test void elRegistroAvisaLaIpDelNavegador() {
        new RegistroApiService(url(), interceptor)
                .registrarHumano(new RegistroDtos.RegistroHumano(null, "ana@example.com", "clave"));

        assertThat(ipPorRuta).containsEntry("/publico/registro/donantes-humanos", IP_NAVEGADOR);
    }

    @Test void laLandingYElMapaAvisanLaIpDelNavegador() {
        var publico = new PublicoService(url(), interceptor);
        publico.donacionesDestacadas();
        publico.entregasEnMapa();

        assertThat(ipPorRuta).containsEntry("/publico/entregas", IP_NAVEGADOR)
                .containsEntry("/publico/entregas/mapa", IP_NAVEGADOR);
    }
}

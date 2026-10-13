package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.exceptions.SesionExpiradaException;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Servidor HTTP local: comprueba el header Authorization que agrega WebApiCallerService con la sesión actual. */
class WebApiCallerAutorizacionTest {
    HttpServer server;
    AtomicReference<String> authorization = new AtomicReference<>();
    AtomicInteger llamadas = new AtomicInteger();
    AutenticacionApiService auth = mock(AutenticacionApiService.class);
    SesionActual sesionActual = new SesionActual();

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/donaciones", exchange -> {
            llamadas.incrementAndGet();
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
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

    String url() { return "http://127.0.0.1:" + server.getAddress().getPort() + "/donaciones"; }

    @SuppressWarnings("unchecked")
    static <T> ObjectProvider<T> proveedorDe(T bean) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(bean);
        return provider;
    }

    WebApiCallerService conSesiones() {
        return new WebApiCallerService(RestClient.builder(), proveedorDe(sesionActual), proveedorDe(auth));
    }

    /** Simula una request con una sesión que contiene el usuario indicado (o sin sesión si es null). */
    void requestActual(SesionUsuario sesion) {
        var request = new MockHttpServletRequest();
        if (sesion != null) {
            var session = new MockHttpSession();
            session.setAttribute(SesionActual.ATRIBUTO, sesion);
            request.setSession(session);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    static SesionUsuario sesion(long vigenciaSegundos) {
        var token = new AuthDtos.Token("access-viejo", "refresh-viejo", vigenciaSegundos, "ADMIN", List.of(), null);
        return SesionUsuario.desde("admin@example.com", token, "admin@example.com");
    }

    @Test void enviaElBearerDeLaSesionActual() {
        requestActual(sesion(900));
        conSesiones().get(url(), String.class);
        assertThat(authorization.get()).isEqualTo("Bearer access-viejo");
    }

    @Test void sinSesionNoEnviaAuthorization() {
        requestActual(null);
        conSesiones().get(url(), String.class);
        assertThat(authorization.get()).isNull();
    }

    @Test void clienteSinSesionNoAgregaAuthorization() {
        requestActual(sesion(900));
        new WebApiCallerService(RestClient.builder()).get(url(), String.class);
        assertThat(authorization.get()).isNull();
    }

    @Test void refrescaElTokenAntesDeVencerYUsaElNuevo() {
        var sesion = sesion(10);
        requestActual(sesion);
        when(auth.refrescar("refresh-viejo")).thenReturn(
                new AuthDtos.Token("access-nuevo", "refresh-nuevo", 900, "ADMIN", List.of(), null));
        conSesiones().get(url(), String.class);
        verify(auth, times(1)).refrescar("refresh-viejo");
        assertThat(authorization.get()).isEqualTo("Bearer access-nuevo");
        assertThat(sesion.getRefreshToken()).isEqualTo("refresh-nuevo");
    }

    @Test void refreshRechazadoCortaLaLlamadaConSesionExpirada() {
        requestActual(sesion(10));
        when(auth.refrescar(anyString())).thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "Unauthorized", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));
        var http = conSesiones();
        assertThatThrownBy(() -> http.get(url(), String.class)).isInstanceOf(SesionExpiradaException.class);
        assertThat(llamadas.get()).isZero();
    }
}

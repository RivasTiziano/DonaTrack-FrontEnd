package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.WebService;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Respuestas 429 del rate limiting del backend (E6, req. 4): el usuario ve cuánto esperar, no un error genérico. */
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ApiErrorMessages errors;
    @MockBean AutenticacionApiService auth;
    @MockBean WebService web;

    static HttpClientErrorException demasiadas(String retryAfter, String body) {
        var headers = new HttpHeaders();
        if (retryAfter != null) headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
        return HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", headers,
                body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Test void elLoginBloqueadoMuestraCuantoEsperar() throws Exception {
        when(auth.login(anyString(), anyString())).thenThrow(demasiadas("42",
                "{\"status\":429,\"politica\":\"login-email\",\"reintentarEnSegundos\":42}"));

        mvc.perform(post("/login").param("email", "ana@example.com").param("password", "incorrecta"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Esperá 42 segundos")));
    }

    @Test void unaPaginaSinManejoPropioMuestraLaPaginaDe429() throws Exception {
        when(web.landing()).thenThrow(demasiadas("12", "{}"));

        mvc.perform(get("/"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "12"))
                .andExpect(view().name("error/429"))
                .andExpect(content().string(containsString("Esperá 12 segundos")));
    }

    @Test void sinRetryAfterUsaElBodyYSinNingunoUnMinuto() {
        assertThat(errors.segundosParaReintentar(demasiadas(null, "{\"reintentarEnSegundos\":7}"))).isEqualTo(7);
        assertThat(errors.segundosParaReintentar(demasiadas(null, "no es json"))).isEqualTo(60);
        assertThat(errors.describe(demasiadas("1", "{}"))).contains("Esperá 1 segundo ");
    }
}

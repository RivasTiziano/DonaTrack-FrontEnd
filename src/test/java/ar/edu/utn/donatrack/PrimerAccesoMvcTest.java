package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.DonacionesApiService;
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
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Primer acceso (Entrega 1, Registro de personas donantes): enlace del correo, validaciones locales y errores del back. */
@SpringBootTest
@AutoConfigureMockMvc
class PrimerAccesoMvcTest {
    static final String CLAVE = "clave-de-prueba-12345";
    @Autowired MockMvc mvc;
    @MockBean AutenticacionApiService auth;
    @MockBean DonacionesApiService donaciones;

    private static HttpClientErrorException error(HttpStatus estado, String detalle) {
        return HttpClientErrorException.create(estado, estado.getReasonPhrase(), HttpHeaders.EMPTY,
                ("{\"detail\":\"" + detalle + "\"}").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Test void enlaceConTokenMuestraFormularioConElToken() throws Exception {
        mvc.perform(get("/primer-acceso").param("token", "abc"))
                .andExpect(status().isOk()).andExpect(view().name("primer-acceso"))
                .andExpect(content().string(containsString("name=\"token\"")))
                .andExpect(content().string(containsString("value=\"abc\"")))
                .andExpect(content().string(containsString("name=\"contrasenia\"")));
    }

    @Test void enlaceSinTokenNoMuestraFormulario() throws Exception {
        mvc.perform(get("/primer-acceso"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El enlace no es válido")))
                .andExpect(content().string(not(containsString("name=\"contrasenia\""))));
    }

    @Test void contraseniaCortaSeRechazaSinLlamarAlServicio() throws Exception {
        mvc.perform(post("/primer-acceso").param("token", "abc")
                        .param("contrasenia", "corta-xyz").param("confirmacion", "corta-xyz"))
                .andExpect(status().isOk()).andExpect(view().name("primer-acceso"))
                .andExpect(model().attribute("error", "La contraseña debe tener al menos 12 caracteres."))
                .andExpect(content().string(not(containsString("corta-xyz"))));
        verify(auth, never()).definirContrasenia(any(), any());
    }

    @Test void confirmacionDistintaSeRechazaSinLlamarAlServicio() throws Exception {
        mvc.perform(post("/primer-acceso").param("token", "abc")
                        .param("contrasenia", CLAVE).param("confirmacion", CLAVE + "x"))
                .andExpect(status().isOk()).andExpect(view().name("primer-acceso"))
                .andExpect(model().attribute("error", "Las contraseñas no coinciden."))
                .andExpect(content().string(not(containsString(CLAVE))));
        verify(auth, never()).definirContrasenia(any(), any());
    }

    @Test void primerAccesoOkRedirigeAlLoginConAviso() throws Exception {
        mvc.perform(post("/primer-acceso").param("token", "abc")
                        .param("contrasenia", CLAVE).param("confirmacion", CLAVE))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?primerAcceso"));
        verify(auth).definirContrasenia("abc", CLAVE);
    }

    @Test void enlaceInvalidoDelBackMuestraSuDetalleSinLaClave() throws Exception {
        doThrow(error(HttpStatus.BAD_REQUEST, "El enlace no es válido o venció"))
                .when(auth).definirContrasenia(any(), any());
        mvc.perform(post("/primer-acceso").param("token", "abc")
                        .param("contrasenia", CLAVE).param("confirmacion", CLAVE))
                .andExpect(status().isOk()).andExpect(view().name("primer-acceso"))
                .andExpect(content().string(containsString("El enlace no es válido o venció")))
                .andExpect(content().string(not(containsString(CLAVE))));
    }

    @Test void cuentaConContraseniaYaDefinidaMuestraSuDetalleSinLaClave() throws Exception {
        doThrow(error(HttpStatus.CONFLICT, "La cuenta ya tiene contraseña"))
                .when(auth).definirContrasenia(any(), any());
        mvc.perform(post("/primer-acceso").param("token", "abc")
                        .param("contrasenia", CLAVE).param("confirmacion", CLAVE))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La cuenta ya tiene contraseña")))
                .andExpect(content().string(not(containsString(CLAVE))));
    }

    @Test void loginConAvisoDePrimerAccesoMuestraConfirmacion() throws Exception {
        mvc.perform(get("/login").param("primerAcceso", ""))
                .andExpect(status().isOk()).andExpect(view().name("login"))
                .andExpect(content().string(containsString("Contraseña guardada")));
    }

    @Test void tokenYContraseniaNoAparecenEnElToString() {
        assertThat(new AuthDtos.PrimerAcceso("tok-secreto", CLAVE).toString())
                .doesNotContain("tok-secreto").doesNotContain(CLAVE);
    }
}

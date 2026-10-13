package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.models.SesionUsuario;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacionMvcTest {
    static final String CLAVE = "clave-de-prueba-12345";
    @Autowired MockMvc mvc;
    @MockBean AutenticacionApiService auth;
    @MockBean DonacionesApiService donaciones;

    private static AuthDtos.Token token(String rol, Long referenciaId) {
        return new AuthDtos.Token("access-de-prueba", "refresh-de-prueba", 900, rol, List.of(), referenciaId);
    }

    private static SesionUsuario sesionGuardada(org.springframework.test.web.servlet.MvcResult result) {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return (SesionUsuario) session.getAttribute(SesionActual.ATRIBUTO);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(String email, String password) {
        return post("/login").param("email", email).param("password", password);
    }

    @Test void adminEntraAlPanelConSesionDeRolAdmin() throws Exception {
        when(auth.login("admin@example.com", CLAVE)).thenReturn(token("ADMIN", null));
        var result = mvc.perform(login("admin@example.com", CLAVE))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/dashboard"))
                .andReturn();
        var sesion = sesionGuardada(result);
        assertThat(sesion.esAdmin()).isTrue();
        assertThat(sesion.getNombreVisible()).isEqualTo("Administración");
        assertThat(sesion.getAccessToken()).isEqualTo("access-de-prueba");
    }

    @Test void donanteHumanoEntraAlPanelConSuNombre() throws Exception {
        when(auth.login("ana@example.com", CLAVE)).thenReturn(token("DONANTE_HUMANO", 1L));
        when(donaciones.humano(1L)).thenReturn(new DonacionesDetalles.Humano(1L, "Ana", "Real", null,
                "DNI", "12345678", "OTRO", null, List.of(), null, "COMPLETO"));
        var result = mvc.perform(login("ana@example.com", CLAVE))
                .andExpect(redirectedUrl("/donante/dashboard")).andReturn();
        var sesion = sesionGuardada(result);
        assertThat(sesion.esDonante()).isTrue();
        assertThat(sesion.getNombreVisible()).isEqualTo("Ana Real");
        assertThat(sesion.getReferenciaId()).isEqualTo(1L);
    }

    @Test void donanteJuridicoEntraAlPanelConSuRazonSocial() throws Exception {
        when(auth.login("empresa@example.com", CLAVE)).thenReturn(token("DONANTE_JURIDICO", 2L));
        when(donaciones.juridico(2L)).thenReturn(new DonacionesDetalles.Juridico(2L, "30123456789", "Empresa Real",
                "EMPRESA", "Alimentos", null, List.of(), null, List.of(), "COMPLETO"));
        var result = mvc.perform(login("empresa@example.com", CLAVE))
                .andExpect(redirectedUrl("/donante/dashboard")).andReturn();
        assertThat(sesionGuardada(result).getNombreVisible()).isEqualTo("Empresa Real");
    }

    @Test void entidadEntraAlPanelConLaRazonSocialDeLaEntidad() throws Exception {
        when(auth.login("entidad@example.com", CLAVE)).thenReturn(token("BENEFICIARIA", 3L));
        when(donaciones.beneficiario(3L)).thenReturn(new DonacionesDetalles.Beneficiario(3L, "Entidad Real", "ONG",
                "Comedor", null, null, List.of(), true));
        var result = mvc.perform(login("entidad@example.com", CLAVE))
                .andExpect(redirectedUrl("/entidad/dashboard")).andReturn();
        var sesion = sesionGuardada(result);
        assertThat(sesion.esBeneficiaria()).isTrue();
        assertThat(sesion.getNombreVisible()).isEqualTo("Entidad Real");
    }

    @Test void siNoSePuedeLeerElPerfilQuedaElEmail() throws Exception {
        when(auth.login("ana@example.com", CLAVE)).thenReturn(token("DONANTE_HUMANO", 1L));
        when(donaciones.humano(1L)).thenThrow(new ResourceAccessException("offline"));
        var result = mvc.perform(login("ana@example.com", CLAVE)).andExpect(redirectedUrl("/donante/dashboard")).andReturn();
        assertThat(sesionGuardada(result).getNombreVisible()).isEqualTo("Donante");
    }

    @Test void credencialesIncorrectasMuestranErrorYConservanElEmailSinLaClave() throws Exception {
        when(auth.login(anyString(), anyString())).thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "Unauthorized", HttpHeaders.EMPTY, "{\"detail\":\"Credenciales inválidas\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));
        mvc.perform(login("ana@example.com", CLAVE))
                .andExpect(status().isOk()).andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Email o contraseña incorrectos."))
                .andExpect(model().attribute("email", "ana@example.com"))
                .andExpect(model().attributeDoesNotExist("password"))
                .andExpect(content().string(not(containsString(CLAVE))));
    }

    @Test void servicioCaidoMuestraMensajeDeConexion() throws Exception {
        when(auth.login(anyString(), anyString())).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(login("ana@example.com", CLAVE))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "No se pudo conectar con el servicio de autenticación."));
    }

    @Test void camposVaciosNoLlamanAlServicio() throws Exception {
        mvc.perform(login("", CLAVE)).andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Ingresá tu email y contraseña."));
        mvc.perform(login("ana@example.com", "")).andExpect(view().name("login"));
        verifyNoInteractions(auth);
    }

    @Test void sinSesionUnAreaPersonalRedirigeAlLogin() throws Exception {
        mvc.perform(get("/admin/dashboard")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/donante/dashboard")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test void donanteNoPuedeEntrarAlAreaDeAdmin() throws Exception {
        mvc.perform(get("/admin/dashboard").session(TestSesiones.donante(1L))).andExpect(status().isForbidden());
    }

    @Test void entidadNoPuedeEntrarAlAreaDeDonante() throws Exception {
        mvc.perform(get("/donante/dashboard").session(TestSesiones.entidad(3L))).andExpect(status().isForbidden());
    }

    @Test void adminNoPuedeEntrarAlAreaDeEntidad() throws Exception {
        mvc.perform(get("/entidad/dashboard").session(TestSesiones.admin())).andExpect(status().isForbidden());
    }

    @Test void loginConSesionIniciadaVaAlPanelDelRol() throws Exception {
        mvc.perform(get("/login").session(TestSesiones.donante(1L))).andExpect(redirectedUrl("/donante/dashboard"));
    }

    @Test void logoutInvalidaLaSesion() throws Exception {
        var session = TestSesiones.admin();
        mvc.perform(post("/logout").session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        assertThat(session.isInvalid()).isTrue();
    }
}

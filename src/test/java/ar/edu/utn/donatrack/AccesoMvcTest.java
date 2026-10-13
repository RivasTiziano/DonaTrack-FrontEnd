package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.AuthDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Crear acceso (fase 8.1): rol deducido del tipo, email del perfil, validación sin llamar a la API y manejo de 409 y 404. */
@SpringBootTest
@AutoConfigureMockMvc
class AccesoMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean AutenticacionApiService auth;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;
    @MockBean NotificacionesApiService notificaciones;

    private static final String PASS = "clave-inicial-segura-2026";

    private DonacionesDetalles.Humano humano(String email) {
        return new DonacionesDetalles.Humano(7L, "Ana", "Prueba", null, "DNI", "12345678", "F", null,
                List.of(), email == null ? null : new MedioDeContactoDto("EMAIL", email), "APROBADO");
    }

    private DonacionesDetalles.Juridico juridico(String email) {
        return new DonacionesDetalles.Juridico(9L, "30111222333", "Arcos Plateados S.A.", "EMPRESA", "Alimentos", null,
                List.of(), email == null ? null : new MedioDeContactoDto("EMAIL", email), List.of(), "APROBADO");
    }

    private DonacionesDetalles.Beneficiario entidad(String email) {
        return new DonacionesDetalles.Beneficiario(3L, "Comedor Sol", "ONG", "Comedor", null,
                email == null ? null : new MedioDeContactoDto("EMAIL", email), List.of(), true);
    }

    @Test void creaElAccesoDelDonanteHumanoConSuRolYEmailDelPerfil() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS)
                        .param("email", "otro@example.com").param("rol", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard/donantes"));
        verify(auth).crearUsuario(new AuthDtos.CrearUsuario("ana@example.com", PASS, "DONANTE_HUMANO", 7L));
    }

    @Test void creaElAccesoDeUnDonanteJuridicoConSuRol() throws Exception {
        when(donaciones.juridico(9L)).thenReturn(juridico("arcos@example.com"));
        mvc.perform(post("/admin/dashboard/donantes/juridicos/9/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS))
                .andExpect(status().is3xxRedirection());
        verify(auth).crearUsuario(new AuthDtos.CrearUsuario("arcos@example.com", PASS, "DONANTE_JURIDICO", 9L));
    }

    @Test void creaElAccesoDeLaEntidadConRolBeneficiaria() throws Exception {
        when(donaciones.beneficiario(3L)).thenReturn(entidad("sol@example.org"));
        mvc.perform(post("/admin/dashboard/beneficiarios/3/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard/beneficiarios"));
        verify(auth).crearUsuario(new AuthDtos.CrearUsuario("sol@example.org", PASS, "BENEFICIARIA", 3L));
    }

    @Test void conflictoDeEmailMuestraMensajeYNoGuardaLaContrasena() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        doThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], null))
                .when(auth).crearUsuario(any());
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe un usuario con ese email.")))
                .andExpect(content().string(not(containsString(PASS))));
    }

    @Test void perfilInexistenteResponde404() throws Exception {
        when(donaciones.humano(99L)).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));
        when(donaciones.beneficiario(98L)).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));
        mvc.perform(get("/admin/dashboard/donantes/humanos/99/acceso").session(TestSesiones.admin()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/admin/dashboard/beneficiarios/98/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS))
                .andExpect(status().isNotFound());
        verify(auth, never()).crearUsuario(any());
    }

    @Test void perfilSinEmailBloqueaLaCreacion() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano(null));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El perfil no tiene un email de contacto; cargalo antes de crear el acceso.")));
        verify(auth, never()).crearUsuario(any());
    }

    @Test void contrasenaCortaDaErrorSinLlamarALaApi() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin())
                        .param("password", "corta").param("confirmacion", "corta"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La contraseña debe tener al menos 12 caracteres.")));
        verify(auth, never()).crearUsuario(any());
    }

    @Test void confirmacionDistintaDaErrorSinLlamarALaApi() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin())
                        .param("password", PASS).param("confirmacion", PASS + "x"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Las contraseñas no coinciden.")));
        verify(auth, never()).crearUsuario(any());
    }

    @Test void formularioMuestraEmailDelPerfilDeSoloLecturaYSinContrasena() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        mvc.perform(get("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"ana@example.com\" readonly")))
                .andExpect(content().string(containsString("name=\"password\"")))
                .andExpect(content().string(not(containsString("name=\"email\""))));
    }
}

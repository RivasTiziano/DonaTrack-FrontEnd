package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.AccesoDtos;
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

/** Enviar acceso por mail: el back envía el mail, el front muestra el estado y valida el email del perfil. */
@SpringBootTest
@AutoConfigureMockMvc
class AccesoMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean AutenticacionApiService auth;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;
    @MockBean NotificacionesApiService notificaciones;

    private static final String ENVIADO = "Se envió el mail de bienvenida para que elija su contraseña";

    private DonacionesDetalles.Humano humano(String email) {
        return new DonacionesDetalles.Humano(7L, "Ana", "Prueba", null, "DNI", "12345678", "F", null,
                List.of(), email == null ? null : new MedioDeContactoDto("EMAIL", email), "PENDIENTE_COMPLETAR");
    }

    private DonacionesDetalles.Juridico juridico(String email) {
        return new DonacionesDetalles.Juridico(9L, "30111222333", "Arcos Plateados S.A.", "EMPRESA", "Alimentos", null,
                List.of(), email == null ? null : new MedioDeContactoDto("EMAIL", email), List.of(), "PENDIENTE_COMPLETAR");
    }

    private DonacionesDetalles.Beneficiario entidad(String email) {
        return new DonacionesDetalles.Beneficiario(3L, "Comedor Sol", "ONG", "Comedor", null,
                email == null ? null : new MedioDeContactoDto("EMAIL", email), List.of(), true);
    }

    private DonacionesDetalles.Humano completo(String email) {
        var h = humano(email);
        return new DonacionesDetalles.Humano(h.id(), h.nombre(), h.apellido(), h.fechaNacimiento(), h.tipoDocumento(), h.numeroDocumento(),
                h.genero(), h.direccion(), h.mediosDeContacto(), h.medioDeContactoPredeterminado(), "COMPLETO");
    }

    @Test void listadoOfreceEnviarAccesoEnTodasLasFilasSinConsultarCuentas() throws Exception {
        when(donaciones.listarHumanos()).thenReturn(List.of(
            new ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse(7L,"Ana","Con cuenta","DNI","70000007",List.of(),"PENDIENTE_COMPLETAR"),
            new ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse(8L,"Ana","Sin email","DNI","70000008",List.of(),"PENDIENTE_COMPLETAR"),
            new ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse(9L,"Ana","Completa","DNI","70000009",List.of(),"COMPLETO")));
        when(donaciones.listarJuridicos()).thenReturn(List.of());
        mvc.perform(get("/admin/dashboard/donantes").session(TestSesiones.admin()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Enviar acceso")))
            .andExpect(content().string(containsString("href=\"/admin/dashboard/donantes/humanos/7/acceso\"")))
            .andExpect(content().string(containsString("href=\"/admin/dashboard/donantes/humanos/8/acceso\"")))
            .andExpect(content().string(containsString("href=\"/admin/dashboard/donantes/humanos/9/acceso\"")));
        verifyNoInteractions(auth);
        verify(donaciones, never()).humano(any());
        verify(donaciones, never()).juridico(any());
    }

    @Test void sinEmailRedirigeACompletarContacto() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano(null));
        mvc.perform(get("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin()))
            .andExpect(redirectedUrl("/admin/dashboard/donantes/humanos/7?completarContacto=true"));
    }

    @Test void completarContactoMuestraCampoEmailSinCompletarElPerfil() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano(null));
        mvc.perform(get("/admin/dashboard/donantes/humanos/7").param("completarContacto","true").session(TestSesiones.admin()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"email\"")))
            .andExpect(content().string(containsString("/admin/dashboard/donantes/humanos/7/acceso/contacto")))
            .andExpect(content().string(not(containsString("name=\"fechaNacimiento\""))));
    }

    @Test void completarEmailRedirigeAlAccesoYNoEnviaNiModificaElPerfil() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano(null));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso/contacto").session(TestSesiones.admin())
            .param("email"," Prueba@EXAMPLE.COM "))
            .andExpect(redirectedUrl("/admin/dashboard/donantes/humanos/7/acceso"));
        verify(donaciones).completarEmail("humanos",7L,"prueba@example.com");
        verify(donaciones,never()).actualizarHumano(any(),any());
        verify(donaciones,never()).enviarAcceso(any(),any());
        verifyNoInteractions(auth);
    }

    @Test void emailInvalidoSeConservaSinEscribir() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano(null));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso/contacto").session(TestSesiones.admin())
            .param("email","sin-arroba"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Ingresá un email válido.")))
            .andExpect(content().string(containsString("value=\"sin-arroba\"")));
        verify(donaciones,never()).completarEmail(any(),any(),any());
    }

    @Test void contactoNoSeModificaSiYaHayEmail() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("original@example.com"));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso/contacto").session(TestSesiones.admin())
            .param("email","nuevo@example.com")).andExpect(status().isOk());
        verify(donaciones,never()).completarEmail(any(),any(),any());
    }

    @Test void formularioMuestraEmailDeSoloLecturaYBotonEnviar() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        mvc.perform(get("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enviar acceso")))
                .andExpect(content().string(containsString("value=\"ana@example.com\" readonly")))
                .andExpect(content().string(not(containsString("type=\"password\""))))
                .andExpect(content().string(not(containsString("name=\"email\""))));
    }

    @Test void enviaElAccesoDelHumanoALaUrlDelTipo() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        when(donaciones.enviarAcceso("humanos", 7L)).thenReturn(new AccesoDtos.Respuesta("ENVIADO", ENVIADO));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin()))
                .andExpect(redirectedUrl("/admin/dashboard/donantes"))
                .andExpect(flash().attribute("success", containsString("Se envió el mail")));
        verifyNoInteractions(auth);
    }

    @Test void enviaElAccesoDeUnDonanteJuridicoALaUrlDelTipo() throws Exception {
        when(donaciones.juridico(9L)).thenReturn(juridico("arcos@example.com"));
        when(donaciones.enviarAcceso("juridicos", 9L)).thenReturn(new AccesoDtos.Respuesta("ENVIADO", ENVIADO));
        mvc.perform(post("/admin/dashboard/donantes/juridicos/9/acceso").session(TestSesiones.admin()))
                .andExpect(redirectedUrl("/admin/dashboard/donantes"))
                .andExpect(flash().attribute("success", containsString("Se envió el mail")));
        verifyNoInteractions(auth);
    }

    @Test void enviaElAccesoDeLaEntidadALaUrlDelTipo() throws Exception {
        when(donaciones.beneficiario(3L)).thenReturn(entidad("sol@example.org"));
        when(donaciones.enviarAcceso("beneficiarios", 3L)).thenReturn(new AccesoDtos.Respuesta("ENVIADO", ENVIADO));
        mvc.perform(post("/admin/dashboard/beneficiarios/3/acceso").session(TestSesiones.admin()))
                .andExpect(redirectedUrl("/admin/dashboard/beneficiarios"))
                .andExpect(flash().attribute("success", containsString("Se envió el mail")));
        verifyNoInteractions(auth);
    }

    private void estadoNoEnviado(String estado, String mensaje) throws Exception {
        when(donaciones.enviarAcceso("humanos", 7L)).thenReturn(new AccesoDtos.Respuesta(estado, mensaje));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-acceso"))
                .andExpect(content().string(containsString(mensaje)));
    }

    @Test void estadosNoEnviadoMuestranElMensajeEnLaVista() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano("ana@example.com"));
        estadoNoEnviado("YA_TIENE_CUENTA", "La persona ya eligió su contraseña y puede ingresar");
        estadoNoEnviado("EMAIL_DE_OTRA_CUENTA", "El email del perfil ya es de la cuenta de otro perfil");
        estadoNoEnviado("ERROR", "No se pudo enviar el acceso en este momento. Intentá de nuevo más tarde");
    }

    @Test void perfilInexistenteResponde404() throws Exception {
        when(donaciones.humano(99L)).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));
        when(donaciones.beneficiario(98L)).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));
        mvc.perform(get("/admin/dashboard/donantes/humanos/99/acceso").session(TestSesiones.admin()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/admin/dashboard/beneficiarios/98/acceso").session(TestSesiones.admin()))
                .andExpect(status().isNotFound());
        verify(donaciones, never()).enviarAcceso(any(), any());
    }

    @Test void perfilSinEmailRedirigeACompletarContactoYNoEnvia() throws Exception {
        when(donaciones.humano(7L)).thenReturn(humano(null));
        mvc.perform(post("/admin/dashboard/donantes/humanos/7/acceso").session(TestSesiones.admin()))
                .andExpect(redirectedUrl("/admin/dashboard/donantes/humanos/7?completarContacto=true"));
        verify(donaciones, never()).enviarAcceso(any(), any());
    }
}

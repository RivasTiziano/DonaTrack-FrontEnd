package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.notificaciones.envio-manual-habilitado=true")
@AutoConfigureMockMvc
class NotificacionesMvcTest {
    @Autowired MockMvc mvc;
    @MockBean NotificacionesApiService api;

    MockHttpSession session() throws Exception {
        return (MockHttpSession) mvc.perform(get("/admin/dashboard/notificaciones"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
    }
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder send(MockHttpSession session) {
        return post("/admin/dashboard/notificaciones/enviar").session(session)
                .param("sendToken", (String) session.getAttribute("notificacionesEnvioToken"))
                .param("medioContacto", "EMAIL").param("nombre", "Persona de prueba")
                .param("email", "prueba@example.com").param("asunto", "Aviso").param("cuerpo", "Mensaje de prueba");
    }
    @Test void personalPagesDoNotInventIdentityOrQueryHistoryOrSend() throws Exception {
        for (String route : java.util.List.of("donante", "entidad"))
            mvc.perform(get("/" + route + "/dashboard/notificaciones"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Historial todavía no disponible")))
                    .andExpect(content().string(not(containsString("Solicitar envío real"))));
        verifyNoInteractions(api);
    }
    @Test void pendingIsAcceptanceNotDeliveryAndPostRedirectGetDoesNotRepeat() throws Exception {
        when(api.enviar(any())).thenReturn(new Respuesta("success", Estado.PENDIENTE, null));
        var session = session();
        var result = mvc.perform(send(session)).andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("sendResult", containsString("no es confirmación de entrega"))).andReturn();
        verify(api).enviar(new Enviar(new Destinatario("Persona de prueba", "prueba@example.com", null),
                new Mensaje("Aviso", "Mensaje de prueba"), Medio.EMAIL));
        mvc.perform(get("/admin/dashboard/notificaciones").session(session).flashAttrs(result.getFlashMap()))
                .andExpect(status().isOk());
        verify(api, times(1)).enviar(any());
    }
    @Test void invalidEmailKeepsValuesAndDoesNotSend() throws Exception {
        var session = session();
        mvc.perform(send(session).with(request -> { request.setParameter("email", "incorrecto"); return request; }))
                .andExpect(status().isOk()).andExpect(content().string(containsString("email válido")))
                .andExpect(content().string(containsString("Persona de prueba")));
        verify(api, never()).enviar(any());
    }
    @Test void smsAndWhatsappRequirePhoneAndUseTheirOwnChannel() throws Exception {
        when(api.enviar(any())).thenReturn(new Respuesta("success", Estado.PENDIENTE, null));
        for (String medio : java.util.List.of("SMS", "WHATSAPP")) {
            var session = session();
            mvc.perform(send(session).with(request -> { request.setParameter("medioContacto", medio); return request; }).param("telefono", "+5491112345678"))
                    .andExpect(status().is3xxRedirection());
            verify(api).enviar(new Enviar(new Destinatario("Persona de prueba", null, "+5491112345678"),
                    new Mensaje("Aviso", "Mensaje de prueba"), Medio.valueOf(medio)));
        }
    }
    @Test void incorrectTokenIsRejectedWithoutSending() throws Exception {
        mvc.perform(send(session()).with(request -> { request.setParameter("sendToken", "incorrecto"); return request; }))
                .andExpect(status().isConflict());
        verify(api, never()).enviar(any());
    }
    @Test void sameSubmissionIsNeverSentTwice() throws Exception {
        when(api.enviar(any())).thenReturn(new Respuesta("success", Estado.PENDIENTE, null));
        var session = session();
        String token = (String) session.getAttribute("notificacionesEnvioToken");
        mvc.perform(send(session)).andExpect(status().is3xxRedirection());
        // El GET renueva el formulario, pero el token anterior sigue siendo inválido.
        mvc.perform(get("/admin/dashboard/notificaciones").session(session));
        mvc.perform(send(session).with(request -> { request.setParameter("sendToken", token); return request; })).andExpect(status().isConflict());
        verify(api, times(1)).enviar(any());
    }
    @Test void http200WithFailureIsNotSuccessAndEscapesMessage() throws Exception {
        when(api.enviar(any())).thenReturn(new Respuesta("error", Estado.FALLIDA, "<script>error</script>"));
        mvc.perform(send(session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;error&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("Solicitud aceptada"))));
    }
    @Test void offlineDoesNotReportAcceptanceOrAutomaticallyRetry() throws Exception {
        when(api.enviar(any())).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(send(session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("verificá el resultado antes de reintentar")));
        verify(api, times(1)).enviar(any());
    }
    @Test void healthCheckExplicitlyDoesNotCertifyDeliveryProviders() throws Exception {
        when(api.disponible()).thenReturn(true);
        mvc.perform(post("/admin/dashboard/notificaciones/comprobar"))
                .andExpect(flash().attribute("serviceStatus", containsString("no verifica Gmail")));
    }
}

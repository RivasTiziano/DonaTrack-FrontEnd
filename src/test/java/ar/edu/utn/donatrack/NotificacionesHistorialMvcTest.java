package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Historial de la persona de la sesión: el servicio la identifica por el token, no por email ni teléfono. */
@SpringBootTest
@AutoConfigureMockMvc
class NotificacionesHistorialMvcTest {
    @Autowired MockMvc mvc;
    @MockBean NotificacionesApiService api;

    Recibida recibida(long id, boolean leida) {
        return new Recibida(id, "Tu donacion fue asignada", "<b>Detalle</b>", Medio.EMAIL,
                LocalDateTime.of(2026, 10, 11, 18, 30), leida, null, null);
    }

    @Test void sinSesionNoSeConsultaElHistorial() throws Exception {
        mvc.perform(get("/donante/dashboard/notificaciones")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(api, never()).listar(anyBoolean(), anyInt());
    }
    @Test void mostrarLaListaDelDonanteConContadorYEscapaContenido() throws Exception {
        when(api.listar(false, 50)).thenReturn(List.of(recibida(1, false), recibida(2, true)));
        mvc.perform(get("/donante/dashboard/notificaciones").session(TestSesiones.donante(1L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("11/10/2026 18:30")))
                .andExpect(content().string(containsString("&lt;b&gt;Detalle&lt;/b&gt;")))
                .andExpect(content().string(containsString("/donante/dashboard/notificaciones/1/leida")))
                .andExpect(content().string(not(containsString("/donante/dashboard/notificaciones/2/leida"))))
                .andExpect(content().string(containsString("Marcar todas como leídas")))
                .andExpect(content().string(not(containsString("name=\"email\""))))
                .andExpect(content().string(not(containsString("name=\"telefono\""))));
    }
    @Test void entidadVeSuPropioHistorial() throws Exception {
        when(api.listar(false, 50)).thenReturn(List.of(recibida(3, false)));
        mvc.perform(get("/entidad/dashboard/notificaciones").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/entidad/dashboard/notificaciones/3/leida")));
    }
    @Test void historialVacioSeExplicaComoVacio() throws Exception {
        when(api.listar(false, 50)).thenReturn(List.of());
        mvc.perform(get("/entidad/dashboard/notificaciones").session(TestSesiones.entidad(3L)))
                .andExpect(content().string(containsString("No tenés notificaciones.")));
    }
    @Test void falloDelServicioSeMuestraComoErrorYNoComoListaVacia() throws Exception {
        when(api.listar(false, 50)).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/entidad/dashboard/notificaciones").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No se pudo comunicar con el servicio")))
                .andExpect(content().string(not(containsString("No tenés notificaciones."))));
    }
    @Test void elContactoDeLaUrlNoCambiaLaConsulta() throws Exception {
        when(api.listar(false, 50)).thenReturn(List.of());
        mvc.perform(get("/donante/dashboard/notificaciones").session(TestSesiones.donante(1L))
                        .param("email", "otra@mail.com").param("telefono", "+5491112345678"))
                .andExpect(status().isOk());
        verify(api).listar(false, 50);
    }
    @Test void marcarUnaLeidaLlamaAlServicioYRedirigeSinDatosDeContacto() throws Exception {
        mvc.perform(post("/donante/dashboard/notificaciones/5/leida").session(TestSesiones.donante(1L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donante/dashboard/notificaciones"));
        verify(api).marcarLeida(5L);
    }
    @Test void marcarTodasLasLeidasDeLaEntidad() throws Exception {
        mvc.perform(post("/entidad/dashboard/notificaciones/leidas").session(TestSesiones.entidad(3L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entidad/dashboard/notificaciones"));
        verify(api).marcarTodasLeidas();
    }
    @Test void falloAlMarcarMuestraElErrorEnElFlash() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceAccessException("offline")).when(api).marcarLeida(anyLong());
        mvc.perform(post("/donante/dashboard/notificaciones/5/leida").session(TestSesiones.donante(1L)))
                .andExpect(flash().attribute("apiError", containsString("No se pudo comunicar")));
    }
}

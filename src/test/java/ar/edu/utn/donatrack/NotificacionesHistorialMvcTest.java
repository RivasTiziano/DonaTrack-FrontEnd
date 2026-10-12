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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Historial por contacto activado (herramienta de desarrollo local; por defecto está apagado). */
@SpringBootTest(properties = "app.notificaciones.historial-por-contacto-habilitado=true")
@AutoConfigureMockMvc
class NotificacionesHistorialMvcTest {
    @Autowired MockMvc mvc;
    @MockBean NotificacionesApiService api;

    Recibida recibida(long id, boolean leida) {
        return new Recibida(id, "Tu donacion fue asignada", "<b>Detalle</b>", Medio.EMAIL,
                LocalDateTime.of(2026, 10, 11, 18, 30), leida, null, null);
    }

    @Test void withoutContactItAsksForOneAndDoesNotQueryTheApi() throws Exception {
        for (String route : List.of("donante", "entidad"))
            mvc.perform(get("/" + route + "/dashboard/notificaciones"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Ver notificaciones")))
                    .andExpect(content().string(not(containsString("Historial todavía no disponible"))));
        verifyNoInteractions(api);
    }
    @Test void showsHistoryWithUnreadCountAndEscapesContent() throws Exception {
        when(api.listar("ana@mail.com", null, false)).thenReturn(List.of(recibida(1, false), recibida(2, true)));
        mvc.perform(get("/donante/dashboard/notificaciones").param("email", "ana@mail.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("11/10/2026 18:30")))
                .andExpect(content().string(containsString("&lt;b&gt;Detalle&lt;/b&gt;")))
                .andExpect(content().string(containsString("/donante/dashboard/notificaciones/1/leida")))
                .andExpect(content().string(not(containsString("/donante/dashboard/notificaciones/2/leida"))))
                .andExpect(content().string(containsString("Marcar todas como leídas")));
    }
    @Test void emptyHistoryIsExplainedAndApiFailureIsNotShownAsEmpty() throws Exception {
        when(api.listar("ana@mail.com", null, false)).thenReturn(List.of());
        mvc.perform(get("/entidad/dashboard/notificaciones").param("email", "ana@mail.com"))
                .andExpect(content().string(containsString("No hay notificaciones para ese contacto")));
        when(api.listar("caida@mail.com", null, false)).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/entidad/dashboard/notificaciones").param("email", "caida@mail.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No se pudo comunicar con el servicio")))
                .andExpect(content().string(not(containsString("No hay notificaciones para ese contacto"))));
    }
    @Test void markingAsReadCallsTheApiAndKeepsTheContactInTheRedirect() throws Exception {
        mvc.perform(post("/donante/dashboard/notificaciones/5/leida").param("email", "ana@mail.com").param("telefono", "+5491112345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donante/dashboard/notificaciones?email=ana%40mail.com&telefono=%2B5491112345678"));
        verify(api).marcarLeida(5L, "ana@mail.com", "+5491112345678");
        mvc.perform(post("/entidad/dashboard/notificaciones/leidas").param("telefono", "+5491112345678"))
                .andExpect(status().is3xxRedirection());
        verify(api).marcarTodasLeidas(null, "+5491112345678");
    }
    @Test void markingWithoutContactDoesNotCallTheApi() throws Exception {
        mvc.perform(post("/donante/dashboard/notificaciones/5/leida"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("apiError", containsString("email o un teléfono")));
        verify(api, never()).marcarLeida(any(), any(), any());
    }
}

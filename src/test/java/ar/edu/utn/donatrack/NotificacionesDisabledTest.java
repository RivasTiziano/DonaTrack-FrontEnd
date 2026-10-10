package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.services.NotificacionesApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.notificaciones.envio-manual-habilitado=false")
@AutoConfigureMockMvc
class NotificacionesDisabledTest {
    @Autowired MockMvc mvc;
    @MockBean NotificacionesApiService api;
    @Test void disabledByDefaultAndDirectPostCannotBypassIt() throws Exception {
        mvc.perform(get("/admin/dashboard/notificaciones")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Deshabilitado por seguridad")))
                .andExpect(content().string(not(containsString("Solicitar envío real"))));
        mvc.perform(post("/admin/dashboard/notificaciones/enviar").param("sendToken", "x"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(api);
    }
}

package ar.edu.utn.donatrack;

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
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Páginas públicas sin datos falsos ni llamadas a la API (fase 6) y registro público con validación en el front (sin llamadas a la API con datos incompletos). */
@SpringBootTest
@AutoConfigureMockMvc
class PublicoYRegistroMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean ar.edu.utn.donatrack.services.PublicoService publico;
    @MockBean AutenticacionApiService autenticacion;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;
    @MockBean NotificacionesApiService notificaciones;
    @MockBean ar.edu.utn.donatrack.services.RegistroApiService registroApi;

    private void sinLlamadasALaApi() {
        verifyNoInteractions(donaciones, autenticacion, incentivos, logistica, notificaciones);
    }

    // Solo el hero es decorativo. Galería y destacadas esperan datos públicos reales.
    @Test void landingNoLlamaALaApiYNoInventaEntregas() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Entrega de útiles escolares"))))
                .andExpect(content().string(containsString("/img/landing-donaciones.webp")))
                .andExpect(content().string(not(containsString("100% trazabilidad verificada"))))
                .andExpect(content().string(containsString("La transparencia empieza con datos reales")));
        sinLlamadasALaApi();
    }

    @Test void mapaPublicoRetiradoNoLlamaALaApi() throws Exception {
        mvc.perform(get("/explorar-donaciones")).andExpect(status().isNotFound());
        sinLlamadasALaApi();
    }

    @Test void detallePublicoRetiradoNoLlamaALaApi() throws Exception {
        mvc.perform(get("/explorar-donaciones/15")).andExpect(status().isNotFound());
        sinLlamadasALaApi();
    }

    // Con datos incompletos el front muestra los errores junto a cada campo y no llama a la API (fase R: registro propio).
    @Test void registroDonanteHumanoConDatosIncompletosNoLlamaALaApi() throws Exception {
        mvc.perform(post("/registro/donante-humano")
                        .param("nombre", "Ana")
                        .param("apellido", "Prueba")
                        .param("email", "ana@example.com"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("Ingresá tu DNI.")))
                .andExpect(content().string(containsString("value=\"Ana\"")))
                .andExpect(content().string(containsString("value=\"ana@example.com\"")))
                .andExpect(content().string(not(containsString("no está habilitado"))));
        verifyNoInteractions(registroApi);
        sinLlamadasALaApi();
    }

    @Test void registroEntidadConDatosIncompletosNoLlamaALaApi() throws Exception {
        mvc.perform(post("/registro/beneficiario").param("razonSocial", "Comedor Sol").param("tipoEntidad", "ONG"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("value=\"Comedor Sol\"")));
        verifyNoInteractions(registroApi);
        sinLlamadasALaApi();
    }

    @Test void registroDonanteOrganizacionConDatosIncompletosNoLlamaALaApi() throws Exception {
        mvc.perform(post("/registro/donante-organizacion").param("razonSocial", "Arcos S.A."))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("value=\"Arcos S.A.\"")));
        verifyNoInteractions(registroApi);
        sinLlamadasALaApi();
    }
}

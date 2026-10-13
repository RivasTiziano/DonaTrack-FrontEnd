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

/** Páginas públicas sin datos falsos ni llamadas a la API (fase 6) y registro público sin alta de cuentas (fase 7). */
@SpringBootTest
@AutoConfigureMockMvc
class PublicoYRegistroMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean AutenticacionApiService autenticacion;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;
    @MockBean NotificacionesApiService notificaciones;

    private void sinLlamadasALaApi() {
        verifyNoInteractions(donaciones, autenticacion, incentivos, logistica, notificaciones);
    }

    // La foto del hero y la galería son ilustrativas; las donaciones destacadas siguen sin datos inventados.
    @Test void landingNoLlamaALaApiYMuestraGaleriaIlustrativa() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Entrega de útiles escolares")))
                .andExpect(content().string(not(containsString("100% trazabilidad verificada"))))
                .andExpect(content().string(containsString("Pronto vas a poder ver acá las donaciones entregadas del último mes.")));
        sinLlamadasALaApi();
    }

    @Test void mapaNoLlamaALaApiNiTraeCoordenadasNiUnsplash() throws Exception {
        mvc.perform(get("/explorar-donaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))))
                .andExpect(content().string(containsString("Pronto vas a poder ver acá las donaciones entregadas del último mes.")));
        sinLlamadasALaApi();
    }

    @Test void detallePublicoSinDatosMuestraEstadoVacio() throws Exception {
        mvc.perform(get("/explorar-donaciones/15"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Esta donación no está disponible públicamente.")))
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))));
        sinLlamadasALaApi();
    }

    @Test void registroDonanteHumanoNoRedirigeNiLlamaALaApi() throws Exception {
        mvc.perform(post("/registro/donante-humano")
                        .param("nombre", "Ana")
                        .param("apellido", "Prueba")
                        .param("email", "ana@example.com"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("El registro en línea todavía no está habilitado.")))
                .andExpect(content().string(containsString("value=\"Ana\"")))
                .andExpect(content().string(containsString("value=\"ana@example.com\"")));
        sinLlamadasALaApi();
    }

    @Test void registroEntidadNoRedirigeNiLlamaALaApi() throws Exception {
        mvc.perform(post("/registro/beneficiario").param("nombre", "Comedor Sol").param("tipoEntidad", "Hogar"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("El registro en línea todavía no está habilitado.")))
                .andExpect(content().string(containsString("value=\"Comedor Sol\"")));
        sinLlamadasALaApi();
    }

    @Test void registroDonanteOrganizacionNoRedirigeNiLlamaALaApi() throws Exception {
        mvc.perform(post("/registro/donante-organizacion").param("razonSocial", "Arcos S.A."))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string(containsString("El registro en línea todavía no está habilitado.")))
                .andExpect(content().string(containsString("value=\"Arcos S.A.\"")));
        sinLlamadasALaApi();
    }
}

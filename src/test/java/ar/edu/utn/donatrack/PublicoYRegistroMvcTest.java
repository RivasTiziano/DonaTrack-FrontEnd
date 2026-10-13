package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.EntregaMapaDto;
import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
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
import java.time.LocalDateTime;
import java.util.List;
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
                .andExpect(content().string(containsString("/img/hero-donaciones.jpg")))
                .andExpect(content().string(not(containsString("100% trazabilidad verificada"))))
                .andExpect(content().string(containsString("La transparencia empieza con datos reales")));
        sinLlamadasALaApi();
    }

    @Test void mapaNoLlamaALaApiNiTraeCoordenadasNiUnsplash() throws Exception {
        mvc.perform(get("/explorar-donaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))))
                .andExpect(content().string(containsString("No se muestran ubicaciones de entregas. Consultá las entregas públicas en la página principal.")));
        sinLlamadasALaApi();
    }

    @Test void detallePublicoSinDatosMuestraEstadoVacio() throws Exception {
        mvc.perform(get("/explorar-donaciones/15"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Este registro no está habilitado para consulta pública.")))
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))));
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

    @Test void detalleMuestraLasFotosRevisadasYLaEntidad() throws Exception {
        when(publico.detalleEntrega("15")).thenReturn(new EntregaPublicaDto(15L, "Fideos", LocalDateTime.of(2026, 10, 2, 10, 0),
                5f, "KILOGRAMO", null, "Alimentos", "Secos", new EntregaPublicaDto.EntidadResumen("Comedor Sol", "La Plata"),
                List.of("https://fotos.example/15.jpg")));
        mvc.perform(get("/explorar-donaciones/15"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("https://fotos.example/15.jpg")))
                .andExpect(content().string(containsString("Comedor Sol")))
                .andExpect(content().string(not(containsString("Este registro no está habilitado"))));
        sinLlamadasALaApi();
    }

    @Test void mapaDibujaLosMarcadoresConLaUbicacionDeLaEntidad() throws Exception {
        when(publico.entregasEnMapa()).thenReturn(List.of(new EntregaMapaDto(1L, LocalDateTime.of(2026, 1, 15, 10, 0),
                "Alimentos", "Secos", 5f, "KILOGRAMO",
                new EntregaMapaDto.EntidadUbicada("Comedor Sol", "La Plata", "Buenos Aires", -34.92f, -57.95f))));
        mvc.perform(get("/explorar-donaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comedor Sol")))
                .andExpect(content().string(containsString("-34.92")))
                .andExpect(content().string(not(containsString("No se muestran ubicaciones de entregas"))));
        sinLlamadasALaApi();
    }
}

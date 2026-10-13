package ar.edu.utn.donatrack;
import ar.edu.utn.donatrack.controllers.*;
import ar.edu.utn.donatrack.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DistribucionControllersMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean LogisticaApiService logistica;
    @MockBean IncentivosApiService incentivos;
    @MockBean ar.edu.utn.donatrack.services.RegistroApiService registroApi;

    /** Las áreas personales exigen sesión del rol correspondiente; lo público se pide sin sesión. */
    MockHttpServletRequestBuilder pedir(String ruta) {
        var request = get(ruta);
        if (ruta.startsWith("/donante/")) return request.session(TestSesiones.donante(1L));
        if (ruta.startsWith("/entidad/")) return request.session(TestSesiones.entidad(1L));
        return request;
    }

    @Test void rutasGeneralesConservanWebController() throws Exception {
        for(String ruta:List.of("/","/legal","/privacidad","/donante/dashboard","/entidad/dashboard"))
            mvc.perform(pedir(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(WebController.class));
    }
    // GET /donaciones es solo ADMIN desde el commit 035387d: los paneles personales no lo consultan.
    @Test void paginasDelPanelNoListanDonacionesDelBackend() throws Exception {
        for(String ruta:List.of("/donante/dashboard/donaciones","/donante/dashboard/entidades",
                "/entidad/dashboard/necesidades","/entidad/dashboard/donaciones"))
            mvc.perform(pedir(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(DonacionesController.class));
        verify(donaciones,never()).donaciones();
        verifyNoInteractions(logistica);
    }
    @Test void entregasNoConsultanLogisticaNiListanDonaciones() throws Exception {
        for(String ruta:List.of("/donante/dashboard/entregas","/entidad/dashboard/entregas"))
            mvc.perform(pedir(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(LogisticaController.class));
        verify(logistica,never()).entregas();
        verify(donaciones,never()).donaciones();
    }
    @Test void confirmarSoloLeeAsignacionesDeLaEntidad() throws Exception {
        mvc.perform(pedir("/entidad/dashboard/confirmar")).andExpect(status().isOk())
                .andExpect(handler().handlerType(LogisticaController.class));
        verify(donaciones).asignacionesEntidad(1L,"EN_TRASLADO");
        verify(donaciones,never()).donaciones();
        verify(donaciones,never()).beneficiarios();
        verifyNoInteractions(logistica,incentivos);
    }
    @Test void incentivosNoCargaLogistica() throws Exception {
        mvc.perform(pedir("/donante/dashboard/incentivos")).andExpect(status().isOk())
                .andExpect(handler().handlerType(IncentivosController.class));
        verify(incentivos).metricas(1L);
        verifyNoInteractions(logistica);
        verify(donaciones,never()).necesidades();
        verify(donaciones,never()).beneficiarios();
    }
    // El login real (con token) se prueba en AutenticacionMvcTest. Aquí solo se confirma que las pantallas de acceso y registro responden.
    @Test void accesoYRegistroSiguenSinLlamadasAlBackend() throws Exception {
        for(String ruta:List.of("/login","/registro","/registro/beneficiario","/registro/entidad-beneficiaria",
                "/registro/donante-humano","/registro/donante-organizacion"))
            mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(AutenticacionController.class));
        for(String ruta:List.of("/registro/donante-humano","/registro/donante-organizacion","/registro/entidad-beneficiaria"))
            // Sin datos, el registro muestra los errores del formulario (200), sin redirigir ni llamar a la API.
            mvc.perform(post(ruta)).andExpect(status().isOk())
                    .andExpect(handler().handlerType(AutenticacionController.class));
        verifyNoInteractions(donaciones,logistica,incentivos,registroApi);
    }
}

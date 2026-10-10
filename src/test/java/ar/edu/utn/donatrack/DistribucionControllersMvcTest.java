package ar.edu.utn.donatrack;
import ar.edu.utn.donatrack.controllers.*;
import ar.edu.utn.donatrack.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
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

    @Test void rutasGeneralesConservanWebController() throws Exception {
        for(String ruta:List.of("/","/legal","/privacidad","/donante/dashboard","/entidad/dashboard"))
            mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(WebController.class));
    }
    @Test void paginasDonacionesNoConsultanOtrosMicroservicios() throws Exception {
        for(String ruta:List.of("/explorar-donaciones","/explorar-donaciones/1",
                "/donante/dashboard/donaciones","/donante/dashboard/entidades",
                "/entidad/dashboard/necesidades","/entidad/dashboard/donaciones"))
            mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(DonacionesController.class));
        verifyNoInteractions(logistica,incentivos);
    }
    @Test void entregasSoloCarganSeguimientoYCabecera() throws Exception {
        for(String ruta:List.of("/donante/dashboard/entregas","/entidad/dashboard/entregas"))
            mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(LogisticaController.class));
        verify(logistica,times(2)).entregas();
        verify(donaciones,never()).donaciones();
        verify(donaciones,never()).necesidades();
        verifyNoInteractions(incentivos);
    }
    @Test void confirmarSigueSiendoPantallaSinEscrituras() throws Exception {
        mvc.perform(get("/entidad/dashboard/confirmar")).andExpect(status().isOk())
                .andExpect(handler().handlerType(LogisticaController.class));
        verify(donaciones).donaciones();
        verify(donaciones).beneficiarios();
        verifyNoMoreInteractions(donaciones);
        verifyNoInteractions(logistica,incentivos);
    }
    @Test void incentivosNoCargaLogistica() throws Exception {
        mvc.perform(get("/donante/dashboard/incentivos")).andExpect(status().isOk())
                .andExpect(handler().handlerType(IncentivosController.class));
        verify(incentivos).ranking();
        verifyNoInteractions(logistica);
        verify(donaciones,never()).necesidades();
        verify(donaciones,never()).beneficiarios();
    }
    @Test void accesoYRegistroSonDemostrativosSinLlamadasAlBackend() throws Exception {
        for(String ruta:List.of("/login","/registro","/registro/beneficiario","/registro/entidad-beneficiaria",
                "/registro/donante-humano","/registro/donante-organizacion"))
            mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(handler().handlerType(AutenticacionController.class));
        mvc.perform(post("/login").param("email","admin@example.com").param("password","demo"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(handler().handlerType(AutenticacionController.class));
        for(String ruta:List.of("/registro/donante-humano","/registro/donante-organizacion","/registro/entidad-beneficiaria"))
            mvc.perform(post(ruta)).andExpect(status().is3xxRedirection())
                    .andExpect(handler().handlerType(AutenticacionController.class));
        verifyNoInteractions(donaciones,logistica,incentivos);
    }
}

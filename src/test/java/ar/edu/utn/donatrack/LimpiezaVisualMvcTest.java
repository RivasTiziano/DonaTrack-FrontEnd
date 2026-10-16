package ar.edu.utn.donatrack;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import ar.edu.utn.donatrack.services.*;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class LimpiezaVisualMvcTest {
 @Autowired MockMvc mvc;
 @MockBean DonacionesApiService donaciones;
 @MockBean AutenticacionApiService auth;
 @MockBean IncentivosApiService incentivos;
 @MockBean LogisticaApiService logistica;
 @MockBean NotificacionesApiService notificaciones;
 @Test void formularioDedicadoYSidebarConLogoutPost() throws Exception {
  when(donaciones.categorias()).thenReturn(List.of());
  mvc.perform(get("/admin/dashboard/donaciones/nueva").session(TestSesiones.admin()))
    .andExpect(status().isOk()).andExpect(view().name("admin-donacion-nueva"))
    .andExpect(content().string(containsString("name=\"donanteId\"")))
    .andExpect(content().string(containsString("sidebar-logout")))
    .andExpect(content().string(containsString("action=\"/logout\" method=\"post\"")));
 }
 @Test void errorConservaValoresYBienesAdicionales() throws Exception {
  mvc.perform(post("/admin/dashboard/donaciones").session(TestSesiones.admin())
   .param("donanteId","invalido").param("descripcionGeneral","Prueba conservar")
   .param("descripcion","Bien principal").param("cantidad","2").param("subCategoriaId","1")
   .param("unidadMedida","UNIDAD").param("tipoBien","DURABLE").param("fueUsado","false")
   .param("bienDescripcion","Bien adicional").param("bienCantidad","3").param("bienSubCategoriaId","1")
   .param("bienUnidadMedida","UNIDAD").param("bienTipoBien","DURABLE").param("bienFueUsado","false"))
   .andExpect(status().isOk()).andExpect(view().name("admin-donacion-nueva"))
   .andExpect(content().string(containsString("value=\"Prueba conservar\"")))
   .andExpect(content().string(containsString("value=\"Bien adicional\"")));
  verify(donaciones,never()).crearDonacion(any());
 }
}

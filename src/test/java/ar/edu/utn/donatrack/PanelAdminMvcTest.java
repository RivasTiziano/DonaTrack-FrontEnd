package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.services.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PanelAdminMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean LogisticaApiService logistica;
    @MockBean IncentivosApiService incentivos;
    private final LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));

    private ApiDtos.Donacion donacion(long id, int dias, String estado) {
        var bien = new ApiDtos.Bien(id,"Alimentos secos",null,80f,50f,null,"KILOGRAMO","PERECEDERO",hoy.plusDays(dias),null);
        return new ApiDtos.Donacion(id,101L,"Campaña de alimentos",bien,estado,hoy.atStartOfDay());
    }
    @BeforeEach void datos() {
        when(donaciones.donaciones()).thenReturn(List.of(donacion(1,7,"EN_DEPOSITO"),donacion(2,-1,"EN_DEPOSITO"),
                donacion(3,8,"EN_DEPOSITO"),donacion(4,2,"ASIGNACION_REALIZADA")));
        when(donaciones.necesidades()).thenReturn(List.of(new ApiDtos.Necesidad(1L,2L,donacion(1,7,"EN_DEPOSITO").bien(),
                "Comedor comunitario","EXTRAORDINARIA",null,30f,50f,"PENDIENTE")));
        when(donaciones.bienes()).thenReturn(List.of(donacion(1,7,"EN_DEPOSITO").bien()));
        when(donaciones.categorias()).thenReturn(List.of(new ApiDtos.Categoria(1L,"Alimentos",true,List.of(new ApiDtos.SubCategoria(2L,"Secos",true)))));
        when(donaciones.sugerencias()).thenReturn(List.of(new ApiDtos.Sugerencia(false,List.of(),List.of(),1L)));
        when(donaciones.listarHumanos()).thenReturn(List.of(new ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse(
                101L,"Ana","Prueba","DNI","12345678",List.of(),"COMPLETO")));
        when(donaciones.listarJuridicos()).thenReturn(List.of());
        when(donaciones.beneficiarios()).thenReturn(List.of(new ApiDtos.Beneficiario(2L,"Comedor de prueba","ONG","Alimentos",true,
                new DonanteRequests.Direccion("Calle","CABA","Buenos Aires","123",-34.6f,-58.4f),null,List.of())));
        when(incentivos.categorias()).thenReturn(List.of(new IncentivosDtos.Categoria(1L,"Inicial",null,
                List.of(new IncentivosDtos.Mision(1L,"RACHA","Primera misión","Donar de manera recurrente",3,1,null)))));
        when(donaciones.algoritmos()).thenReturn(List.of(new ApiDtos.Algoritmo("Semantico",true)));
        when(logistica.camiones()).thenReturn(List.of(new ApiDtos.Camion(3L,"AB123CD",30f,3f,2000f,"DISPONIBLE")));
        when(logistica.entregas()).thenReturn(List.of(new ApiDtos.Entrega(10L,9L,"NO_RECIBIDA","Depósito cerrado",List.of(),3L,"AB123CD",2L,null,null,null),
                new ApiDtos.Entrega(11L,8L,"EN_TRASLADO",null,List.of(),3L,"AB123CD",2L,null,null,null)));
        when(logistica.rutas()).thenReturn(List.of(new ApiDtos.Ruta(1L,"Chofer",hoy,"PLANIFICADA",3L,50.0,60,List.of())));
        when(logistica.ubicaciones()).thenReturn(List.of(new LogisticaDetalles.Seguimiento("AB123CD",-34.6,-58.4,0f,
                hoy.atStartOfDay(),1L,3,1,2,33.0,"Comedor")));
        when(incentivos.ranking()).thenReturn(List.of(new ApiDtos.Ranking(101L,8),new ApiDtos.Ranking(102L,6),new ApiDtos.Ranking(103L,4),new ApiDtos.Ranking(104L,2)));
    }
    @Test void cuentaDatosRealesYRespetaLimiteDeSieteDias() throws Exception {
        mvc.perform(get("/admin/dashboard").session(TestSesiones.admin()))
                .andExpect(status().isOk()).andExpect(view().name("admin-panel"))
                .andExpect(model().attribute("kpiDeposito",3L)).andExpect(model().attribute("kpiVencer",2L))
                .andExpect(model().attribute("kpiNecesidades",1L)).andExpect(model().attribute("kpiFlota",1L))
                .andExpect(model().attribute("kpiTraslado",1L)).andExpect(model().attribute("kpiNoRecibidas",1L))
                .andExpect(content().string(containsString("Ana Prueba")))
                .andExpect(content().string(containsString("Depósito cerrado")))
                .andExpect(content().string(not(containsString("api-links"))));
    }
    @Test void apiCaidaNoRompeLasOtrasTarjetas() throws Exception {
        when(donaciones.donaciones()).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/admin/dashboard").session(TestSesiones.admin()))
                .andExpect(status().isOk()).andExpect(model().attribute("kpiDeposito",nullValue()))
                .andExpect(model().attribute("kpiFlota",1L))
                .andExpect(content().string(containsString("id=\"kpiDeposito\">—")))
                .andExpect(content().string(containsString("role=\"alert\"")));
    }
    @Test void listadosNoMuestranPestanasDeOtrosModulos() throws Exception {
        mvc.perform(get("/admin/dashboard/donaciones").session(TestSesiones.admin()))
            .andExpect(content().string(not(containsString("aria-label=\"Matchmaking\""))))
            .andExpect(content().string(not(containsString("aria-label=\"Rankings\""))));
    }

    @Test void navegacionNoOfreceModulosSinPermiso() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute(ar.edu.utn.donatrack.services.internal.SesionActual.ATRIBUTO,
            ar.edu.utn.donatrack.models.SesionUsuario.desde("operador@example.com",
                new AuthDtos.Token("ficticio","ficticio",900,"ADMIN",List.of("GESTIONAR_DONANTES"),null),"Operador"));
        mvc.perform(get("/admin/dashboard").session(session)).andExpect(status().isOk())
            .andExpect(content().string(containsString("href=\"/admin/dashboard/donantes\"")))
            .andExpect(content().string(not(containsString("href=\"/admin/dashboard/camiones\""))));
        verifyNoInteractions(logistica, incentivos);
    }

    @Test void podioOcultoSinMisionesOAnteFallo() throws Exception {
        for (var ranking : List.of(List.<ApiDtos.Ranking>of(),List.of(new ApiDtos.Ranking(101L,0)))) {
            when(incentivos.ranking()).thenReturn(ranking);
            for (String path : List.of("/admin/dashboard","/admin/dashboard/rankings"))
                mvc.perform(get(path).session(TestSesiones.admin())).andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("class=\"podium\""))))
                    .andExpect(content().string(not(containsString("class=\"dt-podium-grid\""))));
        }
        when(incentivos.ranking()).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/admin/dashboard").session(TestSesiones.admin())).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("class=\"dt-podium-grid\""))));
    }

    @Test void panelNoHaceEscrituras() throws Exception {
        mvc.perform(get("/admin/dashboard").session(TestSesiones.admin())).andExpect(status().isOk());
        verify(donaciones,never()).evaluar();
    }
    @Test void listadosNoContienenFormularioDeEdicionDentroDeCeldas() throws Exception {
        for(String path:List.of("donaciones","bienes","camiones","necesidades","rutas")) {
            String html=mvc.perform(get("/admin/dashboard/"+path).session(TestSesiones.admin()))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            org.junit.jupiter.api.Assertions.assertFalse(java.util.regex.Pattern.compile("<td[^>]*>(?:(?!</td>)[\\s\\S])*<(input|select|textarea)\\b").matcher(html).find(),path);
        }
    }
    @Test void renderizaFixturesParaRevisionVisualSinTocarBasesReales() throws Exception {
        for(String path:List.of("","donantes","donaciones","camiones","rutas","necesidades","seguimiento","asignar","bienes","rankings","catalogo","importar","beneficiarios","incentivos","incentivos/donantes","notificaciones")) {
            var request=get("/admin/dashboard"+(path.isEmpty()?"":"/"+path)).session(TestSesiones.admin());
            if(path.equals("asignar")) request.flashAttr("sugerencia",new ApiDtos.Sugerencia(true,donaciones.necesidades(),
                    List.of(new ApiDtos.Top("Semantico",donaciones.necesidades()),new ApiDtos.Top("Subatendidos",donaciones.necesidades())),1L));
            var response=mvc.perform(request)
                    .andExpect(status().isOk()).andReturn().getResponse();
            if(Boolean.getBoolean("visual.qa")) {
                Path directory=Path.of("target","visual-qa");Files.createDirectories(directory);
                Files.writeString(directory.resolve(path.isEmpty()?"panel.html":path.replace('/','-')+".html"),response.getContentAsString(),java.nio.charset.StandardCharsets.UTF_8);
            }
        }
    }
}

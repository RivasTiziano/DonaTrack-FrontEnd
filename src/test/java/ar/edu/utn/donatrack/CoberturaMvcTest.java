package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import java.time.LocalDate;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CoberturaMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean LogisticaApiService logistica;
    @MockBean IncentivosApiService incentivos;
    DonanteRequests.Direccion direccion = new DonanteRequests.Direccion("Calle Real", "CABA", "Buenos Aires", "123", -34f, -58f);
    MedioDeContactoDto email = new MedioDeContactoDto("EMAIL", "real@example.com");
    DonacionesDetalles.Humano humano;
    DonacionesDetalles.Juridico juridico;
    DonacionesDetalles.Beneficiario beneficiario;

    @BeforeEach void defaults() {
        humano = new DonacionesDetalles.Humano(1L, "Ana", "Real", LocalDate.of(1990,1,1),
                "DNI", "12345678", "OTRO", direccion, List.of(email), email, "COMPLETO");
        var rep = new DonanteRequests.Representante(9L, "Representante", "Real", "DNI", "87654321",
                LocalDate.of(1980,1,1), "OTRO");
        juridico = new DonacionesDetalles.Juridico(2L, "30123456789", "Empresa Real", "EMPRESA", "Alimentos",
                direccion, List.of(email), email, List.of(rep), "COMPLETO");
        beneficiario = new DonacionesDetalles.Beneficiario(3L, "Entidad Real", "ONG", "Alimentos",
                direccion, email, List.of(new DonacionesDetalles.Representante(8L, "Referente Real", "+5491112345678", List.of(email))), true);
        when(donaciones.humano(1L)).thenReturn(humano);
        when(donaciones.juridico(2L)).thenReturn(juridico);
        when(donaciones.beneficiario(3L)).thenReturn(beneficiario);
        when(donaciones.beneficiarios()).thenReturn(List.of(new ApiDtos.Beneficiario(3L,"Entidad Real","ONG","Alimentos",true)));
        when(donaciones.listarHumanos()).thenReturn(List.of());
        when(donaciones.listarJuridicos()).thenReturn(List.of());
        when(donaciones.sugerencias()).thenReturn(List.of(new ApiDtos.Sugerencia(false,List.of(),List.of(),1L)));
        when(donaciones.sugerencia(1L)).thenReturn(new ApiDtos.Sugerencia(false,List.of(),List.of(),1L));
        when(donaciones.asignacionesEntidad(3L, "")).thenReturn(List.of(new ApiDtos.Asignacion(5L,1L,1L,3L,20f,"PENDIENTE_RUTA",null)));
        when(donaciones.contactosDonacion(1L)).thenReturn(new DonacionesDetalles.Contactos(
                new DonacionesDetalles.Contacto(new DonacionesDetalles.Destinatario("Ana","real@example.com",null),"EMAIL"),null));
        when(donaciones.contactosAsignacion(5L)).thenReturn(new DonacionesDetalles.Contactos(null,
                new DonacionesDetalles.Contacto(new DonacionesDetalles.Destinatario("Entidad","entidad@example.com",null),"EMAIL")));
        when(donaciones.bien(1L)).thenReturn(new ApiDtos.Bien(1L,"Arroz",null,30f,20f,null,"KILOGRAMO","PERECEDERO",null,null));
        when(donaciones.necesidad(1L)).thenReturn(new ApiDtos.Necesidad(1L,3L,null,"Alimentos","EXTRAORDINARIA",null,0f,20f,"PENDIENTE"));
        var camion = new ApiDtos.Camion(1L,"AA123BB",20f,3f,1000f,"DISPONIBLE");
        when(logistica.camion(1L)).thenReturn(camion);
        var entrega = new ApiDtos.Entrega(1L,5L,"PENDIENTE",null,List.of("https://example.com/foto.jpg"),null,null);
        when(logistica.entrega(1L)).thenReturn(entrega);
        var parada = new LogisticaDetalles.Parada(1L,1,
                new LogisticaDetalles.Direccion("Calle","1","CABA","BA",-34.0,-58.0),List.of(entrega),"10:00");
        when(logistica.paradas()).thenReturn(List.of(parada));
        when(logistica.parada(1L)).thenReturn(parada);
        when(logistica.ruta(1L)).thenReturn(new LogisticaDetalles.Ruta(1L,"Chofer Real",LocalDate.of(2026,10,10),
                "PLANIFICADA",1L,List.of(parada),10.0,30,"09:00","10:00"));
        when(logistica.ubicaciones()).thenReturn(List.of(new LogisticaDetalles.Seguimiento("AA123BB",-34.0,-58.0,
                20f,null,1L,2,1,1,50,"Calle 1")));
        when(logistica.ubicacion("AA123BB")).thenReturn(new LogisticaDetalles.Ubicacion("AA123BB",-34.0,-58.0,20f,null));
        var mision = new IncentivosDtos.Mision(1L,"RACHA","Racha","Descripción",3,1,null);
        var categoria = new IncentivosDtos.Categoria(1L,"Solidario",null,List.of(mision));
        when(incentivos.categorias()).thenReturn(List.of(categoria));
        when(incentivos.categoria(1L)).thenReturn(categoria);
        when(incentivos.misiones(1L)).thenReturn(List.of(mision));
        when(incentivos.metricas(1L)).thenReturn(new IncentivosDtos.Metricas(2,30,1,"Solidario",
                List.of(new IncentivosDtos.MetricaMensual("2026-10",2,30,1))));
        when(incentivos.misionesCompletadas(1L)).thenReturn(List.of(new IncentivosDtos.MisionCompletada(1,"Racha","Descripción",null)));
        when(incentivos.insignias(1L)).thenReturn(List.of(new IncentivosDtos.Insignia("Insignia","Descripción",null)));
        when(incentivos.historialCategorias(1L)).thenReturn(List.of(new IncentivosDtos.Auditoria(1L,null,"Solidario",null)));
        when(incentivos.progreso(1L)).thenReturn(new IncentivosDtos.Progreso(null,null,null,null,null,null,null,null,"Todas las misiones han sido completadas"));
        when(incentivos.historialRankings()).thenReturn(List.of(new IncentivosDtos.RankingHistorico(1L,LocalDate.of(2026,10,1),
                List.of(new IncentivosDtos.Posicion(1,2,new IncentivosDtos.DonanteRanking(1L))))));
    }

    @Test void rendersEveryNewPageWithActualContractFields() throws Exception {
        for (String path : List.of("/donantes/humanos/1","/donantes/juridicos/2","/beneficiarios/nuevo","/beneficiarios/3",
                "/bienes/1","/necesidades/1","/asignaciones","/asignaciones?entidadBeneficiariaId=3",
                "/donaciones/1/contactos","/asignaciones/5/contactos","/sugerencias","/sugerencias/1",
                "/camiones/1","/entregas/1","/rutas/1","/paradas","/paradas/1","/seguimiento","/seguimiento/AA123BB",
                "/incentivos","/incentivos/categorias/1","/incentivos/donantes","/incentivos/donantes/1","/incentivos/historial"))
            mvc.perform(get("/admin/dashboard" + path)).andExpect(status().isOk());
    }

    @Test void donorUpdatePreservesAllContactsAndRepresentatives() throws Exception {
        mvc.perform(profile("/admin/dashboard/donantes/humanos/1")
                .param("nombre","Ana Editada").param("apellido","Real").param("fechaNacimiento","1990-01-01")
                .param("numeroDocumento","12345678").param("genero","OTRO"))
                .andExpect(status().is3xxRedirection());
        verify(donaciones).actualizarHumano(eq(1L), argThat(r -> r.nombre().equals("Ana Editada")
                && r.mediosDeContacto().equals(humano.mediosDeContacto()) && r.medioDeContactoPredeterminado().equals(email)));
        mvc.perform(profile("/admin/dashboard/donantes/juridicos/2")
                .param("numeroDocumento","30123456789").param("razonSocial","Empresa Editada")
                .param("tipoEntidad","EMPRESA").param("rubro","Alimentos"))
                .andExpect(status().is3xxRedirection());
        verify(donaciones).actualizarJuridico(eq(2L), argThat(r -> r.representantesJuridicos().equals(juridico.personasRepresentantes())));
    }

    @Test void invalidProfileDoesNotWriteAndKeepsSubmittedName() throws Exception {
        mvc.perform(profile("/admin/dashboard/donantes/humanos/1").param("nombre","Nombre conservado"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Nombre conservado")));
        verify(donaciones,never()).actualizarHumano(anyLong(),any());
    }

    @Test void beneficiaryCreationAndUpdateMatchDistinctContracts() throws Exception {
        mvc.perform(profile("/admin/dashboard/beneficiarios/nuevo").param("razonSocial","Entidad")
                .param("tipoEntidad","ONG").param("rubro","Alimentos").param("contactoTipo","EMAIL")
                .param("contactoValor","entidad@example.com").param("repNombre","").param("repTelefono","").param("repEmail",""))
                .andExpect(status().is3xxRedirection());
        verify(donaciones).crearBeneficiario(argThat(r -> r.representantes().isEmpty()));
        mvc.perform(profile("/admin/dashboard/beneficiarios/3").param("razonSocial","Entidad Editada")
                .param("tipoEntidad","ONG").param("rubro","Alimentos").param("contactoTipo","EMAIL")
                .param("contactoValor","entidad@example.com").param("activo","false"))
                .andExpect(status().is3xxRedirection());
        verify(donaciones).actualizarBeneficiario(eq(3L),argThat(r -> !r.activo()
                && r.representantes().equals(beneficiario.representantes())));
    }

    @Test void assignmentFiltersAndNullableBeneficiaryContactAreSupported() throws Exception {
        when(donaciones.asignacionesEntidad(3L,"ENTREGADA")).thenReturn(List.of());
        mvc.perform(get("/admin/dashboard/asignaciones").param("entidadBeneficiariaId","3").param("estado","ENTREGADA"))
                .andExpect(status().isOk());
        verify(donaciones).asignacionesEntidad(3L,"ENTREGADA");
        mvc.perform(get("/admin/dashboard/donaciones/1/contactos")).andExpect(status().isOk())
                .andExpect(content().string(containsString("puede no estar asignada")));
    }

    @Test void routeRequiresDriverAndPlanningIsAcceptedNotCompleted() throws Exception {
        mvc.perform(post("/admin/dashboard/rutas/1/iniciar")).andExpect(status().isBadRequest());
        mvc.perform(post("/admin/dashboard/rutas/1/iniciar").param("chofer",""))
                .andExpect(flash().attributeExists("apiError"));
        verify(logistica,never()).iniciarRuta(anyLong(),anyString());
        mvc.perform(post("/admin/dashboard/rutas/1/iniciar").param("chofer","Chofer Real"))
                .andExpect(status().is3xxRedirection());
        verify(logistica).iniciarRuta(1L,"Chofer Real");
        when(logistica.planificar(LocalDate.of(2026,10,10)))
                .thenReturn(new LogisticaDetalles.Planificacion(2,1,List.of("request-real")));
        mvc.perform(post("/admin/dashboard/rutas/planificar").param("fecha","2026-10-10"))
                .andExpect(flash().attribute("success",containsString("después del callback")))
                .andExpect(flash().attributeExists("planificacion"));
    }

    @Test void allMissionsCompletedIsNotDisplayedAsZeroPercent() throws Exception {
        mvc.perform(get("/admin/dashboard/incentivos/donantes/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Todas las misiones han sido completadas")))
                .andExpect(content().string(not(containsString("null / null"))));
    }

    @Test void currentMissionUsesActualProgressValues() throws Exception {
        when(incentivos.progreso(1L)).thenReturn(new IncentivosDtos.Progreso(2L,"Misión actual","Descripción",
                2,4,50.0,false,1,null));
        mvc.perform(get("/admin/dashboard/incentivos/donantes/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("2 / 4 (50.0%)")));
    }

    @Test void incompleteBeneficiaryRepresentativeDoesNotWrite() throws Exception {
        mvc.perform(profile("/admin/dashboard/beneficiarios/nuevo").param("razonSocial","Entidad")
                .param("tipoEntidad","ONG").param("rubro","Alimentos").param("contactoTipo","EMAIL")
                .param("contactoValor","entidad@example.com").param("repNombre","Referente")
                .param("repTelefono","").param("repEmail",""))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Completá nombre y email")))
                .andExpect(content().string(containsString("Referente")));
        verify(donaciones,never()).crearBeneficiario(any());
    }

    @Test void incentiveManagementUsesCorrectPayloadsAndDoesNotRegisterDonations() throws Exception {
        mvc.perform(post("/admin/dashboard/incentivos/categorias").param("nombre","Solidario").param("tipo","RACHA").param("valorObjetivo","3"))
                .andExpect(status().is3xxRedirection());
        verify(incentivos).crearCategoria(new IncentivosDtos.CategoriaRequest("Solidario",null,new IncentivosDtos.MisionRequest("RACHA",3)));
        mvc.perform(post("/admin/dashboard/incentivos/categorias/1/misiones").param("tipo","COMPLETITUD").param("valorObjetivo","2"))
                .andExpect(status().is3xxRedirection());
        verify(incentivos).agregarMision(1L,new IncentivosDtos.MisionRequest("COMPLETITUD",2));
        mvc.perform(post("/admin/dashboard/incentivos/categorias/1/misiones/2/eliminar"))
                .andExpect(status().is3xxRedirection());
        verify(incentivos).eliminarMision(1L,2L);
        mvc.perform(post("/admin/dashboard/incentivos/donantes/1/visibilidad").param("visible","false"))
                .andExpect(status().is3xxRedirection());
        verify(incentivos).visibilidad(1L,false);
    }

    @Test void readFailureAndRejectedWriteNeverReportSuccess() throws Exception {
        when(logistica.entrega(1L)).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/admin/dashboard/entregas/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("role=\"alert\"")));
        doThrow(new ResourceAccessException("offline")).when(donaciones).eliminarBeneficiario(3L);
        mvc.perform(post("/admin/dashboard/beneficiarios/3/eliminar")).andExpect(flash().attributeExists("apiError"))
                .andExpect(flash().attributeCount(1));
    }

    @Test void evaluatorRunsOnlyByExplicitPost() throws Exception {
        mvc.perform(get("/admin/dashboard/sugerencias")).andExpect(status().isOk());
        verify(donaciones,never()).evaluar();
        mvc.perform(post("/admin/dashboard/evaluador/ejecutar")).andExpect(status().is3xxRedirection());
        verify(donaciones).evaluar();
    }

    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder profile(String path) {
        return post(path).param("calle","Calle Real").param("numero","123").param("ciudad","CABA").param("provincia","Buenos Aires");
    }
}

package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.clients.DonantesApiClient;
import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.ApiRequests;
import ar.edu.utn.donatrack.dto.donante.*;
import ar.edu.utn.donatrack.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean LogisticaApiService logistica;
    @MockBean IncentivosApiService incentivos;
    @MockBean DonantesApiClient donantes;
    @MockBean DonantesApiService registroDonantes;

    @BeforeEach void defaults() {
        when(donaciones.donaciones()).thenReturn(List.of());
        when(donaciones.categorias()).thenReturn(List.of());
        when(donaciones.bienes()).thenReturn(List.of());
        when(donaciones.necesidades()).thenReturn(List.of());
        when(donaciones.beneficiarios()).thenReturn(List.of());
        when(donaciones.algoritmos()).thenReturn(List.of());
        when(logistica.camiones()).thenReturn(List.of());
        when(logistica.entregas()).thenReturn(List.of());
        when(logistica.rutas()).thenReturn(List.of());
        when(incentivos.ranking()).thenReturn(List.of());
        when(donantes.listarHumanos()).thenReturn(List.of());
        when(donantes.listarJuridicos()).thenReturn(List.of());
    }

    @Test void rendersAllAdministrativePagesWithoutBackends() throws Exception {
        for (String path : List.of("", "/donantes", "/donaciones", "/bienes", "/catalogo", "/beneficiarios", "/necesidades", "/camiones", "/rankings", "/asignar", "/importar", "/entregas", "/rutas"))
            mvc.perform(get("/admin/dashboard" + path)).andExpect(status().isOk());
    }

    @Test void rendersHumanAndJuridicalDonorsAndFilters() throws Exception {
        when(donantes.listarHumanos()).thenReturn(List.of(new DonanteHumanoResponse(101L, "Persona Real", "Prueba", "DNI", "12345678",
                List.of(new MedioDeContactoDto("EMAIL", "real@example.com")), "COMPLETO")));
        when(donantes.listarJuridicos()).thenReturn(List.of(new DonanteJuridicoResponse(102L, "Empresa Real", "30123456789",
                List.of(), "PENDIENTE")));
        mvc.perform(get("/admin/dashboard/donantes"))
                .andExpect(content().string(containsString("Persona Real Prueba")))
                .andExpect(content().string(containsString("Empresa Real")))
                .andExpect(content().string(containsString("real@example.com")))
                .andExpect(content().string(containsString("COMPLETO")))
                .andExpect(content().string(containsString("data-type=\"Jurídica\"")))
                .andExpect(content().string(not(containsString("Juan Pérez"))));
    }

    @Test void backendFailureDoesNotShowInventedOrEmptySuccess() throws Exception {
        when(donaciones.donaciones()).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/admin/dashboard/donaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(not(containsString("No hay donaciones registradas."))));
    }

    @Test void rendersDonationAvailableAmountAndAssignmentStates() throws Exception {
        var bien = new ApiDtos.Bien(1L, "Arroz real", null, 80f, 50f, null, "KILOGRAMO", "PERECEDERO", null, null);
        var donation = new ApiDtos.Donacion(9L, 101L, "Donación real", bien, "EN_DEPOSITO", null);
        when(donaciones.donaciones()).thenReturn(List.of(donation));
        when(donaciones.donacion(9L)).thenReturn(donation);
        when(donaciones.asignaciones(9L)).thenReturn(List.of(new ApiDtos.Asignacion(7L, 9L, 1L, 2L, 30f, "EN_TRASLADO", 3L)));
        mvc.perform(get("/admin/dashboard/donaciones"))
                .andExpect(content().string(containsString("50.0 KILOGRAMO")));
        mvc.perform(get("/admin/dashboard/donaciones/9"))
                .andExpect(content().string(containsString("EN_TRASLADO")));
    }

    @Test void assignmentUsesExactAmountAndRedirectsAfterSuccess() throws Exception {
        mvc.perform(post("/admin/dashboard/asignar").param("donacionId", "9").param("necesidadId", "2").param("cantidadAAsignar", "30"))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("success"));
        verify(donaciones).asignar(new ApiRequests.Asignacion(9L, 2L, 30f));
    }

    @Test void rejectedAssignmentDoesNotReportSuccess() throws Exception {
        doThrow(new ResourceAccessException("offline")).when(donaciones).asignar(any());
        mvc.perform(post("/admin/dashboard/asignar").param("donacionId", "9").param("necesidadId", "2").param("cantidadAAsignar", "30"))
                .andExpect(flash().attributeExists("apiError")).andExpect(flash().attributeCount(1));
    }

    @Test void csvUploadsAndRendersActualRowErrors() throws Exception {
        var result = new ApiDtos.Importacion(100, 98, 1, 1, List.of(new ApiDtos.ErrorCsv(11, "Email en uso")));
        when(donaciones.importar(any())).thenReturn(result);
        var file = new MockMultipartFile("archivo", "personas.csv", "text/csv", "TipoPersona,TipoDoc\nHUMANA,DNI".getBytes());
        mvc.perform(multipart("/admin/dashboard/importar").file(file))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("importacion", result));
        mvc.perform(get("/admin/dashboard/importar").flashAttr("importacion", result))
                .andExpect(content().string(containsString("Fila 11: Email en uso")));
    }

    @Test void csvEmptyNeverCallsApi() throws Exception {
        mvc.perform(multipart("/admin/dashboard/importar").file(new MockMultipartFile("archivo", new byte[0])))
                .andExpect(flash().attributeExists("apiError"));
        verify(donaciones, never()).importar(any());
    }

    @Test void matchmakingRendersNoIntersectionAndIndividualTops() throws Exception {
        var necesidad = new ApiDtos.Necesidad(5L, 2L, null, "Necesidad real", "EXTRAORDINARIA", null, 0f, 30f, "PENDIENTE");
        var suggestion = new ApiDtos.Sugerencia(false, List.of(), List.of(new ApiDtos.Top("Semantico", List.of(necesidad))), 9L);
        mvc.perform(get("/admin/dashboard/asignar").flashAttr("sugerencia", suggestion))
                .andExpect(content().string(containsString("No hay intersección")))
                .andExpect(content().string(containsString("Necesidad real")));
    }

    @Test void ranksByActualApiMetricNotInventedKilograms() throws Exception {
        when(incentivos.ranking()).thenReturn(List.of(new ApiDtos.Ranking(101L, 8)));
        mvc.perform(get("/admin/dashboard/rankings"))
                .andExpect(content().string(containsString("Misiones resueltas")))
                .andExpect(content().string(containsString("101")));
    }

    @Test void goodsFormBindsNumbersAndDates() throws Exception {
        mvc.perform(post("/admin/dashboard/bienes")
                .param("descripcion", "Sillas").param("cantidad", "10").param("subCategoriaId", "3")
                .param("unidadMedida", "UNIDAD").param("tipoBien", "DURABLE").param("fueUsado", "false"))
                .andExpect(flash().attributeExists("success"));
        verify(donaciones).crearBien(new ApiRequests.Bien("Sillas", null, 10f, 3L, "UNIDAD", "DURABLE", null, false));
    }

    @Test void invalidAmountReturnsReadable400AndDoesNotCallApi() throws Exception {
        mvc.perform(post("/admin/dashboard/asignar").param("donacionId", "9").param("necesidadId", "2").param("cantidadAAsignar", "texto"))
                .andExpect(status().isBadRequest()).andExpect(content().string(containsString("formatos inválidos")));
        verify(donaciones, never()).asignar(any());
    }

    @Test void rendersPopulatedTablesAndEditForms() throws Exception {
        var category = new ApiDtos.Categoria(1L, "Alimentos", true, List.of(new ApiDtos.SubCategoria(2L, "Secos", true)));
        var bien = new ApiDtos.Bien(3L, "Arroz", null, 80f, 50f,
                new ApiDtos.CategoriaBien("Alimentos", new ApiDtos.SubCategoriaBien("Secos")), "KILOGRAMO", "PERECEDERO", java.time.LocalDate.of(2027,1,1), null);
        when(donaciones.categorias()).thenReturn(List.of(category));
        when(donaciones.bienes()).thenReturn(List.of(bien));
        when(donaciones.necesidades()).thenReturn(List.of(new ApiDtos.Necesidad(1L, 2L, bien, "Alimentos", "RECURRENTE", "P1M", 0f, 80f, "PENDIENTE")));
        when(donaciones.beneficiarios()).thenReturn(List.of(new ApiDtos.Beneficiario(2L, "Entidad Real", "ONG", "Comedor", true)));
        when(logistica.camiones()).thenReturn(List.of(new ApiDtos.Camion(3L, "AB123CD", 30f, 3f, 2000f, "DISPONIBLE")));
        when(logistica.entregas()).thenReturn(List.of(new ApiDtos.Entrega(1L, 7L, "EN_TRASLADO", null, List.of(), 3L, "AB123CD")));
        when(logistica.rutas()).thenReturn(List.of(new ApiDtos.Ruta(1L, "Chofer", java.time.LocalDate.now(), "PLANIFICADA", 3L, 50.0, 60)));
        when(donaciones.algoritmos()).thenReturn(List.of(new ApiDtos.Algoritmo("Semantico", true)));
        for (String path : List.of("bienes", "necesidades", "beneficiarios", "catalogo", "camiones", "entregas", "rutas", "asignar"))
            mvc.perform(get("/admin/dashboard/" + path)).andExpect(status().isOk());
    }

    @Test void updateNecessityUsesPutContractWithoutChangingOwner() throws Exception {
        mvc.perform(post("/admin/dashboard/necesidades/5").param("descripcionNecesidad", "Ayuda recurrente")
                .param("tipoNecesidad", "RECURRENTE").param("periodo", "P1M").param("descripcion", "Arroz")
                .param("cantidad", "80").param("subCategoriaId", "2").param("unidadMedida", "KILOGRAMO").param("tipoBien", "PERECEDERO"))
                .andExpect(flash().attributeExists("success"));
        verify(donaciones).actualizarNecesidad(eq(5L), argThat(request -> "RECURRENTE".equals(request.tipoDeNecesidad()) && "P1M".equals(request.periodo())));
    }

    @Test void realDonorFormsRenderForBothTypes() throws Exception {
        mvc.perform(get("/admin/dashboard/donantes/nuevo/humanos")).andExpect(status().isOk());
        mvc.perform(get("/admin/dashboard/donantes/nuevo/juridicos")).andExpect(status().isOk());
    }

    @Test void donorCreationSendsDateNotAgeAndDoesNotCreateAccount() throws Exception {
        mvc.perform(post("/admin/dashboard/donantes/nuevo/humanos")
                .param("nombre", "Ana").param("apellido", "Pérez").param("numeroDocumento", "12345678")
                .param("fechaNacimiento", "1990-01-01").param("genero", "FEMENINO")
                .param("calle", "Calle").param("numero", "123").param("ciudad", "CABA").param("provincia", "CABA")
                .param("email", "ana@example.com"))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("success"));
        verify(registroDonantes).crearHumano(argThat(request -> java.time.LocalDate.of(1990,1,1).equals(request.fechaNacimiento())
                && "EMAIL".equals(request.medioDeContactoPredeterminado().tipoMedioContacto())));
    }

    @Test void rejectedDonorCreationKeepsSubmittedValuesAndEscapesErrors() throws Exception {
        doThrow(org.springframework.web.client.HttpClientErrorException.create(org.springframework.http.HttpStatus.BAD_REQUEST,
                "Bad request", org.springframework.http.HttpHeaders.EMPTY,
                "{\"message\":\"<script>alert(1)</script>\"}".getBytes(), java.nio.charset.StandardCharsets.UTF_8))
                .when(registroDonantes).crearHumano(any());
        mvc.perform(post("/admin/dashboard/donantes/nuevo/humanos").param("nombre", "Ana Conservada").param("email", "ana@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Ana Conservada\"")))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }
}

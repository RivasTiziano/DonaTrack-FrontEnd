package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pantalla Entregas del admin: publicar o despublicar solo entregas ENTREGADA, con la asignación de la fila. */
@SpringBootTest
@AutoConfigureMockMvc
class PublicacionEntregasMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean LogisticaApiService logistica;
    @MockBean IncentivosApiService incentivos;

    private static ApiDtos.Entrega entrega(long id, long asignacionId, String estado) {
        return new ApiDtos.Entrega(id, asignacionId, estado, null, List.of(), 3L, "AB123CD", 3L, null, null, null);
    }

    @Test void soloLasEntregasEntregadasTienenAccionDePublicar() throws Exception {
        when(logistica.entregas()).thenReturn(List.of(entrega(7L, 5L, "ENTREGADA"), entrega(8L, 6L, "EN_TRASLADO")));
        mvc.perform(get("/admin/dashboard/entregas").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("publicar-asignacion-5")))
                .andExpect(content().string(containsString("Publicar en la landing")))
                .andExpect(content().string(containsString("Revisá que las fotos no muestren datos personales")))
                .andExpect(content().string(not(containsString("publicar-asignacion-6"))));
    }

    @Test void laTablaMuestraNumeroDestinoYBienConUnaSolaLlamadaABeneficiarios() throws Exception {
        var conBienes = new ApiDtos.Entrega(20L, 9L, "PLANIFICADA", null, List.of(), 3L, "AB123CD", 3L, null, null,
                List.of(new ApiDtos.BienEntrega("Fideos secos", 5f), new ApiDtos.BienEntrega("Arroz", 2.5f)));
        var sinEntidad = new ApiDtos.Entrega(21L, 10L, "PLANIFICADA", null, List.of(), 3L, "AB123CD", 99L, null, null, null);
        when(logistica.entregas()).thenReturn(List.of(conBienes, sinEntidad));
        when(donaciones.beneficiarios()).thenReturn(List.of(
                new ApiDtos.Beneficiario(3L, "Comedor Los Pibes", null, null, true, null, null, null)));
        mvc.perform(get("/admin/dashboard/entregas").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("#20")))
                .andExpect(content().string(containsString("#21")))
                .andExpect(content().string(containsString("Comedor Los Pibes")))
                .andExpect(content().string(containsString("Fideos secos (5), Arroz (2,5)")));
        verify(donaciones, times(1)).beneficiarios();
    }

    @Test void siBeneficiariosFallaLaTablaIgualCarga() throws Exception {
        when(logistica.entregas()).thenReturn(List.of(entrega(7L, 5L, "ENTREGADA")));
        when(donaciones.beneficiarios()).thenThrow(new IllegalStateException("caído"));
        mvc.perform(get("/admin/dashboard/entregas").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("#7")))
                .andExpect(content().string(containsString("publicar-asignacion-5")));
    }

    @Test void publicarEnviaLaAsignacionDeLaFilaYLaDescripcion() throws Exception {
        when(donaciones.publicacion(eq(5L), eq(true), eq("Fideos del comedor")))
                .thenReturn(new DonacionesDetalles.Publicacion(5L, true, "Fideos del comedor", List.of("https://fotos.example/5.jpg")));
        mvc.perform(post("/admin/dashboard/asignaciones/5/publicacion").session(TestSesiones.admin())
                        .param("publicable", "true").param("descripcionPublica", "Fideos del comedor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard/entregas"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attributeExists("publicacion"));
        verify(donaciones).publicacion(5L, true, "Fideos del comedor");
    }

    @Test void despublicarNoEnviaDescripcion() throws Exception {
        when(donaciones.publicacion(anyLong(), anyBoolean(), any()))
                .thenReturn(new DonacionesDetalles.Publicacion(5L, false, null, List.of()));
        mvc.perform(post("/admin/dashboard/asignaciones/5/publicacion").session(TestSesiones.admin())
                        .param("publicable", "false").param("descripcionPublica", "texto que no debe viajar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("success"));
        verify(donaciones).publicacion(5L, false, null);
    }

    @Test void descripcionDeMasDe500CaracteresSeRechazaSinLlamarALaApi() throws Exception {
        mvc.perform(post("/admin/dashboard/asignaciones/5/publicacion").session(TestSesiones.admin())
                        .param("publicable", "true").param("descripcionPublica", "a".repeat(501)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard/entregas"))
                .andExpect(flash().attributeExists("apiError"))
                .andExpect(flash().attributeCount(1));
        verify(donaciones, never()).publicacion(anyLong(), anyBoolean(), any());
    }
}

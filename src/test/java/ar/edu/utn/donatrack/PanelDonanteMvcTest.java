package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Panel del donante: datos filtrados por la sesión, sin datos de maqueta y con visibilidad del propio donante. */
@SpringBootTest
@AutoConfigureMockMvc
class PanelDonanteMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;

    private DonacionesDetalles.Humano ana() {
        return new DonacionesDetalles.Humano(7L, "Ana", "Prueba", null, "DNI", "12345678", "F", null,
                List.of(), new MedioDeContactoDto("EMAIL", "ana@example.com"), "APROBADO");
    }

    @Test void perfilSaleDeLaSesionYNoSeListanDonacionesDelBackend() throws Exception {
        when(donaciones.humano(7L)).thenReturn(ana());
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Prueba")))
                .andExpect(content().string(containsString("Todavía no podemos mostrar tu historial de donaciones.")));
        verify(donaciones, never()).donaciones();
        verify(donaciones).humano(7L);
    }

    @Test void resumenNoMuestraDatosDeMaqueta() throws Exception {
        when(donaciones.humano(7L)).thenReturn(ana());
        mvc.perform(get("/donante/dashboard").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))))
                .andExpect(content().string(not(containsString("Chofer Asignado"))))
                .andExpect(content().string(not(containsString("CUIT-"))));
    }

    @Test void entidadesYEntregasNoInventanDatos() throws Exception {
        when(donaciones.beneficiarios()).thenReturn(List.of(new ApiDtos.Beneficiario(3L, "Fundación Vida", "ONG", "Comedor", true,
                null, new MedioDeContactoDto("EMAIL", "vida@example.org"), List.of())));
        mvc.perform(get("/donante/dashboard/entidades").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fundación Vida")))
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))))
                .andExpect(content().string(not(containsString("CUIT-"))));
        mvc.perform(get("/donante/dashboard/entregas").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Chofer Asignado"))));
        verify(logistica, never()).entregas();
    }

    @Test void visibilidadUsaElReferenciaDeLaSesionYNoElParametro() throws Exception {
        mvc.perform(post("/donante/dashboard/incentivos/visibilidad").session(TestSesiones.donante(7L))
                        .param("visible", "false").param("donanteId", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donante/dashboard/incentivos"));
        verify(incentivos).visibilidad(7L, false);
        verify(incentivos, never()).visibilidad(eq(99L), anyBoolean());
    }

    @Test void incentivosMuestranCategoriaYMetricasReales() throws Exception {
        when(incentivos.metricas(7L)).thenReturn(new IncentivosDtos.Metricas(3, 30, 2, "Sostenedor Plata", List.of()));
        when(incentivos.categorias()).thenReturn(List.of(new IncentivosDtos.Categoria(1L, "Iniciador Bronce", null, List.of()),
                new IncentivosDtos.Categoria(2L, "Sostenedor Plata", null, List.of())));
        mvc.perform(get("/donante/dashboard/incentivos").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sostenedor Plata")))
                .andExpect(content().string(not(containsString("rachaMeses"))))
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))));
    }

    @Test void caidaDelBackendDegradaSinRomperLaPagina() throws Exception {
        when(incentivos.metricas(7L)).thenThrow(new ResourceAccessException("offline"));
        when(donaciones.humano(7L)).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/donante/dashboard").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk());
    }
}

package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Panel de la entidad: el dueño es siempre el de la sesión; confirmación de recepción validada. */
@SpringBootTest
@AutoConfigureMockMvc
class PanelEntidadMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;

    @Test void altaDeNecesidadUsaLaEntidadDeLaSesionAunqueElFormularioMandeOtra() throws Exception {
        mvc.perform(post("/entidad/dashboard/necesidades").session(TestSesiones.entidad(3L))
                        .param("entidadBeneficiariaId", "99")
                        .param("descripcionNecesidad", "Fideos para el comedor")
                        .param("tipoNecesidad", "RECURRENTE").param("periodo", "P1M")
                        .param("descripcion", "Fideos secos").param("cantidad", "50")
                        .param("subCategoriaId", "5").param("unidadMedida", "KILOGRAMO").param("tipoBien", "PERECEDERO"))
                .andExpect(status().is3xxRedirection());
        verify(donaciones).crearNecesidad(argThat(r -> Long.valueOf(3L).equals(r.entidadBeneficiariaId())));
        verify(donaciones, never()).crearNecesidad(argThat(r -> Long.valueOf(99L).equals(r.entidadBeneficiariaId())));
    }

    @Test void noSePuedeBorrarUnaNecesidadDeOtraEntidad() throws Exception {
        when(donaciones.necesidad(5L)).thenReturn(new ApiDtos.Necesidad(5L, 9L, null, "Ajena", "RECURRENTE", "P1M",
                0f, 10f, "PENDIENTE"));
        mvc.perform(post("/entidad/dashboard/necesidades/5/eliminar").session(TestSesiones.entidad(3L)))
                .andExpect(status().isForbidden());
        verify(donaciones, never()).eliminarNecesidad(anyLong());
    }

    @Test void borraLaNecesidadPropia() throws Exception {
        when(donaciones.necesidad(6L)).thenReturn(new ApiDtos.Necesidad(6L, 3L, null, "Propia", "RECURRENTE", "P1M",
                0f, 10f, "PENDIENTE"));
        mvc.perform(post("/entidad/dashboard/necesidades/6/eliminar").session(TestSesiones.entidad(3L)))
                .andExpect(status().is3xxRedirection());
        verify(donaciones).eliminarNecesidad(6L);
    }

    @Test void confirmacionRecibidaArmaElPatchConLaEntregaYLasFotos() throws Exception {
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("entregaId", "7").param("resultado", "RECIBIDA")
                        .param("fotos", "https://ejemplo.org/1.jpg\n\nhttps://ejemplo.org/2.jpg"))
                .andExpect(status().is3xxRedirection());
        verify(logistica).actualizarEstadoEntrega(eq(7L), argThat(b -> "ENTREGADA".equals(b.estado())
                && b.fotos().equals(List.of("https://ejemplo.org/1.jpg", "https://ejemplo.org/2.jpg"))));
    }

    @Test void confirmacionSinFotosNoLlegaALaApi() throws Exception {
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("entregaId", "7").param("resultado", "RECIBIDA").param("fotos", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("apiError", containsString("al menos una foto")));
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
    }

    @Test void noRecibidaExigeJustificacionYEnviaNoRecibida() throws Exception {
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("entregaId", "8").param("resultado", "NO_RECIBIDA").param("justificacion", " "))
                .andExpect(flash().attribute("apiError", containsString("Explicá")));
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("entregaId", "8").param("resultado", "NO_RECIBIDA").param("justificacion", "Llegó dañada"))
                .andExpect(status().is3xxRedirection());
        verify(logistica).actualizarEstadoEntrega(eq(8L), argThat(b -> "NO_RECIBIDA".equals(b.estado())
                && "Llegó dañada".equals(b.justificacion())));
    }

    @Test void resumenNoListaBeneficiariosNiMuestraDatosDeMaqueta() throws Exception {
        mvc.perform(get("/entidad/dashboard").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsStringIgnoringCase("unsplash"))))
                .andExpect(content().string(not(containsString("CUIT-"))))
                .andExpect(content().string(not(containsString("Chofer Asignado"))));
        verify(donaciones, never()).beneficiarios();
        verify(donaciones, never()).donaciones();
    }
}

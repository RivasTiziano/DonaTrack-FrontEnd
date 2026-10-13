package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse;
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
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Ranking con nombres (fase 8.2): el nombre sale de Donaciones y, si no aparece, se muestra el id. */
@SpringBootTest
@AutoConfigureMockMvc
class RankingNombresMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean IncentivosApiService incentivos;
    @MockBean AutenticacionApiService auth;
    @MockBean LogisticaApiService logistica;
    @MockBean NotificacionesApiService notificaciones;

    @Test void rankingMuestraNombreYDonanteSinNombreComoId() throws Exception {
        when(incentivos.ranking()).thenReturn(List.of(new ApiDtos.Ranking(7L, 8), new ApiDtos.Ranking(99L, 3)));
        when(donaciones.listarHumanos()).thenReturn(List.of(new DonanteHumanoResponse(7L, "Ana", "Prueba", "DNI", "12345678", List.of(), "APROBADO")));
        when(donaciones.listarJuridicos()).thenReturn(List.of());
        mvc.perform(get("/admin/dashboard/rankings").session(TestSesiones.admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Prueba")))
                .andExpect(content().string(containsString("Donante #99")));
        verify(donaciones, times(1)).listarHumanos();
        verify(donaciones, times(1)).listarJuridicos();
    }
}

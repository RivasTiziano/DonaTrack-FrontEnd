package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiRequests;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.mappers.DonacionesMapper;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.DonacionesService;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import ar.edu.utn.donatrack.validators.DonacionesValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Publicación de entregas (admin): body del PATCH, llamada a la API y validación del largo antes del PATCH. */
class PublicacionAsignacionTest {
    private static final String BASE = "http://api.test";
    private final ObjectMapper json = new ObjectMapper();

    @Test void publicarEnviaSoloPublicableYLaDescripcion() throws Exception {
        assertEquals("{\"publicable\":true,\"descripcionPublica\":\"Fideos del comedor\"}",
                json.writeValueAsString(new ApiRequests.PublicacionAsignacion(true, "Fideos del comedor")));
    }

    @Test void despublicarOmiteLaDescripcionEnElBody() throws Exception {
        assertEquals("{\"publicable\":false}", json.writeValueAsString(new ApiRequests.PublicacionAsignacion(false, null)));
    }

    @Test void elPatchVaAlEndpointDeLaAsignacionConElBodyExacto() {
        var http = mock(WebApiCallerService.class);
        var respuesta = new DonacionesDetalles.Publicacion(5L, true, "Fideos del comedor", List.of("https://fotos.example/5.jpg"));
        when(http.patch(any(), any(), any())).thenReturn(respuesta);

        var resultado = new DonacionesApiService(http, BASE).publicacion(5L, true, "Fideos del comedor");

        verify(http).patch(BASE + "/asignaciones/5/publicacion",
                new ApiRequests.PublicacionAsignacion(true, "Fideos del comedor"), DonacionesDetalles.Publicacion.class);
        assertEquals(respuesta, resultado);
    }

    @Test void unaDescripcionDeMasDe500CaracteresSeRechazaSinLlamarALaApi() {
        var api = mock(DonacionesApiService.class);
        var service = new DonacionesService(api, mock(DonacionesMapper.class), mock(DonacionesValidator.class));
        String demasiado = "a".repeat(501);

        assertThrows(FormularioInvalidoException.class, () -> service.publicarAsignacion(5L, true, demasiado));
        verify(api, never()).publicacion(anyLong(), anyBoolean(), any());
    }

    @Test void unaDescripcionDe500CaracteresEsValidaYDespublicarIgnoraLaDescripcion() {
        var api = mock(DonacionesApiService.class);
        var service = new DonacionesService(api, mock(DonacionesMapper.class), mock(DonacionesValidator.class));

        service.publicarAsignacion(5L, true, "a".repeat(500));
        service.publicarAsignacion(6L, false, "texto que no debe viajar");

        verify(api).publicacion(5L, true, "a".repeat(500));
        verify(api).publicacion(6L, false, null);
    }

}

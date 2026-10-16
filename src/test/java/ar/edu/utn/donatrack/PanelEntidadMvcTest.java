package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.LogisticaDetalles;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Panel de la entidad: el dueño es siempre el de la sesión; confirmación de recepción por asignación. */
@SpringBootTest
@AutoConfigureMockMvc
class PanelEntidadMvcTest {
    @Autowired MockMvc mvc;
    @MockBean DonacionesApiService donaciones;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;

    private static final String SIN_PENDIENTE = "Esta entrega ya no está pendiente de confirmación.";

    private static ApiDtos.Entrega entregaEnTraslado(long id, long asignacionId, long entidadId) {
        return new ApiDtos.Entrega(id, asignacionId, "EN_TRASLADO", null, List.of(), null, null, entidadId, null, null, null);
    }

    private static ApiDtos.Asignacion asignacionEnTraslado(long id) {
        return new ApiDtos.Asignacion(id, 40L, 50L, 3L, 10f, "EN_TRASLADO", null, "Donación de prueba", null, null, null, null);
    }

    @Test void todasLasPantallasCompartenSidebarSinHeaderYConCierreDeSesion() throws Exception {
        for (String path : List.of("", "/necesidades", "/donaciones", "/entregas", "/confirmar")) {
            mvc.perform(get("/entidad/dashboard" + path).session(TestSesiones.entidad(3L)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("beneficiary-shell")))
                    .andExpect(content().string(containsString("admin-sidebar-collapse")))
                    .andExpect(content().string(containsString("sidebar-logout")))
                    .andExpect(content().string(not(containsString("<header class=\"dashboard-header\""))))
                    .andExpect(content().string(not(containsString("class=\"onboarding-hint "))));
        }
    }

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

    @Test void confirmarUsaUnaSolaConsultaAEntregasEnTraslado() throws Exception {
        when(donaciones.asignacionesEntidad(3L, "EN_TRASLADO")).thenReturn(List.of(asignacionEnTraslado(5L)));
        when(logistica.entregas(3L, null, null, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(7L, 5L, 3L)));
        mvc.perform(get("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"asignacionId\"")))
                .andExpect(content().string(containsString("value=\"5\"")))
                .andExpect(content().string(not(containsString("Número de entrega"))));
        verify(logistica, times(1)).entregas(3L, null, null, "EN_TRASLADO");
        verify(logistica, never()).entregas();
    }

    @Test void sinEntregaEnTrasladoLaFilaNoMuestraAcciones() throws Exception {
        when(donaciones.asignacionesEntidad(3L, "EN_TRASLADO")).thenReturn(List.of(asignacionEnTraslado(5L)));
        when(logistica.entregas(3L, null, null, "EN_TRASLADO")).thenReturn(List.of());
        mvc.perform(get("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("todavía no está disponible para confirmar")))
                .andExpect(content().string(not(containsString("name=\"resultado\""))));
    }

    @Test void recibidaArmaElPatchConLaEntregaDelServidorYLasFotos() throws Exception {
        when(logistica.entregas(3L, null, 5L, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(7L, 5L, 3L)));
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("resultado", "RECIBIDA")
                        .param("fotos", "https://ejemplo.org/1.jpg\n\nhttps://ejemplo.org/2.jpg"))
                .andExpect(status().is3xxRedirection());
        verify(logistica).actualizarEstadoEntrega(eq(7L), argThat(b -> "ENTREGADA".equals(b.estado())
                && b.fotos().equals(List.of("https://ejemplo.org/1.jpg", "https://ejemplo.org/2.jpg"))));
    }

    @Test void entregaIdQueLlegaEnElFormularioSeIgnora() throws Exception {
        when(logistica.entregas(3L, null, 5L, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(7L, 5L, 3L)));
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("entregaId", "999").param("resultado", "RECIBIDA")
                        .param("fotos", "https://ejemplo.org/1.jpg"))
                .andExpect(status().is3xxRedirection());
        verify(logistica).actualizarEstadoEntrega(eq(7L), any());
        verify(logistica, never()).actualizarEstadoEntrega(eq(999L), any());
    }

    @Test void recibidaSinFotosNoLlegaALaApi() throws Exception {
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("resultado", "RECIBIDA").param("fotos", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("apiError", containsString("al menos una foto")));
        verify(logistica, never()).entregas(anyLong(), any(), any(), any());
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
    }

    @Test void sinAsignacionNoLlegaALaApi() throws Exception {
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("resultado", "RECIBIDA").param("fotos", "https://ejemplo.org/1.jpg"))
                .andExpect(flash().attribute("apiError", containsString("la donación a confirmar")));
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
    }

    @Test void noRecibidaExigeJustificacionYEnviaNoRecibida() throws Exception {
        when(logistica.entregas(3L, null, 8L, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(18L, 8L, 3L)));
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "8").param("resultado", "NO_RECIBIDA").param("justificacion", " "))
                .andExpect(flash().attribute("apiError", containsString("Explicá")));
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "8").param("resultado", "NO_RECIBIDA").param("justificacion", "Llegó dañada"))
                .andExpect(status().is3xxRedirection());
        verify(logistica).actualizarEstadoEntrega(eq(18L), argThat(b -> "NO_RECIBIDA".equals(b.estado())
                && "Llegó dañada".equals(b.justificacion())));
    }

    @Test void asignacionSinEntregaEnTrasladoNoHacePatch() throws Exception {
        when(logistica.entregas(3L, null, 5L, "EN_TRASLADO")).thenReturn(List.of());
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("resultado", "RECIBIDA")
                        .param("fotos", "https://ejemplo.org/1.jpg"))
                .andExpect(flash().attribute("apiError", SIN_PENDIENTE));
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
    }

    @Test void entregaAjenaNoHacePatch() throws Exception {
        when(logistica.entregas(3L, null, 5L, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(7L, 5L, 9L)));
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("resultado", "NO_RECIBIDA")
                        .param("justificacion", "Llegó dañada"))
                .andExpect(flash().attribute("apiError", SIN_PENDIENTE));
        verify(logistica, never()).actualizarEstadoEntrega(anyLong(), any());
    }

    @Test void forbiddenDelBackSeTraduce() throws Exception {
        when(logistica.entregas(3L, null, 5L, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(7L, 5L, 3L)));
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(logistica).actualizarEstadoEntrega(eq(7L), any());
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("resultado", "RECIBIDA")
                        .param("fotos", "https://ejemplo.org/1.jpg"))
                .andExpect(flash().attribute("apiError", "No podés confirmar esta entrega."));
    }

    @Test void transicionInvalidaDelBackSeTraduce() throws Exception {
        when(logistica.entregas(3L, null, 5L, "EN_TRASLADO")).thenReturn(List.of(entregaEnTraslado(7L, 5L, 3L)));
        var cuerpo = "{\"message\":\"Transición de estado inválida\"}".getBytes(StandardCharsets.UTF_8);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, cuerpo, StandardCharsets.UTF_8))
                .when(logistica).actualizarEstadoEntrega(eq(7L), any());
        mvc.perform(post("/entidad/dashboard/confirmar").session(TestSesiones.entidad(3L))
                        .param("asignacionId", "5").param("resultado", "RECIBIDA")
                        .param("fotos", "https://ejemplo.org/1.jpg"))
                .andExpect(flash().attribute("apiError", "Esta entrega ya no está en traslado; actualizá la página."));
    }

    @Test void necesidadesSeConsultanConLaEntidadDeLaSesion() throws Exception {
        when(donaciones.necesidades(3L, null)).thenReturn(List.of(new ApiDtos.Necesidad(5L, 3L, null, "Fideos del comedor",
                "RECURRENTE", "P1M", 0f, 10f, "PENDIENTE")));
        mvc.perform(get("/entidad/dashboard/necesidades").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fideos del comedor")));
        verify(donaciones).necesidades(3L, null);
        verify(donaciones, never()).necesidades();
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

    // ---------- Entregas en camino (req. 14) ----------

    private static LogisticaDetalles.SeguimientoEntrega envioEnCamino(long entregaId, long asignacionId, LogisticaDetalles.CamionPosicion camion) {
        return new LogisticaDetalles.SeguimientoEntrega(entregaId, asignacionId, "EN_TRASLADO",
                new LogisticaDetalles.Destino("La Plata", "Buenos Aires", -34.92, -57.95), camion);
    }

    @Test void entregasUsanElSeguimientoYUnaSolaConsultaDeAsignaciones() throws Exception {
        var camion = new LogisticaDetalles.CamionPosicion("AB123CD", -34.9, -57.9, LocalDateTime.of(2026, 10, 13, 10, 30));
        when(logistica.seguimiento()).thenReturn(List.of(envioEnCamino(7L, 5L, camion)));
        when(donaciones.asignacionesEntidad(3L, "")).thenReturn(List.of(asignacionEnTraslado(5L)));
        mvc.perform(get("/entidad/dashboard/entregas").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AB123CD")))
                .andExpect(content().string(containsString("Donación de prueba")))
                .andExpect(content().string(containsString("10:30")));
        verify(logistica, times(1)).seguimiento();
        verify(donaciones, times(1)).asignacionesEntidad(3L, "");
        verify(logistica, never()).ubicaciones();
        verify(logistica, never()).ubicacion(anyString());
    }

    @Test void entidadSinCamionMuestraSinPosicionTodavia() throws Exception {
        when(logistica.seguimiento()).thenReturn(List.of(envioEnCamino(7L, 5L, null)));
        when(donaciones.asignacionesEntidad(3L, "")).thenReturn(List.of(asignacionEnTraslado(5L)));
        mvc.perform(get("/entidad/dashboard/entregas").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sin posición todavía")));
    }

    @Test void entidadSinEnviosEnCaminoMuestraEstadoVacio() throws Exception {
        when(logistica.seguimiento()).thenReturn(List.of());
        mvc.perform(get("/entidad/dashboard/entregas").session(TestSesiones.entidad(3L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay envíos en camino a tu sede.")));
    }
}

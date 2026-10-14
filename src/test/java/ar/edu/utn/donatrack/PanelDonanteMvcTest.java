package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.dto.LogisticaDetalles;
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
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private static ApiDtos.Donacion donacion(long id) {
        return new ApiDtos.Donacion(id, 7L, "Donación " + id, null, "EN_DEPOSITO", LocalDateTime.of(2026, 1, (int) id, 10, 0));
    }

    /** Donación de 120 kg con un Bien (cantidad total 120, disponible según el parámetro). */
    private static ApiDtos.Donacion donacionConBien(long id, String estado, float cantidadDisponible) {
        var bien = new ApiDtos.Bien(1L, "Fideos y arroz", null, 120f, cantidadDisponible, null, "KILOGRAMO", "DURABLE", null, false);
        return new ApiDtos.Donacion(id, 7L, "Donación " + id, bien, estado, LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    private static ApiDtos.Asignacion asignacion(long id, long donacionId, long entidadId, String estado) {
        return asignacion(id, donacionId, entidadId, estado, LocalDateTime.of(2026, 2, 1, 10, 0));
    }

    private static ApiDtos.Asignacion asignacion(long id, long donacionId, long entidadId, String estado, LocalDateTime fecha) {
        return new ApiDtos.Asignacion(id, donacionId, 50L, entidadId, 5f, estado, null, "Donación", null,
                fecha, null, null);
    }

    private static ApiDtos.Asignacion asignacion(long id, long donacionId, long entidadId, String estado, float cantidad,
                                                 LocalDateTime fechaAsignacion, LocalDateTime fechaEntrega) {
        return new ApiDtos.Asignacion(id, donacionId, 50L, entidadId, cantidad, estado, null, "Donación", null,
                fechaAsignacion, fechaEntrega, null);
    }

    private static ApiDtos.Beneficiario fundacionVida() {
        return new ApiDtos.Beneficiario(3L, "Fundación Vida", "ONG", "Comedor", true, null,
                new MedioDeContactoDto("EMAIL", "vida@example.org"), List.of());
    }

    @Test void perfilSaleDeLaSesionYLasDonacionesSeConsultanConElDonanteDeLaSesion() throws Exception {
        when(donaciones.humano(7L)).thenReturn(ana());
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Prueba")))
                .andExpect(content().string(containsString("No tenés donaciones con esos filtros.")));
        verify(donaciones).donaciones(7L, null, null, null);
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

    @Test void directorioDelDonanteHaceUnaSolaLlamadaANecesidadesYAgrupaPorEntidad() throws Exception {
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        when(donaciones.necesidades()).thenReturn(List.of(new ApiDtos.Necesidad(5L, 3L, null, "Fideos del comedor",
                "RECURRENTE", "P1M", 0f, 10f, "PENDIENTE")));
        mvc.perform(get("/donante/dashboard/entidades").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fideos del comedor")));
        verify(donaciones, times(1)).necesidades();
        verify(donaciones, never()).necesidades(anyLong(), any());
    }

    @Test void categoriaYSubcategoriaSeFiltranEnElBackendConElDonanteDeLaSesion() throws Exception {
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L))
                        .param("estado", "EN_DEPOSITO").param("categoriaId", "2").param("subcategoriaId", "5")
                        .param("donanteId", "99"))
                .andExpect(status().isOk());
        verify(donaciones).donaciones(7L, null, 2L, 5L);
        verify(donaciones, never()).donaciones(eq(99L), any(), any(), any());
    }

    @Test void filtroEstadoUsaElEstadoEfectivoEnMemoria() throws Exception {
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(donacion(1L), donacion(2L), donacion(3L)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(asignacion(10L, 1L, 3L, "ENTREGADA"),
                asignacion(11L, 2L, 3L, "PENDIENTE_RUTA")));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)).param("estado", "ENTREGADA"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donación 1")))
                .andExpect(content().string(not(containsString("Donación 2"))))
                .andExpect(content().string(not(containsString("Donación 3"))));
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)).param("estado", "ASIGNACION_REALIZADA"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donación 2")))
                .andExpect(content().string(not(containsString("Donación 1"))))
                .andExpect(content().string(not(containsString("Donación 3"))));
        verify(donaciones, times(2)).donaciones(7L, null, null, null);
    }

    @Test void filtroAceptaLosSieteEstadosDeE2() throws Exception {
        for (String estado : List.of("LISTA_PARA_ENTREGAR", "EN_TRASLADO", "ENTREGA_FALLIDA")) {
            mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)).param("estado", estado))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("no son válidos"))));
        }
    }

    @Test void estadoEIdsInvalidosSeIgnoranConAviso() throws Exception {
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L))
                        .param("estado", "INVENTADO").param("categoriaId", "abc").param("subcategoriaId", "-4"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no son válidos: estado, categoría, subcategoría.")));
        verify(donaciones).donaciones(7L, null, null, null);
    }

    @Test void historialUsaUnaSolaConsultaDeAsignacionesYUnaDeBeneficiarios() throws Exception {
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(donacion(1L), donacion(2L)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(asignacion(10L, 1L, 3L, "ASIGNACION_REALIZADA")));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fundación Vida")));
        verify(donaciones, times(1)).asignacionesDonante(7L);
        verify(donaciones, times(1)).beneficiarios();
        verify(donaciones, never()).asignaciones(anyLong());
    }

    // Donación de 120 kg (50 sin asignar) con partes: 30 entregadas, 20 en traslado, 20 pendientes de ruta.
    private void prepararDonacionConDesglose() {
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(donacionConBien(1L, "EN_DEPOSITO", 50f)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(
                asignacion(10L, 1L, 3L, "ENTREGADA", 30f, LocalDateTime.of(2026, 2, 1, 10, 0), LocalDateTime.of(2026, 2, 5, 10, 0)),
                asignacion(11L, 1L, 3L, "EN_TRASLADO", 20f, LocalDateTime.of(2026, 2, 3, 10, 0), null),
                asignacion(12L, 1L, 3L, "PENDIENTE_RUTA", 20f, LocalDateTime.of(2026, 2, 4, 10, 0), null)));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
    }

    /** Solo la grilla de tarjetas: el modal y el select de filtros también traen etiquetas de estado. */
    private static String grilla(String html) {
        int inicio = html.indexOf("id=\"donations-grid\"");
        int fin = html.indexOf("id=\"modal-donation-detail\"", inicio);
        assertTrue(inicio >= 0 && fin > inicio, "Debe existir la grilla de donaciones");
        return html.substring(inicio, fin);
    }

    private String consultar(String estado) throws Exception {
        var req = get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L));
        if (estado != null) req = req.param("estado", estado);
        return grilla(mvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    @Test void etiquetaIgualAlNombreDelFiltro() throws Exception {
        prepararDonacionConDesglose();
        String sinFiltro = consultar(null);
        assertTrue(sinFiltro.contains(">Asignación realizada</span>"), "PENDIENTE_RUTA se muestra como Asignación realizada");
        assertTrue(sinFiltro.contains(">Entregada</span>"));
        assertTrue(sinFiltro.contains(">En traslado</span>"));
        assertTrue(sinFiltro.contains(">En depósito</span>"));
        assertTrue(!sinFiltro.contains(">Asignada</span>") && !sinFiltro.contains("Pendiente de ruta"));

        String pagina = mvc.perform(get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        for (String etiqueta : List.of("En depósito", "Asignación realizada", "Lista para entregar", "En traslado",
                "Entregada", "Entrega fallida", "Vencida")) {
            assertTrue(pagina.contains(">" + etiqueta + "</option>"), "Falta la opción " + etiqueta);
        }
    }

    @Test void desgloseConCantidadesYSinAsignar() throws Exception {
        prepararDonacionConDesglose();
        String sinFiltro = consultar(null);
        assertTrue(sinFiltro.contains("120 kilogramo"));
        assertTrue(sinFiltro.contains("Sin asignar:") && sinFiltro.contains("50 kilogramo"));
        assertTrue(sinFiltro.contains("30 kilogramo"));
        assertTrue(sinFiltro.contains("Fundación Vida"));
        assertTrue(sinFiltro.contains("Asignaciones (3)"));
        assertEquals(3, sinFiltro.split("class=\"asignacion-fila\"", -1).length - 1);
        assertTrue(!sinFiltro.contains("open=\"open\""), "Sin filtro, el desglose va plegado");
        verify(donaciones, times(1)).asignacionesDonante(7L);
        verify(donaciones, never()).asignaciones(anyLong());
    }

    @Test void filtroEntregadaMuestraSoloAsignacionesEntregadas() throws Exception {
        prepararDonacionConDesglose();
        String entregadas = consultar("ENTREGADA");
        assertTrue(entregadas.contains("Asignaciones (1)"));
        assertTrue(entregadas.contains("open=\"open\""), "Con filtro de asignación, el desglose va desplegado");
        assertTrue(entregadas.contains(">Entregada</span>"));
        assertTrue(entregadas.contains("Entrega: 05/02/2026"));
        assertEquals(1, entregadas.split("class=\"asignacion-fila\"", -1).length - 1);
        assertTrue(!entregadas.contains(">En traslado</span>") && !entregadas.contains("Sin asignar:"));
        verify(donaciones, times(1)).asignacionesDonante(7L);
        verify(donaciones, never()).asignaciones(anyLong());
    }

    /** Solo el cuerpo del modal de trazabilidad (renderizado en el servidor, un bloque por donación). */
    private String modal(String estado) throws Exception {
        var req = get("/donante/dashboard/donaciones").session(TestSesiones.donante(7L));
        if (estado != null) req = req.param("estado", estado);
        String html = mvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int inicio = html.indexOf("id=\"modal-donation-detail\"");
        int fin = html.indexOf("<script", inicio);
        assertTrue(inicio >= 0 && fin > inicio, "Debe existir el modal de trazabilidad");
        return html.substring(inicio, fin);
    }

    private static int contar(String html, String texto) { return html.split(java.util.regex.Pattern.quote(texto), -1).length - 1; }

    @Test void modalMuestraUnaSeccionPorAsignacionConSuEstadoYCantidad() throws Exception {
        prepararDonacionConDesglose();
        String m = modal(null);
        assertEquals(1, contar(m, "class=\"traza-donacion\""));
        assertEquals(3, contar(m, "class=\"traza-asignacion\""));
        assertTrue(m.contains("30 kilogramo → Fundación Vida"));
        assertTrue(m.contains("20 kilogramo → Fundación Vida"));
        assertTrue(m.contains(">Entregada</span>"));
        assertTrue(m.contains(">En traslado</span>"));
        assertTrue(m.contains(">Asignación realizada</span>"));
        assertTrue(m.contains("Recibida en depósito:") && m.contains("Sin asignar:") && m.contains("50 kilogramo"));
        assertTrue(m.contains("05/02/2026"), "Debe mostrarse la fecha de entrega");
        assertTrue(m.contains("01/02/2026"), "Debe mostrarse la fecha de asignación");
        assertEquals(3, contar(m, "class=\"dt-stepper\""));
        verify(donaciones, times(1)).asignacionesDonante(7L);
        verify(donaciones, never()).asignaciones(anyLong());
    }

    @Test void pasosDelModalUsanLasEtiquetasDelFiltro() throws Exception {
        prepararDonacionConDesglose();
        String m = modal(null);
        for (String label : List.of("En depósito", "Asignación realizada", "Lista para entregar", "En traslado", "Entregada")) {
            assertEquals(3, contar(m, "class=\"dt-step-label\">" + label + "</span>"), label);
        }
        assertTrue(!m.contains(">Asignada</span>") && !m.contains("Pendiente de ruta") && !m.contains("PENDIENTE_RUTA"));
    }

    @Test void entregaFallidaMuestraElPasoFallidoYLaJustificacion() throws Exception {
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(donacionConBien(1L, "EN_DEPOSITO", 0f)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(new ApiDtos.Asignacion(20L, 1L, 50L, 3L, 120f,
                "ENTREGA_FALLIDA", null, "Donación", null, LocalDateTime.of(2026, 2, 1, 10, 0), null,
                "Tocamos timbre pero nadie respondió")));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        String m = modal(null);
        assertTrue(m.contains("dt-step failed"));
        assertTrue(m.contains("class=\"dt-step-label\">Entrega fallida</span>"));
        assertTrue(!m.contains("class=\"dt-step-label\">Entregada</span>"));
        assertTrue(m.contains("Tocamos timbre pero nadie respondió"));
        assertEquals(4, contar(m, "dt-step completed"));
        assertTrue(!m.contains("Sin asignar:"));
    }

    @Test void modalConFiltroDeAsignacionMuestraSoloLasCoincidentes() throws Exception {
        prepararDonacionConDesglose();
        String m = modal("ENTREGADA");
        assertEquals(1, contar(m, "class=\"traza-asignacion\""));
        assertTrue(m.contains("30 kilogramo → Fundación Vida"));
        assertTrue(!m.contains("20 kilogramo → Fundación Vida"));
        String m2 = modal("EN_DEPOSITO");
        assertEquals(3, contar(m2, "class=\"traza-asignacion\""));
    }

    @Test void filtroEnDepositoPorCantidadLibre() throws Exception {
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(
                donacionConBien(1L, "EN_DEPOSITO", 50f), donacionConBien(2L, "ASIGNACION_REALIZADA", 0f)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(
                asignacion(10L, 1L, 3L, "ENTREGADA", 30f, LocalDateTime.of(2026, 2, 1, 10, 0), LocalDateTime.of(2026, 2, 5, 10, 0)),
                asignacion(11L, 2L, 3L, "ENTREGADA", 120f, LocalDateTime.of(2026, 2, 2, 10, 0), LocalDateTime.of(2026, 2, 6, 10, 0))));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        String enDeposito = consultar("EN_DEPOSITO");
        assertTrue(enDeposito.contains("data-id=\"1\""));
        assertTrue(!enDeposito.contains("data-id=\"2\""), "Sin cantidad libre no está en depósito");
        String entregada = consultar("ENTREGADA");
        assertTrue(entregada.contains("data-id=\"1\"") && entregada.contains("data-id=\"2\""));
        verify(donaciones, times(2)).asignacionesDonante(7L);
        verify(donaciones, never()).asignaciones(anyLong());
    }

    @Test void filtroVencida() throws Exception {
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(donacionConBien(1L, "VENCIDA", 50f)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of());
        String vencidas = consultar("VENCIDA");
        assertTrue(vencidas.contains("data-id=\"1\"") && vencidas.contains(">Vencida</span>"));
        assertTrue(!consultar("EN_DEPOSITO").contains("data-id=\"1\""), "Una donación vencida no está en depósito");
        verify(donaciones, times(2)).asignacionesDonante(7L);
        verify(donaciones, never()).asignaciones(anyLong());
    }

    @Test void resumenLimitaACincoDonaciones() throws Exception {
        when(donaciones.humano(7L)).thenReturn(ana());
        when(donaciones.donaciones(7L, null, null, null)).thenReturn(List.of(donacion(1L), donacion(2L), donacion(3L),
                donacion(4L), donacion(5L), donacion(6L)));
        mvc.perform(get("/donante/dashboard").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userDonations", hasSize(5)));
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

    @Test void escalafonSigueLaCadenaDeCategoriaSiguiente() throws Exception {
        when(incentivos.metricas(7L)).thenReturn(new IncentivosDtos.Metricas(3, 30, 2, "Sostenedor", List.of()));
        when(incentivos.categorias()).thenReturn(List.of(new IncentivosDtos.Categoria(1L, "Transformador", null, List.of()),
                new IncentivosDtos.Categoria(2L, "Sostenedor", 1L, List.of()),
                new IncentivosDtos.Categoria(3L, "Colaborador", 2L, List.of())));
        var resultado = mvc.perform(get("/donante/dashboard/incentivos").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andReturn();
        String body = resultado.getResponse().getContentAsString();
        int inicio = body.indexOf("Camino de Categor");
        int colaborador = body.indexOf(">Colaborador</h3>", inicio);
        int sostenedor = body.indexOf(">Sostenedor</h3>", inicio);
        int transformador = body.indexOf(">Transformador</h3>", inicio);
        assertTrue(inicio >= 0 && colaborador > inicio, "Colaborador debe estar en el escalafón");
        assertTrue(colaborador < sostenedor && sostenedor < transformador, "El orden debe seguir categoriaSiguienteId");
        assertTrue(!body.contains("Bronce") && !body.contains("Platino"), "No deben quedar las categorías viejas");
    }

    @Test void caidaDelBackendDegradaSinRomperLaPagina() throws Exception {
        when(incentivos.metricas(7L)).thenThrow(new ResourceAccessException("offline"));
        when(donaciones.humano(7L)).thenThrow(new ResourceAccessException("offline"));
        mvc.perform(get("/donante/dashboard").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk());
    }

    // ---------- Entregas en camino (req. 9) ----------

    private static LogisticaDetalles.SeguimientoEntrega seguimiento(long entregaId, long asignacionId, LogisticaDetalles.CamionPosicion camion) {
        return new LogisticaDetalles.SeguimientoEntrega(entregaId, asignacionId, "EN_TRASLADO",
                new LogisticaDetalles.Destino("La Plata", "Buenos Aires", -34.92, -57.95), camion);
    }

    @Test void entregasUsanUnaSolaConsultaAlSeguimientoYNoConsultanUbicaciones() throws Exception {
        when(logistica.seguimiento()).thenReturn(List.of(seguimiento(15L, 5L, null), seguimiento(16L, 6L, null)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(asignacion(5L, 40L, 3L, "EN_TRASLADO"),
                asignacion(6L, 41L, 3L, "EN_TRASLADO")));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        mvc.perform(get("/donante/dashboard/entregas").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fundación Vida")));
        verify(logistica, times(1)).seguimiento();
        verify(donaciones, times(1)).asignacionesDonante(7L);
        verify(donaciones, times(1)).beneficiarios();
        verify(logistica, never()).ubicaciones();
        verify(logistica, never()).ubicacion(anyString());
    }

    @Test void conCamionMuestraPatenteYUltimaActualizacion() throws Exception {
        var camion = new LogisticaDetalles.CamionPosicion("AB123CD", -34.9, -57.9, LocalDateTime.of(2026, 10, 13, 10, 30));
        when(logistica.seguimiento()).thenReturn(List.of(seguimiento(15L, 5L, camion)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(asignacion(5L, 40L, 3L, "EN_TRASLADO")));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        mvc.perform(get("/donante/dashboard/entregas").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AB123CD")))
                .andExpect(content().string(containsString("Última actualización")))
                .andExpect(content().string(containsString("10:30")));
    }

    @Test void sinCamionMuestraSinPosicionTodavia() throws Exception {
        when(logistica.seguimiento()).thenReturn(List.of(seguimiento(15L, 5L, null)));
        when(donaciones.asignacionesDonante(7L)).thenReturn(List.of(asignacion(5L, 40L, 3L, "EN_TRASLADO")));
        when(donaciones.beneficiarios()).thenReturn(List.of(fundacionVida()));
        mvc.perform(get("/donante/dashboard/entregas").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sin posición todavía")))
                .andExpect(content().string(containsString("Fundación Vida")));
    }

    @Test void sinEntregasEnCaminoMuestraEstadoVacioSinConsultarAsignaciones() throws Exception {
        when(logistica.seguimiento()).thenReturn(List.of());
        mvc.perform(get("/donante/dashboard/entregas").session(TestSesiones.donante(7L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No tenés entregas en camino en este momento.")));
        verify(donaciones, never()).asignacionesDonante(anyLong());
    }
}

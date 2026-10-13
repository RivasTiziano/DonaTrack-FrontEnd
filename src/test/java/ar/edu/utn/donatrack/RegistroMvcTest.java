package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.RegistroDtos;
import ar.edu.utn.donatrack.services.AutenticacionApiService;
import ar.edu.utn.donatrack.services.DonacionesApiService;
import ar.edu.utn.donatrack.services.IncentivosApiService;
import ar.edu.utn.donatrack.services.LogisticaApiService;
import ar.edu.utn.donatrack.services.NotificacionesApiService;
import ar.edu.utn.donatrack.services.RegistroApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Registro público: redirige al login tras el 201 sin iniciar sesión, muestra los errores del backend y no llama a la API con datos inválidos. */
@SpringBootTest
@AutoConfigureMockMvc
class RegistroMvcTest {
    @Autowired MockMvc mvc;
    @MockBean RegistroApiService registro;
    @MockBean AutenticacionApiService autenticacion;
    @MockBean DonacionesApiService donaciones;
    @MockBean IncentivosApiService incentivos;
    @MockBean LogisticaApiService logistica;
    @MockBean NotificacionesApiService notificaciones;

    private static final String PASS = "clave-inicial-segura-2026";
    private static final String RUTA_HUMANO = "/registro/donante-humano";
    private static final String RUTA_JURIDICO = "/registro/donante-organizacion";
    private static final String RUTA_ENTIDAD = "/registro/entidad-beneficiaria";
    private static final String CUENTA_EXISTENTE = "{\"message\":\"Ya existe una cuenta con ese email\"}";

    private Map<String, String> datosHumano() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("nombre", "Ana"); d.put("apellido", "Prueba"); d.put("fechaNacimiento", "1990-05-17");
        d.put("documento", "12345678"); d.put("genero", "FEMENINO");
        d.put("calle", "Av. Corrientes"); d.put("numero", "1234"); d.put("ciudad", "CABA"); d.put("provincia", "Buenos Aires");
        d.put("email", "ana@example.com"); d.put("telefono", "+5491112345678"); d.put("whatsapp", "");
        d.put("predeterminado", "SMS");
        d.put("password", PASS); d.put("confirmacion", PASS);
        return d;
    }

    private Map<String, String> datosJuridico() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("documento", "30111222333"); d.put("razonSocial", "Arcos Plateados S.A."); d.put("tipoEntidad", "EMPRESA");
        d.put("rubro", "Alimentos"); d.put("calle", "Av. Libertador"); d.put("numero", "900"); d.put("ciudad", "CABA");
        d.put("provincia", "Buenos Aires"); d.put("email", "arcos@example.com"); d.put("telefono", ""); d.put("whatsapp", "");
        d.put("predeterminado", "EMAIL");
        d.put("representantes[0].nombre", "Juan"); d.put("representantes[0].apellido", "Gómez");
        d.put("representantes[0].documento", "20111222"); d.put("representantes[0].tipoDocumento", "DNI");
        d.put("representantes[0].fechaNacimiento", "1980-01-02"); d.put("representantes[0].genero", "MASCULINO");
        d.put("password", PASS); d.put("confirmacion", PASS);
        return d;
    }

    private Map<String, String> datosEntidad() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("razonSocial", "Comedor Sol"); d.put("tipoEntidad", "ONG"); d.put("rubro", "Comedor comunitario");
        d.put("calle", "Av. Rivadavia"); d.put("numero", "5678"); d.put("ciudad", "CABA"); d.put("provincia", "Buenos Aires");
        d.put("email", "sol@example.org");
        d.put("representantes[0].nombre", "Ana Referente"); d.put("representantes[0].telefono", "+5491112345678");
        d.put("password", PASS); d.put("confirmacion", PASS);
        return d;
    }

    private MockHttpServletRequestBuilder enviar(String ruta, Map<String, String> campos) {
        MockHttpServletRequestBuilder request = post(ruta);
        for (Map.Entry<String, String> campo : campos.entrySet()) request.param(campo.getKey(), campo.getValue());
        return request;
    }

    private static HttpClientErrorException error(HttpStatus estado, String cuerpo) {
        return HttpClientErrorException.create(estado, estado.getReasonPhrase(), HttpHeaders.EMPTY, cuerpo.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Test void humanoExitosoRedirigeAlLoginSinIniciarSesion() throws Exception {
        when(registro.registrarHumano(any())).thenReturn(new RegistroDtos.Registrado(7L, "DONANTE_HUMANO", "ana@example.com"));
        mvc.perform(enviar(RUTA_HUMANO, datosHumano()))
                .andExpect(redirectedUrl("/login?registrado"));
        verify(registro).registrarHumano(any());
        verifyNoInteractions(autenticacion);
    }

    @Test void juridicoExitosoRedirigeAlLoginSinIniciarSesion() throws Exception {
        when(registro.registrarJuridico(any())).thenReturn(new RegistroDtos.Registrado(9L, "DONANTE_JURIDICO", "arcos@example.com"));
        mvc.perform(enviar(RUTA_JURIDICO, datosJuridico()))
                .andExpect(redirectedUrl("/login?registrado"));
        verify(registro).registrarJuridico(any());
        verifyNoInteractions(autenticacion);
    }

    @Test void entidadExitosaRedirigeAlLoginSinIniciarSesion() throws Exception {
        when(registro.registrarEntidad(any())).thenReturn(new RegistroDtos.Registrado(3L, "BENEFICIARIA", "sol@example.org"));
        mvc.perform(enviar(RUTA_ENTIDAD, datosEntidad()))
                .andExpect(redirectedUrl("/login?registrado"));
        verify(registro).registrarEntidad(any());
        verifyNoInteractions(autenticacion);
    }

    @Test void conflictoDeEmailMuestraElLoginYLaAyudaSinMostrarLaContrasena() throws Exception {
        when(registro.registrarHumano(any())).thenThrow(error(HttpStatus.CONFLICT, CUENTA_EXISTENTE));
        mvc.perform(enviar(RUTA_HUMANO, datosHumano()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe una cuenta con ese email")))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("pedí tu acceso a la administración")))
                .andExpect(content().string(not(containsString(PASS))));
        verifyNoInteractions(autenticacion);
    }

    @Test void conflictoGenericoPideElAccesoAlAdmin() throws Exception {
        when(registro.registrarJuridico(any())).thenThrow(error(HttpStatus.CONFLICT, "{\"message\":\"Documento duplicado\"}"));
        mvc.perform(enviar(RUTA_JURIDICO, datosJuridico()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe un registro con esos datos")))
                .andExpect(content().string(containsString("pedí tu acceso a la administración")))
                .andExpect(content().string(not(containsString("Documento duplicado"))));
    }

    @Test void errorDeValidacionDelBackendMuestraSuMensaje() throws Exception {
        when(registro.registrarEntidad(any())).thenThrow(error(HttpStatus.BAD_REQUEST,
                "{\"message\":\"El email de la cuenta tiene que estar entre los medios de contacto del perfil\"}"));
        mvc.perform(enviar(RUTA_ENTIDAD, datosEntidad()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El email de la cuenta tiene que estar entre los medios de contacto del perfil")));
    }

    @Test void servicioNoDisponibleMuestraElMensajeGenerico() throws Exception {
        when(registro.registrarHumano(any())).thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE,
                "Unavailable", HttpHeaders.EMPTY, new byte[0], null));
        mvc.perform(enviar(RUTA_HUMANO, datosHumano()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No se pudo crear la cuenta. Probá de nuevo más tarde.")));
    }

    @Test void sinConexionMuestraElMensajeGenerico() throws Exception {
        when(registro.registrarJuridico(any())).thenThrow(new ResourceAccessException("connection refused"));
        mvc.perform(enviar(RUTA_JURIDICO, datosJuridico()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No se pudo crear la cuenta. Probá de nuevo más tarde.")));
    }

    @Test void contrasenaCortaNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosHumano();
        datos.put("password", "corta"); datos.put("confirmacion", "corta");
        mvc.perform(enviar(RUTA_HUMANO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La contraseña debe tener al menos 12 caracteres.")))
                .andExpect(content().string(not(containsString("value=\"corta\""))));
        verifyNoInteractions(registro);
    }

    @Test void confirmacionDistintaNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosHumano();
        datos.put("confirmacion", PASS + "x");
        mvc.perform(enviar(RUTA_HUMANO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Las contraseñas no coinciden.")));
        verifyNoInteractions(registro);
    }

    @Test void predeterminadoNoCargadoNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosHumano();
        datos.put("predeterminado", "WHATSAPP");
        datos.put("whatsapp", "");
        mvc.perform(enviar(RUTA_HUMANO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elegí como predeterminado un medio de contacto que hayas cargado.")));
        verifyNoInteractions(registro);
    }

    @Test void juridicoSinRepresentanteNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosJuridico();
        datos.keySet().removeIf(clave -> clave.startsWith("representantes"));
        mvc.perform(enviar(RUTA_JURIDICO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cargá al menos una persona representante con todos sus datos.")));
        verifyNoInteractions(registro);
    }

    @Test void formularioDeOrganizacionMuestraUnRepresentanteObligatorio() throws Exception {
        mvc.perform(get(RUTA_JURIDICO))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"representantes[0].nombre\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("name=\"representantes[1].nombre\""))))
                .andExpect(content().string(containsString("data-registro-agregar")));
    }

    // Regla a: el contacto EMAIL es obligatorio en el registro de persona humana (el back lo exige).
    @Test void humanoSinEmailNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosHumano();
        datos.put("email", "");
        mvc.perform(enviar(RUTA_HUMANO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresá un email de contacto.")));
        verifyNoInteractions(registro);
    }

    // Regla b: el predeterminado tiene que ser un medio cargado; SMS sin teléfono no se envía.
    @Test void predeterminadoSmsSinTelefonoNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosHumano();
        datos.put("telefono", "");
        datos.put("predeterminado", "SMS");
        mvc.perform(enviar(RUTA_HUMANO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elegí como predeterminado un medio de contacto que hayas cargado.")));
        verifyNoInteractions(registro);
    }

    // Regla c: el tipo de documento de cada representante jurídico es obligatorio (DNI o CUIT).
    @Test void juridicoConRepresentanteSinTipoDocumentoNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosJuridico();
        datos.remove("representantes[0].tipoDocumento");
        mvc.perform(enviar(RUTA_JURIDICO, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Completá todos los datos de cada representante cargado.")));
        verifyNoInteractions(registro);
    }

    // Regla d: nombre y teléfono del representante de entidad son NOT NULL; sin teléfono no se llama a la API.
    @Test void entidadConRepresentanteSinTelefonoNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosEntidad();
        datos.put("representantes[0].telefono", "");
        mvc.perform(enviar(RUTA_ENTIDAD, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cada representante necesita nombre y teléfono.")));
        verifyNoInteractions(registro);
    }

    @Test void entidadSinRepresentanteNoLlamaALaApi() throws Exception {
        Map<String, String> datos = datosEntidad();
        datos.keySet().removeIf(clave -> clave.startsWith("representantes"));
        mvc.perform(enviar(RUTA_ENTIDAD, datos))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cargá al menos una persona representante con nombre y teléfono.")));
        verifyNoInteractions(registro);
    }

    @Test void entidadConNombreYTelefonoSinCorreoEnviaCorreosVacios() throws Exception {
        when(registro.registrarEntidad(any())).thenReturn(new RegistroDtos.Registrado(3L, "BENEFICIARIA", "sol@example.org"));
        mvc.perform(enviar(RUTA_ENTIDAD, datosEntidad()))
                .andExpect(redirectedUrl("/login?registrado"));
        verify(registro).registrarEntidad(argThat(r -> r.perfil().representantes().size() == 1
                && "Ana Referente".equals(r.perfil().representantes().get(0).nombre())
                && "+5491112345678".equals(r.perfil().representantes().get(0).telefono())
                && r.perfil().representantes().get(0).correos().isEmpty()));
    }
}

package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.RegistroDtos;
import ar.edu.utn.donatrack.forms.RegistroForm;
import ar.edu.utn.donatrack.forms.RegistroForm.Representante;
import ar.edu.utn.donatrack.mappers.RegistroMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Forma del JSON que recibe Donaciones (E1): {perfil, email, contrasenia}, teléfono como SMS y email de la cuenta igual al contacto EMAIL. */
class RegistroMapperTest {
    private static final String PASS = "clave-inicial-segura-2026";
    private final RegistroMapper mapper = new RegistroMapper();
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RegistroForm humano() {
        RegistroForm f = new RegistroForm();
        f.setNombre(" Ana "); f.setApellido("Prueba"); f.setFechaNacimiento("1990-05-17"); f.setDocumento("12345678");
        f.setGenero("FEMENINO"); f.setCalle("Av. Corrientes"); f.setNumero("1234"); f.setCiudad("CABA"); f.setProvincia("Buenos Aires");
        f.setEmail("  Ana@Example.com "); f.setTelefono("+5491112345678"); f.setWhatsapp("");
        f.setPredeterminado("SMS"); f.setPassword(PASS); f.setConfirmacion(PASS);
        return f;
    }

    private JsonNode aJson(Object cuerpo) throws Exception {
        return json.readTree(json.writeValueAsString(cuerpo));
    }

    private static Set<String> campos(JsonNode nodo) {
        List<String> nombres = new ArrayList<>();
        nodo.fieldNames().forEachRemaining(nombres::add);
        return new HashSet<>(nombres);
    }

    @Test void cuerpoDelHumanoTieneLaFormaDelContrato() throws Exception {
        JsonNode cuerpo = aJson(mapper.humano(humano()));
        assertEquals(Set.of("perfil", "email", "contrasenia"), campos(cuerpo));
        assertEquals(PASS, cuerpo.get("contrasenia").asText());
        JsonNode perfil = cuerpo.get("perfil");
        assertEquals(Set.of("nombre", "apellido", "fechaNacimiento", "numeroDocumento", "genero", "direccion",
                "mediosDeContacto", "medioDeContactoPredeterminado"), campos(perfil));
        assertEquals("1990-05-17", perfil.get("fechaNacimiento").asText());
        assertEquals("Ana", perfil.get("nombre").asText());
        assertEquals("FEMENINO", perfil.get("genero").asText());
        assertEquals("Av. Corrientes", perfil.get("direccion").get("calle").asText());
    }

    @Test void telefonoSeEnviaComoSms() throws Exception {
        JsonNode medios = aJson(mapper.humano(humano())).get("perfil").get("mediosDeContacto");
        assertEquals(2, medios.size());
        assertEquals("EMAIL", medios.get(0).get("tipoMedioContacto").asText());
        assertEquals("ana@example.com", medios.get(0).get("formaContacto").asText());
        assertEquals("SMS", medios.get(1).get("tipoMedioContacto").asText());
        assertEquals("+5491112345678", medios.get(1).get("formaContacto").asText());
        assertEquals("SMS", aJson(mapper.humano(humano())).get("perfil").get("medioDeContactoPredeterminado").get("tipoMedioContacto").asText());
    }

    @Test void emailDeLaCuentaEsElContactoEmailNormalizado() {
        RegistroDtos.RegistroHumano cuenta = mapper.humano(humano());
        assertEquals("ana@example.com", cuenta.email());
        assertEquals("ana@example.com", cuenta.perfil().mediosDeContacto().get(0).formaContacto());
    }

    @Test void whatsappVacioNoSeEnvia() {
        RegistroDtos.RegistroHumano cuenta = mapper.humano(humano());
        assertTrue(cuenta.perfil().mediosDeContacto().stream().noneMatch(m -> "WHATSAPP".equals(m.tipoMedioContacto())));
    }

    @Test void toStringNoMuestraLaContrasena() {
        RegistroDtos.RegistroHumano cuenta = mapper.humano(humano());
        assertFalse(cuenta.toString().contains(PASS));
        assertFalse(humano().toString().contains(PASS));
    }

    @Test void juridicoEnviaRepresentantesYOmiteBloquesVacios() throws Exception {
        RegistroForm f = humano();
        f.setDocumento("30111222333"); f.setRazonSocial("Arcos S.A."); f.setTipoEntidad("EMPRESA"); f.setRubro("Alimentos");
        Representante completo = new Representante();
        completo.setNombre("Juan"); completo.setApellido("Gómez"); completo.setDocumento("20111222");
        completo.setTipoDocumento("DNI"); completo.setFechaNacimiento("1980-01-02"); completo.setGenero("MASCULINO");
        f.setRepresentantes(new ArrayList<>(List.of(completo, new Representante())));
        JsonNode perfil = aJson(mapper.juridico(f)).get("perfil");
        assertEquals(1, perfil.get("representantesJuridicos").size());
        JsonNode rep = perfil.get("representantesJuridicos").get(0);
        assertEquals("Juan", rep.get("nombre").asText());
        assertEquals("DNI", rep.get("tipoDocumento").asText());
        assertEquals("1980-01-02", rep.get("fechaNacimiento").asText());
        assertEquals("EMPRESA", perfil.get("tipoEntidad").asText());
    }

    @Test void entidadEnviaSoloElContactoPredeterminadoYCorreosComoEmail() throws Exception {
        RegistroForm f = humano();
        f.setRazonSocial("Comedor Sol"); f.setTipoEntidad("ONG"); f.setRubro("Comedor");
        Representante r = new Representante();
        r.setNombre("Ana García"); r.setTelefono("+5491198765432"); r.setCorreo("Ana@Sol.org");
        f.setRepresentantes(new ArrayList<>(List.of(r)));
        RegistroDtos.RegistroEntidad cuenta = mapper.entidad(f);
        JsonNode cuerpo = aJson(cuenta);
        assertEquals(Set.of("perfil", "email", "contrasenia"), campos(cuerpo));
        JsonNode perfil = cuerpo.get("perfil");
        assertEquals("EMAIL", perfil.get("medioDeContactoPredeterminado").get("tipoMedioContacto").asText());
        assertEquals("ana@example.com", perfil.get("medioDeContactoPredeterminado").get("formaContacto").asText());
        JsonNode representante = perfil.get("representantes").get(0);
        assertEquals("+5491198765432", representante.get("telefono").asText());
        assertEquals("ana@sol.org", representante.get("correos").get(0).get("formaContacto").asText());
        assertEquals("ana@example.com", cuenta.email());
        assertFalse(cuenta.toString().contains(PASS));
    }
}

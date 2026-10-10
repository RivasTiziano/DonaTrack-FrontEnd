package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.services.*;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.assertj.core.api.Assertions.*;

/** Verifica HTTP/JSON contra un servidor local simulado; no modifica bases reales. */
class ApiCoberturaTest {
    HttpServer server;
    DonacionesApiService donaciones;
    LogisticaApiService logistica;
    IncentivosApiService incentivos;
    record Reply(int status, String type, String body) {}
    Map<String,Reply> routes = new ConcurrentHashMap<>();
    Map<String,String> requests = new ConcurrentHashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            String key = exchange.getRequestMethod() + " " + exchange.getRequestURI();
            requests.put(key,new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            Reply reply = routes.getOrDefault(key,new Reply(404,"application/json","{}"));
            byte[] body = reply.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type",reply.type());
            if (reply.status() == 204) exchange.sendResponseHeaders(204,-1);
            else {
                exchange.sendResponseHeaders(reply.status(),body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();
        var http = new WebApiCallerService(RestClient.builder());
        String url = "http://127.0.0.1:" + server.getAddress().getPort();
        donaciones = new DonacionesApiService(http,url);
        logistica = new LogisticaApiService(http,url);
        incentivos = new IncentivosApiService(http,url);
    }
    @AfterEach void stop() { server.stop(0); }
    void route(String method, String path, int status, String json) {
        routes.put(method + " " + path,new Reply(status,"application/json",json));
    }

    @Test void fullDonorReadsAndUpdatesPreserveCurrentContract() throws Exception {
        route("GET","/donantes/humanos/1",200,"""
            {"id":1,"nombre":"Ana","fechaNacimiento":"1990-01-01","genero":"OTRO",
            "direccion":{"calle":"Real","ciudad":"CABA","provincia":"BA","numero":"1"},
            "mediosDeContacto":[{"tipoMedioContacto":"EMAIL","formaContacto":"ana@example.com"}],
            "medioDeContactoPredeterminado":{"tipoMedioContacto":"EMAIL","formaContacto":"ana@example.com"}}
            """);
        route("GET","/donantes/juridicos/2",200,"""
            {"id":2,"razonSocial":"Empresa","personasRepresentantes":[{"id":9,"nombre":"Representante","tipoDocumento":"DNI"}]}
            """);
        var humano = donaciones.humano(1L);
        assertThat(humano.fechaNacimiento()).isEqualTo(LocalDate.of(1990,1,1));
        assertThat(humano.direccion().calle()).isEqualTo("Real");
        assertThat(donaciones.juridico(2L).personasRepresentantes().get(0).id()).isEqualTo(9L);
        route("PUT","/donantes/humanos/1",200,"{}");
        donaciones.actualizarHumano(1L,new DonanteRequests.Humano("Ana","Real",humano.fechaNacimiento(),"123",
                "OTRO",humano.direccion(),humano.mediosDeContacto(),humano.medioDeContactoPredeterminado()));
        assertThat(mapper.readTree(requests.get("PUT /donantes/humanos/1")).path("mediosDeContacto").get(0)
                .path("formaContacto").asText()).isEqualTo("ana@example.com");
        route("PUT","/donantes/juridicos/2",200,"{}");
        donaciones.actualizarJuridico(2L,new DonanteRequests.Juridico("301","Empresa","EMPRESA","Rubro",
                humano.direccion(),humano.mediosDeContacto(),humano.medioDeContactoPredeterminado(),
                donaciones.juridico(2L).personasRepresentantes()));
        assertThat(mapper.readTree(requests.get("PUT /donantes/juridicos/2")).path("representantesJuridicos").get(0).path("id").asLong()).isEqualTo(9);
    }

    @Test void beneficiaryCrudUsesDifferentCreateAndUpdatePayloads() throws Exception {
        route("GET","/beneficiarios/3",200,"{\"id\":3,\"activo\":true,\"representantes\":[{\"id\":8,\"nombre\":\"Referente\",\"telefono\":\"123\",\"correos\":[]}]}");
        var original = donaciones.beneficiario(3L);
        assertThat(original.representantes().get(0).id()).isEqualTo(8L);
        var email = new MedioDeContactoDto("EMAIL","entidad@example.com");
        route("POST","/beneficiarios",201,"{\"id\":3}");
        donaciones.crearBeneficiario(new DonacionesDetalles.CrearBeneficiario("Entidad","ONG","Alimentos",null,email,original.representantes()));
        assertThat(mapper.readTree(requests.get("POST /beneficiarios")).has("activo")).isFalse();
        route("PUT","/beneficiarios/3",200,"{}");
        donaciones.actualizarBeneficiario(3L,new DonacionesDetalles.ActualizarBeneficiario("Entidad","ONG","Alimentos",null,email,original.representantes(),false));
        assertThat(mapper.readTree(requests.get("PUT /beneficiarios/3")).path("activo").asBoolean()).isFalse();
        route("DELETE","/beneficiarios/3",204,"");
        donaciones.eliminarBeneficiario(3L);
        assertThat(requests).containsKey("DELETE /beneficiarios/3");
    }

    @Test void donationQueriesUseExactEndpointsAndOptionalFilters() {
        route("GET","/bienes/1",200,"{\"id\":1}");
        route("GET","/necesidades/2",200,"{\"necesidadId\":2}");
        route("GET","/asignaciones?entidadBeneficiariaId=3",200,"[]");
        route("GET","/asignaciones?entidadBeneficiariaId=3&estado=ENTREGADA",200,"[{\"id\":4,\"estado\":\"ENTREGADA\"}]");
        route("GET","/donaciones/1/contactos",200,"{\"donante\":null,\"entidadBeneficiaria\":null}");
        route("GET","/asignaciones/4/contactos",200,"{\"donante\":null,\"entidadBeneficiaria\":{\"destinatario\":{\"email\":\"entidad@example.com\"},\"medioContacto\":\"EMAIL\"}}");
        route("GET","/sugerencias",200,"[]");
        route("GET","/sugerencias/1",200,"{\"donacionId\":1,\"mejoresNecesidades\":[],\"tops\":[]}");
        routes.put("POST /evaluador/ejecutar",new Reply(200,"text/plain; charset=UTF-8","Evaluación ejecutada"));
        assertThat(donaciones.bien(1L).id()).isEqualTo(1);
        assertThat(donaciones.necesidad(2L).necesidadId()).isEqualTo(2);
        assertThat(donaciones.asignacionesEntidad(3L,"")).isEmpty();
        assertThat(donaciones.asignacionesEntidad(3L,"ENTREGADA").get(0).estado()).isEqualTo("ENTREGADA");
        assertThat(donaciones.contactosDonacion(1L).entidadBeneficiaria()).isNull();
        assertThat(donaciones.contactosAsignacion(4L).entidadBeneficiaria().destinatario().email()).isEqualTo("entidad@example.com");
        assertThat(donaciones.sugerencias()).isEmpty();
        assertThat(donaciones.sugerencia(1L).donacionId()).isEqualTo(1);
        assertThat(donaciones.evaluar()).isEqualTo("Evaluación ejecutada");
    }

    @Test void logisticsDriverPlanningDetailsAndGpsMatchBackend() throws Exception {
        route("GET","/camiones/1",200,"{\"id\":1,\"patente\":\"AA123BB\"}");
        route("GET","/entregas/2",200,"{\"id\":2,\"asignacionId\":9}");
        route("GET","/rutas/1",200,"{\"id\":1,\"paradas\":[{\"id\":3,\"orden\":1,\"direccion\":{\"calle\":\"Real\",\"numero\":\"123\"},\"entregas\":[]}]}");
        route("GET","/rutas/paradas",200,"[{\"id\":3}]");
        route("GET","/rutas/paradas/3",200,"{\"id\":3}");
        route("GET","/camiones/ubicaciones",200,"[{\"patente\":\"AA123BB\",\"porcentajeAvance\":50,\"entregasPendientes\":1}]");
        route("GET","/camiones/AA123BB/ubicacion",200,"{\"patente\":\"AA123BB\",\"latitud\":-34.5}");
        route("POST","/rutas/1/iniciar",202,"{}");
        route("POST","/rutas/plan-route",202,"{\"cantidadEntregas\":2,\"cantidadLotes\":1,\"requestIds\":[\"req-1\"]}");
        assertThat(logistica.camion(1L).patente()).isEqualTo("AA123BB");
        assertThat(logistica.entrega(2L).asignacionId()).isEqualTo(9);
        assertThat(logistica.ruta(1L).paradas().get(0).direccion().calle()).isEqualTo("Real");
        assertThat(logistica.paradas().get(0).id()).isEqualTo(3);
        assertThat(logistica.parada(3L).id()).isEqualTo(3);
        assertThat(logistica.ubicaciones().get(0).porcentajeAvance()).isEqualTo(50);
        assertThat(logistica.ubicacion("AA123BB").latitud()).isEqualTo(-34.5);
        logistica.iniciarRuta(1L,"Chofer Real");
        assertThat(mapper.readTree(requests.get("POST /rutas/1/iniciar")).path("chofer").asText()).isEqualTo("Chofer Real");
        assertThat(logistica.planificar(LocalDate.of(2026,10,10)).requestIds()).containsExactly("req-1");
        assertThat(mapper.readTree(requests.get("POST /rutas/plan-route")).path("fecha").asText()).isEqualTo("2026-10-10");
    }

    @Test void incentiveReadsAndManagementUseActualJsonNames() throws Exception {
        route("GET","/incentivos/categorias",200,"[{\"id\":1,\"nombre\":\"Solidario\",\"misiones\":[{\"id\":2,\"nombre\":\"Racha\"}]}]");
        route("GET","/incentivos/categorias/1",200,"{\"id\":1}");
        route("GET","/incentivos/categorias/1/misiones",200,"[{\"id\":2,\"tipo\":\"RACHA\"}]");
        route("GET","/incentivos/donantes/1/metricas",200,"{\"totalDonacionesHistoricas\":3,\"desgloseMensual\":[]}");
        route("GET","/incentivos/donantes/1/misiones",200,"[{\"idMision\":2,\"nombreMision\":\"Racha\"}]");
        route("GET","/incentivos/donantes/1/insignias",200,"[{\"nombre\":\"Solidario\"}]");
        route("GET","/incentivos/donantes/1/historial-categorias",200,"[{\"id\":3,\"categoriaNueva\":\"Solidario\"}]");
        route("GET","/incentivos/donantes/1/progreso-mision-actual",200,"{\"mensaje\":\"Todas las misiones han sido completadas\"}");
        route("POST","/incentivos/categorias",201,"{\"id\":1}");
        route("POST","/incentivos/categorias/1/misiones",201,"{\"id\":2}");
        route("DELETE","/incentivos/categorias/1/misiones/2",204,"");
        route("PATCH","/incentivos/donantes/1/insignias/visibilidad",200,"{}");
        assertThat(incentivos.categorias().get(0).misiones().get(0).nombre()).isEqualTo("Racha");
        assertThat(incentivos.categoria(1L).id()).isEqualTo(1);
        assertThat(incentivos.misiones(1L).get(0).tipo()).isEqualTo("RACHA");
        assertThat(incentivos.metricas(1L).totalDonacionesHistoricas()).isEqualTo(3);
        assertThat(incentivos.misionesCompletadas(1L).get(0).idMision()).isEqualTo(2);
        assertThat(incentivos.insignias(1L).get(0).nombre()).isEqualTo("Solidario");
        assertThat(incentivos.historialCategorias(1L).get(0).categoriaNueva()).isEqualTo("Solidario");
        assertThat(incentivos.progreso(1L).idMision()).isNull();
        assertThat(incentivos.progreso(1L).mensaje()).contains("completadas");
        incentivos.crearCategoria(new IncentivosDtos.CategoriaRequest("Solidario",null,new IncentivosDtos.MisionRequest("RACHA",3)));
        assertThat(mapper.readTree(requests.get("POST /incentivos/categorias")).path("mision").path("valorObjetivo").asInt()).isEqualTo(3);
        incentivos.agregarMision(1L,new IncentivosDtos.MisionRequest("RACHA",3));
        incentivos.eliminarMision(1L,2L);
        incentivos.visibilidad(1L,false);
        assertThat(mapper.readTree(requests.get("PATCH /incentivos/donantes/1/insignias/visibilidad")).path("insigniasVisibles").asBoolean()).isFalse();
    }

    @Test void rankingHistoryReadsEntityContractNotMonthlyRankingDto() {
        route("GET","/incentivos/rankings/historial",200,"""
            [{"id":1,"periodo":"2026-10-01","posiciones":[{"posicion":1,"misionesCompletadas":2,
            "donante":{"idReferencia":9,"categoriaActual":null,"insigniasVisibles":true,"progresos":[]}}]}]
            """);
        var posicion = incentivos.historialRankings().get(0).posiciones().get(0);
        assertThat(posicion.donante().idReferencia()).isEqualTo(9);
        assertThat(posicion.misionesCompletadas()).isEqualTo(2);
    }
}

package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.EntregaMapaDto;
import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.net.http.HttpClient;
import java.util.List;

@Service
public class PublicoService {
 private static final ZoneId ZONA_PUBLICA = ZoneId.of("America/Argentina/Buenos_Aires");
 private final RestClient client;
 private final String url;
 public PublicoService(@Value("${apis.donaciones.url}") String url) {
   this.url=url.replaceAll("/+$","");
   var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
   factory.setReadTimeout(Duration.ofSeconds(5));
   this.client=RestClient.builder().requestFactory(factory).build();
 }
 /** Destacadas de la landing: entregadas en los últimos 30 días (desde = hoy − 30, hora de Buenos Aires). */
 public List<EntregaPublicaDto> donacionesDestacadas() {
   String desde = LocalDate.now(ZONA_PUBLICA).minusDays(30).toString();
   return leerLista(url+"/publico/entregas?desde={desde}", new ParameterizedTypeReference<List<EntregaPublicaDto>>(){}, desde);
 }
 public EntregaPublicaDto detalleEntrega(String id) {
   if(!id.matches("[1-9][0-9]*"))return null;
   try { return client.get().uri(url+"/publico/entregas/"+id).retrieve().body(EntregaPublicaDto.class); }
   catch(RestClientException e) { return null; }
 }
 /** Mapa: todas las entregadas cuya entidad tiene coordenadas. Sin límite de fecha. */
 public List<EntregaMapaDto> entregasEnMapa() {
   return leerLista(url+"/publico/entregas/mapa", new ParameterizedTypeReference<List<EntregaMapaDto>>(){});
 }
 /** Si la API no responde, la lista queda vacía y la página muestra su estado vacío. */
 private <T> List<T> leerLista(String uri, ParameterizedTypeReference<List<T>> tipo, Object... variables) {
   try { var result=client.get().uri(uri, variables).retrieve().body(tipo);
     return result == null ? List.of() : result;
   } catch(RestClientException e) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Public deliveries unavailable: {}",e.getMessage()); return List.of(); }
 }
}

package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.EntregaPublicaDto;
import ar.edu.utn.donatrack.dto.ApiDtos;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import java.time.Duration;
import java.net.http.HttpClient;
import java.util.List;

@Service
public class PublicoService {
 private final RestClient client;
 private final String url;
 public PublicoService(@Value("${apis.donaciones.url}") String url) {
   this.url=url.replaceAll("/+$","");
   var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
   factory.setReadTimeout(Duration.ofSeconds(5));
   this.client=RestClient.builder().requestFactory(factory).build();
 }
 public List<EntregaPublicaDto> entregasPublicas() {
   try { var result=client.get().uri(url+"/publico/entregas").retrieve().body(new ParameterizedTypeReference<List<EntregaPublicaDto>>(){});
     return result == null ? List.of() : result;
   } catch(RestClientException e) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Public deliveries unavailable: {}",e.getMessage()); return List.of(); }
 }
 public EntregaPublicaDto detalleEntrega(String id) {
   if(!id.matches("[1-9][0-9]*"))return null;
   try { return client.get().uri(url+"/publico/entregas/"+id).retrieve().body(EntregaPublicaDto.class); }
   catch(RestClientException e) { return null; }
 }
 /* Public coordinates are intentionally unavailable, including in the map. */
 public List<ApiDtos.Donacion> entregasEnMapa() { return List.of(); }
}

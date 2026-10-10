package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.ApiRequests;
import ar.edu.utn.donatrack.dto.DonanteRequests;
import ar.edu.utn.donatrack.dto.donante.DonanteHumanoResponse;
import ar.edu.utn.donatrack.dto.donante.DonanteJuridicoResponse;
import ar.edu.utn.donatrack.services.internal.WebApiCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** Cliente de toda la API del microservicio de Donaciones, organizado por recurso. */
@Service
public class DonacionesApiService {
    private final WebApiCallerService http;
    private final String url;

    // Constructor que recibe un servicio de llamadas HTTP y la URL base de la API.
    public DonacionesApiService(WebApiCallerService http, @Value("${apis.donaciones.url}") String url) {
        this.http = http;
        this.url = url.replaceAll("/+$", "");
    }

    // Donantes: consulta, registro, eliminación e importación.

    public List<DonanteHumanoResponse> listarHumanos() {
        return http.getList(url + "/donantes/humanos", DonanteHumanoResponse.class);
    }

    public List<DonanteJuridicoResponse> listarJuridicos() {
        return http.getList(url + "/donantes/juridicos", DonanteJuridicoResponse.class);
    }

    public void crearHumano(DonanteRequests.Humano request) {
        http.post(url + "/donantes/humanos", request, DonanteHumanoResponse.class);
    }

    public void crearJuridico(DonanteRequests.Juridico request) {
        http.post(url + "/donantes/juridicos", request, DonanteJuridicoResponse.class);
    }

    public void eliminarHumano(Long id) {
        http.delete(url + "/donantes/humanos/" + id);
    }

    public void eliminarJuridico(Long id) {
        http.delete(url + "/donantes/juridicos/" + id);
    }

    public ApiDtos.Importacion importar(MultipartFile file) {
        return http.upload(url + "/donantes/importar", file, ApiDtos.Importacion.class);
    }

    // Donaciones y asignaciones.

    public List<ApiDtos.Donacion> donaciones() {
        return http.getList(url + "/donaciones", ApiDtos.Donacion.class);
    }

    public ApiDtos.Donacion donacion(Long id) {
        return http.get(url + "/donaciones/" + id, ApiDtos.Donacion.class);
    }

    public List<ApiDtos.Asignacion> asignaciones(Long id) {
        return http.getList(url + "/donaciones/" + id + "/asignaciones", ApiDtos.Asignacion.class);
    }

    public void asignar(ApiRequests.Asignacion request) {
        http.post(url + "/asignaciones", request, Object.class);
    }

    public void crearDonacion(ApiRequests.Donacion request) {
        http.post(url + "/donaciones", request, new ParameterizedTypeReference<List<ApiDtos.Donacion>>() {});
    }

    public void estado(Long id, ApiRequests.EstadoDonacion request) {
        http.update(HttpMethod.PATCH, url + "/donaciones/" + id + "/estado", request);
    }

    public void eliminarDonacion(Long id) {
        http.delete(url + "/donaciones/" + id);
    }

    // Catálogo.

    public List<ApiDtos.Categoria> categorias() {
        return http.getList(url + "/catalogo/categorias", ApiDtos.Categoria.class);
    }

    public void crearCategoria(String nombre) {
        http.post(url + "/catalogo/categorias", new ApiRequests.Nombre(nombre), Object.class);
    }

    public void crearSubCategoria(Long id, String nombre) {
        http.post(url + "/catalogo/categorias/" + id + "/subcategorias", new ApiRequests.Nombre(nombre), Object.class);
    }

    // Bienes.

    public List<ApiDtos.Bien> bienes() {
        return http.getList(url + "/bienes", ApiDtos.Bien.class);
    }

    public void crearBien(ApiRequests.Bien request) {
        http.post(url + "/bienes", request, Object.class);
    }

    public void actualizarBien(Long id, ApiRequests.Bien request) {
        http.update(HttpMethod.PUT, url + "/bienes/" + id, request);
    }

    public void eliminarBien(Long id) {
        http.delete(url + "/bienes/" + id);
    }

    // Beneficiarios y necesidades.

    public List<ApiDtos.Beneficiario> beneficiarios() {
        return http.getList(url + "/beneficiarios", ApiDtos.Beneficiario.class);
    }

    public List<ApiDtos.Necesidad> necesidades() {
        return http.getList(url + "/necesidades", ApiDtos.Necesidad.class);
    }

    public void crearNecesidad(ApiRequests.Necesidad request) {
        http.post(url + "/necesidades", request, Object.class);
    }

    public void actualizarNecesidad(Long id, ApiRequests.ActualizarNecesidad request) {
        http.update(HttpMethod.PUT, url + "/necesidades/" + id, request);
    }

    public void eliminarNecesidad(Long id) {
        http.delete(url + "/necesidades/" + id);
    }

    // Matchmaking.

    public List<ApiDtos.Algoritmo> algoritmos() {
        return http.getList(url + "/algoritmos", ApiDtos.Algoritmo.class);
    }

    public void algoritmo(ApiRequests.Algoritmo request) {
        http.update(HttpMethod.PUT, url + "/algoritmos", request);
    }

    public ApiDtos.Sugerencia sugerir(Long id) {
        return http.post(url + "/sugerencias/" + id, Map.of(), ApiDtos.Sugerencia.class);
    }
}

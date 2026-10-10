package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.dto.donante.*;
import ar.edu.utn.donatrack.forms.*;
import ar.edu.utn.donatrack.mappers.DonacionesMapper;
import ar.edu.utn.donatrack.models.DonanteFila;
import ar.edu.utn.donatrack.validators.DonacionesValidator;
import ar.edu.utn.donatrack.models.WebDatos;
import ar.edu.utn.donatrack.mappers.*;
import static ar.edu.utn.donatrack.services.internal.ConsultaOpcional.lista;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;

/** Casos de uso del cliente liviano; no implementa reglas de negocio ni persistencia. */
@Service
public class DonacionesService {
    private final WebMapper webMapper;
    private final ar.edu.utn.donatrack.validators.WebValidator webValidator;
    private final DonanteDashboardMapper donanteMapper;
    private final BeneficiarioDashboardMapper beneficiarioMapper;
    private final DonacionesApiService api;
    private final DonacionesMapper mapper;
    private final DonacionesValidator validator;
    public DonacionesService(DonacionesApiService api, DonacionesMapper mapper, DonacionesValidator validator, WebMapper webMapper, ar.edu.utn.donatrack.validators.WebValidator webValidator, DonanteDashboardMapper donanteMapper, BeneficiarioDashboardMapper beneficiarioMapper) {
        this.api=api; this.mapper=mapper; this.validator=validator; this.webMapper=webMapper; this.webValidator=webValidator; this.donanteMapper=donanteMapper; this.beneficiarioMapper=beneficiarioMapper;
    }

    public Map<String,Object> camposDonante(DonanteForm form) { return mapper.camposDonante(form); }

    public void crear(String tipo, DonanteForm form) {
        var errors = new BeanPropertyBindingResult(form,"form");
        validator.validar(tipo,form,errors);
        check(errors);
        if ("humanos".equals(tipo)) api.crearHumano(mapper.humano(form));
        else api.crearJuridico(mapper.juridico(form));
    }

    public void eliminarDonante(String tipo, Long id) {
        if ("humanos".equals(tipo)) api.eliminarHumano(id); else api.eliminarJuridico(id);
    }

    public List<DonanteFila> filasDonantes() { return mapper.filas(api.listarHumanos(),api.listarJuridicos()); }

    public record Perfil(PerfilForm form, List<MedioDeContactoDto> contactos, List<?> representantes) {}
    public Perfil perfilDonante(String tipo,Long id) {
        if ("humanos".equals(tipo)) {
            var p=api.humano(id);
            return new Perfil(mapper.perfil(p),p.mediosDeContacto(),List.of());
        }
        var p=api.juridico(id);
        return new Perfil(mapper.perfil(p),p.mediosDeContacto(),p.personasRepresentantes());
    }
    public Perfil perfilBeneficiario(Long id) {
        var p=api.beneficiario(id);
        return new Perfil(mapper.perfil(p),List.of(),p.representantes());
    }
    public PerfilForm nuevoBeneficiario() { return mapper.nuevoBeneficiario(); }

    public void guardarDonante(String tipo,Long id,PerfilForm form) {
        var errors=new BeanPropertyBindingResult(form,"form");
        validator.validarPerfil(tipo,form,id,errors); check(errors);
        // Leer el perfil completo antes de PUT conserva los campos no editables de la pantalla.
        if ("humanos".equals(tipo)) api.actualizarHumano(id,mapper.humano(form,api.humano(id)));
        else api.actualizarJuridico(id,mapper.juridico(form,api.juridico(id)));
    }

    public void guardarBeneficiario(Long id,PerfilForm form,List<String> nombres,List<String> telefonos,List<String> emails) {
        var errors=new BeanPropertyBindingResult(form,"form");
        validator.validarPerfil("beneficiarios",form,id,errors);
        if(id==null) validator.validarRepresentantesBeneficiario(nombres,telefonos,emails,errors);
        check(errors);
        if(id==null) api.crearBeneficiario(mapper.beneficiario(form,nombres,telefonos,emails));
        else {
            var actual=api.beneficiario(id);
            api.actualizarBeneficiario(id,mapper.beneficiario(form,actual));
        }
    }

    public void donar(Long donanteId,String descripcion,BienForm inicial,BienesAdicionalesForm adicionales) {
        var bienes=mapper.bienes(inicial,adicionales);
        validator.bienes(bienes);
        api.crearDonacion(mapper.donacion(donanteId,descripcion,bienes));
    }
    public void crearBien(BienForm form) {
        var request=mapper.bien(form); validator.bien(request); api.crearBien(request);
    }
    public void actualizarBien(Long id,BienForm form) {
        var request=mapper.bien(form); validator.bien(request); api.actualizarBien(id,request);
    }
    public void crearNecesidad(Long entidadId,String descripcion,String tipo,String periodo,BienForm form) {
        var bien=mapper.bien(form); validator.bien(bien);
        api.crearNecesidad(mapper.necesidad(entidadId,descripcion,tipo,periodo,bien));
    }
    public void actualizarNecesidad(Long id,String descripcion,String tipo,String periodo,BienForm form) {
        var bien=mapper.bien(form); validator.bien(bien);
        api.actualizarNecesidad(id,mapper.necesidad(descripcion,tipo,periodo,bien));
    }
    public void estado(Long id,String estado,String justificacion) { api.estado(id,mapper.estado(estado,justificacion)); }
    public void asignar(Long donacionId,Long necesidadId,Float cantidad) {
        validator.asignacion(donacionId,necesidadId,cantidad);
        api.asignar(mapper.asignacion(donacionId,necesidadId,cantidad));
    }
    public void algoritmo(String nombre,boolean activo) { api.algoritmo(mapper.algoritmo(nombre,activo)); }
    public ApiDtos.Importacion importar(MultipartFile file) { validator.archivo(file); return api.importar(file); }

    private void check(BeanPropertyBindingResult errors) {
        if(errors.hasErrors()) throw new FormularioInvalidoException(errors.getAllErrors().get(0).getDefaultMessage());
    }

    // Consultas y operaciones que no requieren transformación del formulario.
    public List<DonanteHumanoResponse> listarHumanos() {
        return api.listarHumanos();
    }

    public List<DonanteJuridicoResponse> listarJuridicos() {
        return api.listarJuridicos();
    }

    public void eliminarHumano(Long id) {
        api.eliminarHumano(id);
    }

    public void eliminarJuridico(Long id) {
        api.eliminarJuridico(id);
    }

    public DonacionesDetalles.Humano humano(Long id) {
        return api.humano(id);
    }

    public DonacionesDetalles.Juridico juridico(Long id) {
        return api.juridico(id);
    }

    public List<ApiDtos.Donacion> donaciones() {
        return api.donaciones();
    }

    public ApiDtos.Donacion donacion(Long id) {
        return api.donacion(id);
    }

    public List<ApiDtos.Asignacion> asignaciones(Long id) {
        return api.asignaciones(id);
    }

    public void eliminarDonacion(Long id) {
        api.eliminarDonacion(id);
    }

    public DonacionesDetalles.Contactos contactosDonacion(Long id) {
        return api.contactosDonacion(id);
    }

    public List<ApiDtos.Asignacion> asignacionesEntidad(Long entidadId, String estado) {
        return api.asignacionesEntidad(entidadId, estado);
    }

    public DonacionesDetalles.Contactos contactosAsignacion(Long id) {
        return api.contactosAsignacion(id);
    }

    public List<ApiDtos.Categoria> categorias() {
        return api.categorias();
    }

    public void crearCategoria(String nombre) {
        api.crearCategoria(nombre);
    }

    public void crearSubCategoria(Long id, String nombre) {
        api.crearSubCategoria(id, nombre);
    }

    public List<ApiDtos.Bien> bienes() {
        return api.bienes();
    }

    public ApiDtos.Bien bien(Long id) {
        return api.bien(id);
    }

    public void eliminarBien(Long id) {
        api.eliminarBien(id);
    }

    public List<ApiDtos.Beneficiario> beneficiarios() {
        return api.beneficiarios();
    }

    public DonacionesDetalles.Beneficiario beneficiario(Long id) {
        return api.beneficiario(id);
    }

    public void eliminarBeneficiario(Long id) {
        api.eliminarBeneficiario(id);
    }

    public List<ApiDtos.Necesidad> necesidades() {
        return api.necesidades();
    }

    public ApiDtos.Necesidad necesidad(Long id) {
        return api.necesidad(id);
    }

    public void eliminarNecesidad(Long id) {
        api.eliminarNecesidad(id);
    }

    public List<ApiDtos.Algoritmo> algoritmos() {
        return api.algoritmos();
    }

    public ApiDtos.Sugerencia sugerir(Long id) {
        return api.sugerir(id);
    }

    public List<ApiDtos.Sugerencia> sugerencias() {
        return api.sugerencias();
    }

    public ApiDtos.Sugerencia sugerencia(Long donacionId) {
        return api.sugerencia(donacionId);
    }

    public String evaluar() {
        return api.evaluar();
    }

    /** Cabecera demostrativa: no representa una identidad autenticada. */
    public WebDatos datosDemostracionDonante() {
        return new WebDatos(List.of(),null,lista(api::listarHumanos),lista(api::listarJuridicos),List.of(),List.of(),List.of(),List.of());
    }
    public WebDatos datosDemostracionBeneficiario() {
        return new WebDatos(List.of(),null,List.of(),List.of(),lista(api::beneficiarios),List.of(),List.of(),List.of());
    }
    public Map<String,Object> explorarDonaciones() {
        return webMapper.map(new WebDatos(lista(api::donaciones),null,List.of(),List.of(),List.of(),List.of(),List.of(),List.of()));
    }
    public Map<String,Object> detallePublico(String id) {
        ApiDtos.Donacion donation=null;
        try { donation=api.donacion(webValidator.identificador(id)); }
        catch(org.springframework.web.client.RestClientException | IllegalStateException | FormularioInvalidoException e) { /* Detalle no disponible en la maqueta. */ }
        return webMapper.donationDetail(id,new WebDatos(List.of(),donation,List.of(),List.of(),List.of(),List.of(),List.of(),List.of()));
    }
    public Map<String,Object> donacionesDonante() {
        var d=datosDemostracionDonante();
        return donanteMapper.mapear(new WebDatos(lista(api::donaciones),null,d.humanos(),d.juridicos(),List.of(),List.of(),List.of(),List.of()));
    }
    public Map<String,Object> entidadesDonante() {
        var d=datosDemostracionDonante();
        return donanteMapper.mapear(new WebDatos(List.of(),null,d.humanos(),d.juridicos(),lista(api::beneficiarios),List.of(),List.of(),List.of()));
    }
    public Map<String,Object> necesidadesBeneficiario() {
        var d=datosDemostracionBeneficiario();
        return beneficiarioMapper.mapear(new WebDatos(List.of(),null,List.of(),List.of(),d.beneficiarios(),lista(api::necesidades),List.of(),List.of()));
    }
    public Map<String,Object> donacionesBeneficiario() {
        var d=datosDemostracionBeneficiario();
        return beneficiarioMapper.mapear(new WebDatos(lista(api::donaciones),null,List.of(),List.of(),d.beneficiarios(),List.of(),List.of(),List.of()));
    }
}

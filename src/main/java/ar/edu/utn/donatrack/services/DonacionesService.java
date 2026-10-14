package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.dto.donante.*;
import ar.edu.utn.donatrack.forms.*;
import ar.edu.utn.donatrack.mappers.DonacionesMapper;
import ar.edu.utn.donatrack.models.DonanteFila;
import ar.edu.utn.donatrack.validators.DonacionesValidator;
import ar.edu.utn.donatrack.mappers.*;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;

/** Casos de uso del cliente liviano; no implementa reglas de negocio ni persistencia. */
@Service
public class DonacionesService {
    public static final int DESCRIPCION_PUBLICA_MAX = 500;
    private final DonacionesApiService api;
    private final DonacionesMapper mapper;
    private final DonacionesValidator validator;
    public DonacionesService(DonacionesApiService api, DonacionesMapper mapper, DonacionesValidator validator) {
        this.api=api; this.mapper=mapper; this.validator=validator;
    }

    public Map<String,Object> camposDonante(DonanteForm form) { return mapper.camposDonante(form); }

    /** Devuelve el acceso que informa el alta (ENVIADO, SIN_EMAIL, ... o null). */
    public String crear(String tipo, DonanteForm form) {
        var errors = new BeanPropertyBindingResult(form,"form");
        validator.validar(tipo,form,errors);
        check(errors);
        if ("humanos".equals(tipo)) return api.crearHumano(mapper.humano(form));
        return api.crearJuridico(mapper.juridico(form));
    }

    public AccesoDtos.Respuesta enviarAcceso(String tipo, Long id) { return api.enviarAcceso(tipo, id); }

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

    public void completarEmail(String tipo,Long id,String email) { api.completarEmail(tipo,id,email); }

    public void guardarDonante(String tipo,Long id,PerfilForm form) {
        var errors=new BeanPropertyBindingResult(form,"form");
        validator.validarPerfil(tipo,form,id,errors); check(errors);
        // Leer el perfil completo antes de PUT conserva los campos no editables de la pantalla.
        if ("humanos".equals(tipo)) api.actualizarHumano(id,mapper.humano(form,api.humano(id)));
        else api.actualizarJuridico(id,mapper.juridico(form,api.juridico(id)));
    }

    /** Devuelve el acceso del alta (null en edición). */
    public String guardarBeneficiario(Long id,PerfilForm form,List<String> nombres,List<String> telefonos,List<String> emails) {
        var errors=new BeanPropertyBindingResult(form,"form");
        validator.validarPerfil("beneficiarios",form,id,errors);
        if(id==null) {
            validator.validarRepresentantesBeneficiario(nombres,telefonos,emails,errors);
            check(errors);
            return api.crearBeneficiario(mapper.beneficiario(form,nombres,telefonos,emails));
        }
        // Lectura previa (no escribe): si la entidad no tiene representantes, el PUT necesita uno cargado.
        var actual=api.beneficiario(id);
        if(actual.representantes()==null || actual.representantes().isEmpty())
            validator.validarRepresentantesBeneficiario(nombres,telefonos,emails,errors);
        check(errors);
        api.actualizarBeneficiario(id,mapper.beneficiario(form,actual,nombres,telefonos,emails));
        return null;
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

    /**
     * Publica o despublica una entrega en la landing. Al despublicar no se envía descripción.
     * El largo se valida acá, antes del PATCH.
     */
    public DonacionesDetalles.Publicacion publicarAsignacion(Long asignacionId, boolean publicable, String descripcionPublica) {
        if (publicable && descripcionPublica != null && descripcionPublica.length() > DESCRIPCION_PUBLICA_MAX)
            throw new FormularioInvalidoException("La descripción pública no puede superar los " + DESCRIPCION_PUBLICA_MAX + " caracteres.");
        String descripcion = publicable && descripcionPublica != null && !descripcionPublica.isBlank()
                ? descripcionPublica.trim() : null;
        return api.publicacion(asignacionId, publicable, descripcion);
    }
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

    /**
     * Nombre visible de cada donante (humano o jurídico), armado una sola vez para rankings e historial.
     * Si la consulta falla, el mapa queda vacío y las vistas muestran el id.
     */
    public Map<Long,String> nombresDonantes() {
        Map<Long,String> nombres = new HashMap<>();
        try {
            for (var h : api.listarHumanos()) {
                if (h.id() != null) nombres.put(h.id(), (h.nombre() + " " + h.apellido()).trim());
            }
            for (var j : api.listarJuridicos()) {
                if (j.id() != null) nombres.putIfAbsent(j.id(), j.razonSocial());
            }
        } catch (org.springframework.web.client.RestClientException | IllegalStateException e) {
            return Map.of();
        }
        return nombres;
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

    /** Descripción por id de donación, con una sola llamada a GET /donaciones (sin N+1). */
    public Map<Long,String> descripcionesDonaciones() {
        Map<Long,String> descripciones=new HashMap<>();
        for (var d : api.donaciones()) if (d.id()!=null && d.descripcionGeneral()!=null) descripciones.putIfAbsent(d.id(), d.descripcionGeneral());
        return descripciones;
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

    /** Razón social por id de entidad beneficiaria, con una sola llamada a GET /beneficiarios (sin N+1). */
    public Map<Long,String> nombresEntidades() {
        Map<Long,String> nombres=new HashMap<>();
        for (var b : api.beneficiarios()) if (b.id()!=null && b.razonSocial()!=null) nombres.putIfAbsent(b.id(), b.razonSocial());
        return nombres;
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
}

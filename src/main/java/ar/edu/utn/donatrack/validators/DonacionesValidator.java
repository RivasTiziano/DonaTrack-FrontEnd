package ar.edu.utn.donatrack.validators;
import ar.edu.utn.donatrack.forms.*;
import ar.edu.utn.donatrack.dto.ApiRequests;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Objects;

/** Validaciones del cliente: el backend conserva la autoridad sobre reglas de negocio. */
@Component
public class DonacionesValidator {
    public void validar(String tipo, DonanteForm f, Errors errors) {
        required(f.numeroDocumento(), "numeroDocumento", errors);
        required(f.calle(), "calle", errors); required(f.numero(), "numero", errors);
        required(f.ciudad(), "ciudad", errors); required(f.provincia(), "provincia", errors);
        if (!email(f.email())) errors.rejectValue("email", "format", "Ingresá un email válido.");
        if ("humanos".equals(tipo)) {
            required(f.nombre(), "nombre", errors); required(f.apellido(), "apellido", errors);
            required(f.genero(), "genero", errors);
            if (f.fechaNacimiento() == null) errors.rejectValue("fechaNacimiento", "required", "Indicá la fecha de nacimiento.");
        } else {
            required(f.razonSocial(), "razonSocial", errors); required(f.tipoEntidad(), "tipoEntidad", errors);
            required(f.rubro(), "rubro", errors);
            boolean initial = !blank(f.representanteNombre()) || !blank(f.representanteApellido())
                    || !blank(f.representanteDocumento()) || f.representanteNacimiento() != null || !blank(f.representanteGenero());
            if (initial) {
                required(f.representanteNombre(), "representanteNombre", errors);
                required(f.representanteApellido(), "representanteApellido", errors);
                required(f.representanteDocumento(), "representanteDocumento", errors);
                required(f.representanteGenero(), "representanteGenero", errors);
                if (f.representanteNacimiento() == null) errors.rejectValue("representanteNacimiento", "required", "Completá la fecha del representante.");
            }
            int count = initial ? 1 : 0;
            int rows = Math.max(size(f.repNombre()), Math.max(size(f.repApellido()), Math.max(size(f.repDocumento()), Math.max(size(f.repNacimiento()), size(f.repGenero())))));
            for (int i = 0; i < rows; i++) {
                if (blank(at(f.repNombre(),i)) && blank(at(f.repApellido(),i)) && blank(at(f.repDocumento(),i))
                        && at(f.repNacimiento(),i) == null && blank(at(f.repGenero(),i))) continue;
                count++;
                if (blank(at(f.repNombre(),i)) || blank(at(f.repApellido(),i)) || blank(at(f.repDocumento(),i))
                        || at(f.repNacimiento(),i) == null || blank(at(f.repGenero(),i)))
                    errors.reject("representantes", "Completá todos los datos de los representantes.");
            }
            if (count == 0) errors.reject("representantes", "Ingresá al menos un representante real; no se generan datos ficticios.");
        }
        int rows = Math.max(size(f.contactoTipo()), size(f.contactoValor()));
        for (int i = 0; i < rows; i++) {
            String t = at(f.contactoTipo(),i), v = at(f.contactoValor(),i);
            if (blank(t) && blank(v)) continue;
            if (blank(t) || blank(v) || !List.of("EMAIL","SMS","WHATSAPP").contains(t) || ("EMAIL".equals(t) && !email(v)))
                errors.reject("contactos", "Completá canal y valor de cada contacto.");
        }
        String t = f.contactoPredeterminadoTipo(), v = f.contactoPredeterminadoValor();
        if (!blank(t) || !blank(v)) {
            boolean belongs = ("EMAIL".equals(t) && Objects.equals(v,f.email())) || ("SMS".equals(t) && Objects.equals(v,f.telefono()));
            for (int i = 0; i < size(f.contactoTipo()); i++)
                belongs |= Objects.equals(t,at(f.contactoTipo(),i)) && Objects.equals(v,at(f.contactoValor(),i));
            if (blank(t) || blank(v) || !belongs) errors.reject("predeterminado", "El contacto predeterminado debe pertenecer a los contactos informados.");
        }
    }

    public void validarPerfil(String tipo, PerfilForm f, Long id, Errors errors) {
        required(f.calle(),"calle",errors); required(f.numero(),"numero",errors);
        required(f.ciudad(),"ciudad",errors); required(f.provincia(),"provincia",errors);
        if ("humanos".equals(tipo)) {
            required(f.nombre(),"nombre",errors); required(f.apellido(),"apellido",errors);
            required(f.genero(),"genero",errors);
            if (f.fechaNacimiento()==null) errors.rejectValue("fechaNacimiento","required","Indicá la fecha de nacimiento.");
        } else {
            required(f.razonSocial(),"razonSocial",errors); required(f.tipoEntidad(),"tipoEntidad",errors); required(f.rubro(),"rubro",errors);
        }
        if (!"beneficiarios".equals(tipo)) required(f.numeroDocumento(),"numeroDocumento",errors);
        else {
            required(f.contactoTipo(),"contactoTipo",errors); required(f.contactoValor(),"contactoValor",errors);
            if ("EMAIL".equals(f.contactoTipo()) && !email(f.contactoValor())) errors.rejectValue("contactoValor","format","Ingresá un email válido.");
            if (id!=null && f.activo()==null) errors.rejectValue("activo","required","Indicá el estado activo.");
        }
    }

    public void validarRepresentantesBeneficiario(List<String> nombres, List<String> telefonos, List<String> emails, Errors errors) {
        if (size(nombres)!=size(telefonos) || size(nombres)!=size(emails)) {
            errors.reject("representantes","Los datos de representantes están incompletos."); return;
        }
        for (int i=0;i<size(nombres);i++) {
            if (blank(nombres.get(i)) && blank(telefonos.get(i)) && blank(emails.get(i))) continue;
            if (blank(nombres.get(i)) || !email(emails.get(i)))
                errors.reject("representantes","Completá nombre y email del representante, o dejá todos sus campos vacíos.");
        }
    }

    public void archivo(MultipartFile f) { require(f!=null && !f.isEmpty(), "Seleccioná un archivo CSV no vacío."); }
    public void bienes(List<ApiRequests.Bien> values) {
        require(!values.isEmpty(), "Debe agregar al menos un bien a la donación.");
        values.forEach(this::bien);
    }
    public void bien(ApiRequests.Bien b) {
        require(!blank(b.descripcion()) && b.cantidad()!=null && Float.isFinite(b.cantidad()) && b.cantidad()>0
                && b.subCategoriaId()!=null && !blank(b.unidadMedida()) && !blank(b.tipoBien()),
                "Completá descripción, cantidad positiva, subcategoría, unidad y tipo del bien.");
    }
    public void asignacion(Long donacionId, Long necesidadId, Float cantidad) {
        require(donacionId!=null && necesidadId!=null && cantidad!=null && Float.isFinite(cantidad) && cantidad>0, "Indicá una cantidad de asignación mayor que cero.");
    }
    private void required(String s,String field,Errors errors) { if(blank(s)) errors.rejectValue(field,"required","Este campo es obligatorio."); }
    private boolean email(String s) { return s!=null && s.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"); }
    private boolean blank(String s) { return s==null || s.isBlank(); }
    private int size(List<?> l) { return l==null ? 0 : l.size(); }
    private <T> T at(List<T> l,int i) { return l==null || i>=l.size() ? null : l.get(i); }
    private void require(boolean ok,String message) { if(!ok) throw new ar.edu.utn.donatrack.exceptions.FormularioInvalidoException(message); }
}

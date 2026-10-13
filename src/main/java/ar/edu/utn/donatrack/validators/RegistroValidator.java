package ar.edu.utn.donatrack.validators;

import ar.edu.utn.donatrack.forms.RegistroForm;
import ar.edu.utn.donatrack.forms.RegistroForm.Representante;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reglas del registro público del lado del front. Los errores quedan junto a cada campo.
 * El backend valida lo mismo; esta validación evita llamarlo con datos que sabemos inválidos.
 */
@Component
public class RegistroValidator {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> GENEROS = Set.of("FEMENINO", "MASCULINO", "NO_BINARIO", "OTRO", "PREFIERE_NO_INFORMAR");
    private static final Set<String> TIPOS_ENTIDAD = Set.of("GUBERNAMENTAL", "ONG", "EMPRESA", "INSTITUCION");
    private static final Set<String> TIPOS_DOCUMENTO = Set.of("DNI", "CUIT");
    private static final String AVISO_CONTRASENIA = "La contraseña debe tener al menos " + AccesoValidator.LONGITUD_MINIMA + " caracteres.";
    private static final String REPRESENTANTE_INCOMPLETO = "Completá todos los datos de cada representante cargado.";
    private static final String REPRESENTANTE_ENTIDAD_INCOMPLETO = "Cada representante necesita nombre y teléfono.";

    public void validarHumano(RegistroForm f, Errors errors) {
        requerido(f.getNombre(), "nombre", "Ingresá tu nombre.", errors);
        requerido(f.getApellido(), "apellido", "Ingresá tu apellido.", errors);
        fechaNacimiento(f.getFechaNacimiento(), errors);
        requerido(f.getDocumento(), "documento", "Ingresá tu DNI.", errors);
        if (!pertenece(GENEROS, f.getGenero())) errors.rejectValue("genero", "invalid", "Elegí una opción de género.");
        direccion(f, errors);
        contactos(f, errors);
        clave(f, errors);
    }

    public void validarJuridico(RegistroForm f, Errors errors) {
        requerido(f.getDocumento(), "documento", "Ingresá el CUIT de la organización.", errors);
        requerido(f.getRazonSocial(), "razonSocial", "Ingresá la razón social.", errors);
        if (!pertenece(TIPOS_ENTIDAD, f.getTipoEntidad())) errors.rejectValue("tipoEntidad", "invalid", "Elegí el tipo de organización.");
        requerido(f.getRubro(), "rubro", "Ingresá el rubro.", errors);
        direccion(f, errors);
        contactos(f, errors);
        representantesJuridicos(f, errors);
        clave(f, errors);
    }

    public void validarEntidad(RegistroForm f, Errors errors) {
        requerido(f.getRazonSocial(), "razonSocial", "Ingresá la razón social.", errors);
        if (!pertenece(TIPOS_ENTIDAD, f.getTipoEntidad())) errors.rejectValue("tipoEntidad", "invalid", "Elegí el tipo de entidad.");
        requerido(f.getRubro(), "rubro", "Ingresá el rubro.", errors);
        direccion(f, errors);
        email(f.getEmail(), "Ingresá el email de contacto de la entidad.", errors);
        representantesEntidad(f, errors);
        clave(f, errors);
    }

    private void direccion(RegistroForm f, Errors errors) {
        requerido(f.getCalle(), "calle", "Ingresá la calle.", errors);
        requerido(f.getNumero(), "numero", "Ingresá el número.", errors);
        requerido(f.getCiudad(), "ciudad", "Ingresá la ciudad.", errors);
        requerido(f.getProvincia(), "provincia", "Ingresá la provincia.", errors);
    }

    // El email de la cuenta es el contacto EMAIL. El predeterminado tiene que ser uno de los contactos cargados.
    private void contactos(RegistroForm f, Errors errors) {
        email(f.getEmail(), "Ingresá un email de contacto.", errors);
        Set<String> cargados = new HashSet<>(Set.of("EMAIL"));
        if (!blank(f.getTelefono())) cargados.add("SMS");
        if (!blank(f.getWhatsapp())) cargados.add("WHATSAPP");
        if (!cargados.contains(f.getPredeterminado()))
            errors.rejectValue("predeterminado", "invalid", "Elegí como predeterminado un medio de contacto que hayas cargado.");
    }

    // Al menos un representante completo. Un representante a medio cargar también se rechaza.
    private void representantesJuridicos(RegistroForm f, Errors errors) {
        boolean completo = false;
        boolean incompleto = false;
        for (Representante r : f.getRepresentantes()) {
            if (vacio(r)) continue;
            if (completoJuridico(r)) completo = true; else incompleto = true;
        }
        if (incompleto) errors.rejectValue("representantes", "incomplete", REPRESENTANTE_INCOMPLETO);
        else if (!completo) errors.rejectValue("representantes", "required", "Cargá al menos una persona representante con todos sus datos.");
    }

    // Al menos un representante con nombre y teléfono (NOT NULL en Donaciones). El correo es opcional; si se carga, tiene que ser válido.
    private void representantesEntidad(RegistroForm f, Errors errors) {
        boolean completo = false;
        for (Representante r : f.getRepresentantes()) {
            if (vacio(r)) continue;
            if (blank(r.getNombre()) || blank(r.getTelefono())) {
                errors.rejectValue("representantes", "incomplete", REPRESENTANTE_ENTIDAD_INCOMPLETO);
                return;
            }
            if (!blank(r.getCorreo()) && !EMAIL.matcher(r.getCorreo().trim()).matches()) {
                errors.rejectValue("representantes", "format", "Ingresá un correo válido para el representante.");
                return;
            }
            completo = true;
        }
        if (!completo) errors.rejectValue("representantes", "required", "Cargá al menos una persona representante con nombre y teléfono.");
    }

    private void clave(RegistroForm f, Errors errors) {
        if (blank(f.getPassword()) || f.getPassword().length() < AccesoValidator.LONGITUD_MINIMA)
            errors.rejectValue("password", "size", AVISO_CONTRASENIA);
        if (f.getPassword() != null && !f.getPassword().equals(f.getConfirmacion()))
            errors.rejectValue("confirmacion", "mismatch", "Las contraseñas no coinciden.");
    }

    private void fechaNacimiento(String valor, Errors errors) {
        if (blank(valor)) { errors.rejectValue("fechaNacimiento", "required", "Indicá la fecha de nacimiento."); return; }
        LocalDate fecha = fecha(valor);
        if (fecha == null) errors.rejectValue("fechaNacimiento", "format", "Revisá la fecha de nacimiento.");
        else if (!fecha.isBefore(LocalDate.now())) errors.rejectValue("fechaNacimiento", "future", "La fecha de nacimiento tiene que ser anterior a hoy.");
        else if (fecha.getYear() < 1900) errors.rejectValue("fechaNacimiento", "range", "Revisá la fecha de nacimiento.");
    }

    private void email(String valor, String mensajeVacio, Errors errors) {
        if (blank(valor)) errors.rejectValue("email", "required", mensajeVacio);
        else if (!EMAIL.matcher(valor.trim()).matches()) errors.rejectValue("email", "format", "Ingresá un email válido.");
    }

    private static void requerido(String valor, String campo, String mensaje, Errors errors) {
        if (blank(valor)) errors.rejectValue(campo, "required", mensaje);
    }

    private static boolean completoJuridico(Representante r) {
        return !blank(r.getNombre()) && !blank(r.getApellido()) && !blank(r.getDocumento())
                && fecha(r.getFechaNacimiento()) != null && pertenece(GENEROS, r.getGenero())
                && pertenece(TIPOS_DOCUMENTO, r.getTipoDocumento());
    }

    private static boolean vacio(Representante r) {
        return List.of(valor(r.getNombre()), valor(r.getApellido()), valor(r.getDocumento()), valor(r.getTipoDocumento()),
                        valor(r.getFechaNacimiento()), valor(r.getGenero()), valor(r.getTelefono()), valor(r.getCorreo()))
                .stream().allMatch(String::isEmpty);
    }

    private static String valor(String s) { return s == null ? "" : s.trim(); }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    // Set.of no admite null en contains: un campo sin cargar no debe romper la validación.
    private static boolean pertenece(Set<String> valores, String valor) { return valor != null && valores.contains(valor); }

    private static LocalDate fecha(String s) {
        if (blank(s)) return null;
        try { return LocalDate.parse(s.trim()); } catch (DateTimeParseException e) { return null; }
    }
}

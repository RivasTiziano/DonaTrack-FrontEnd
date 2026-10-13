package ar.edu.utn.donatrack.forms;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulario único del registro público (persona humana, organización o entidad beneficiaria).
 * Cada tipo usa un subconjunto de los campos. Los representantes son una lista que el front agrega o quita.
 * Es una clase con getters y setters para que Spring la enlace; la contraseña nunca se imprime ni vuelve a la vista.
 */
public class RegistroForm {
    private String nombre;
    private String apellido;
    private String fechaNacimiento;
    private String documento;
    private String genero;
    private String calle;
    private String numero;
    private String ciudad;
    private String provincia;
    private String email;
    private String telefono;
    private String whatsapp;
    private String predeterminado = "EMAIL";
    private String razonSocial;
    private String tipoEntidad;
    private String rubro;
    private String password;
    private String confirmacion;
    private List<Representante> representantes = new ArrayList<>();

    /** Un representante (organización o entidad). Cada tipo usa los campos que le corresponden. */
    public static class Representante {
        private String nombre;
        private String apellido;
        private String documento;
        private String tipoDocumento;
        private String fechaNacimiento;
        private String genero;
        private String telefono;
        private String correo;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getDocumento() { return documento; }
        public void setDocumento(String documento) { this.documento = documento; }
        public String getTipoDocumento() { return tipoDocumento; }
        public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
        public String getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
    }

    /** Deja al menos {@code cantidad} bloques de representante, para mostrarlos en el formulario. */
    public void completarRepresentantes(int cantidad) {
        while (representantes.size() < cantidad) representantes.add(new Representante());
    }

    /** Borra las contraseñas antes de volver a mostrar el formulario. */
    public void limpiarContrasenas() {
        password = "";
        confirmacion = "";
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getPredeterminado() { return predeterminado; }
    public void setPredeterminado(String predeterminado) { this.predeterminado = predeterminado; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; }
    public String getRubro() { return rubro; }
    public void setRubro(String rubro) { this.rubro = rubro; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmacion() { return confirmacion; }
    public void setConfirmacion(String confirmacion) { this.confirmacion = confirmacion; }
    public List<Representante> getRepresentantes() { return representantes; }
    public void setRepresentantes(List<Representante> representantes) { this.representantes = representantes; }

    @Override public String toString() {
        return "RegistroForm[nombre=" + nombre + ", razonSocial=" + razonSocial + ", email=" + email + ", contrasenia=********]";
    }
}

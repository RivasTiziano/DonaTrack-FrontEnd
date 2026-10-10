package ar.edu.utn.donatrack.exceptions;
/** Error de entrada del frontend, con un mensaje apto para mostrar en el formulario. */
public class FormularioInvalidoException extends RuntimeException {
    public FormularioInvalidoException(String mensaje) { super(mensaje); }
}

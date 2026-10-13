package ar.edu.utn.donatrack.exceptions;

/** Se pidió la sesión actual en una operación que la necesita y no hay usuario autenticado. */
public class SesionRequeridaException extends RuntimeException {
    public SesionRequeridaException() {
        super("Necesitás iniciar sesión para realizar esta operación.");
    }
}

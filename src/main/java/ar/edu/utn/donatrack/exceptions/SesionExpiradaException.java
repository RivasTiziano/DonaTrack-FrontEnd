package ar.edu.utn.donatrack.exceptions;

/** El refresh token fue rechazado: la sesión ya no sirve y hay que volver a iniciarla. */
public class SesionExpiradaException extends RuntimeException {
    public SesionExpiradaException() {
        super("Tu sesión venció. Iniciá sesión nuevamente.");
    }
}

package ar.edu.utn.donatrack.exceptions;

/** El registro público no se pudo completar. El motivo decide qué ayuda se muestra al usuario. */
public class RegistroRechazadoException extends RuntimeException {
    public enum Motivo { DATOS_INVALIDOS, CUENTA_EXISTENTE, DATOS_DUPLICADOS, NO_DISPONIBLE }

    private final Motivo motivo;

    public RegistroRechazadoException(Motivo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public Motivo motivo() { return motivo; }
}

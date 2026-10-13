package ar.edu.utn.donatrack.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** El donante o la entidad no existe en Donaciones: no se puede crear su acceso (responde 404). */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PerfilNoEncontradoException extends RuntimeException {
    public PerfilNoEncontradoException() {
        super("El perfil no existe en el servicio de donaciones.");
    }
}

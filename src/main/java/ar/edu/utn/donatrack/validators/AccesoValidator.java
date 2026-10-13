package ar.edu.utn.donatrack.validators;

import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import org.springframework.stereotype.Component;

/** Reglas de la contraseña inicial de un acceso nuevo: mínimo 12 caracteres y confirmación igual. */
@Component
public class AccesoValidator {
    public static final int LONGITUD_MINIMA = 12;

    public void validar(String password, String confirmacion) {
        if (password == null || password.length() < LONGITUD_MINIMA)
            throw new FormularioInvalidoException("La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        if (!password.equals(confirmacion))
            throw new FormularioInvalidoException("Las contraseñas no coinciden.");
    }
}

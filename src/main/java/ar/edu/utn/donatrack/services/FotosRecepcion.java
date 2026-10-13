package ar.edu.utn.donatrack.services;

import java.util.List;

/**
 * Convierte la entrada de fotos del formulario de recepción en la lista que se envía a logística.
 * Permite cambiar la fuente (URL pegadas, subida de archivos) sin tocar el controlador.
 */
public interface FotosRecepcion {
    List<String> resolver(String textoUrls);
}

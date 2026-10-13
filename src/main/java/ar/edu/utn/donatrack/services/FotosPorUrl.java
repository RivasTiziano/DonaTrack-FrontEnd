package ar.edu.utn.donatrack.services;

import org.springframework.stereotype.Service;
import java.util.List;

/** Implementación inicial: una URL por línea. Las líneas vacías se descartan; la validación la hace RecepcionValidator. */
@Service
public class FotosPorUrl implements FotosRecepcion {
    @Override
    public List<String> resolver(String textoUrls) {
        if (textoUrls == null || textoUrls.isBlank()) return List.of();
        return textoUrls.lines().map(String::trim).filter(linea -> !linea.isEmpty()).toList();
    }
}

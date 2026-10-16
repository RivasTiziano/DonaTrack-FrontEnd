package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.services.internal.SesionActual;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Fotos locales del frontend, separadas por entidad y conservadas entre reinicios. */
@Service
public class ImagenesRecepcion {
    private final Path directorio;
    private final SesionActual sesiones;
    public ImagenesRecepcion(@Value("${app.recepciones.directorio:${user.home}/.donatrack/recepciones}") String directorio, SesionActual sesiones) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        this.sesiones = sesiones;
    }
    private Path carpeta() {
        var sesion = sesiones.obtener().orElseThrow(() -> new FormularioInvalidoException("Volvé a iniciar sesión."));
        return directorio.resolve(String.valueOf(sesion.getReferenciaId()));
    }
    public String guardar(MultipartFile archivo) {
        if (archivo.isEmpty() || archivo.getSize() > 10 * 1024 * 1024)
            throw new FormularioInvalidoException("Seleccioná una imagen JPG o PNG de hasta 10 MB.");
        try (var stream = ImageIO.createImageInputStream(archivo.getInputStream())) {
            if (stream == null) throw new FormularioInvalidoException("No se pudo leer la imagen.");
            var lectores = ImageIO.getImageReaders(stream);
            if (!lectores.hasNext()) throw new FormularioInvalidoException("La imagen debe ser JPG o PNG.");
            var lector = lectores.next();
            try {
                String formato = lector.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!java.util.Set.of("png", "jpeg", "jpg").contains(formato))
                    throw new FormularioInvalidoException("La imagen debe ser JPG o PNG.");
                lector.setInput(stream);
                if ((long) lector.getWidth(0) * lector.getHeight(0) > 25_000_000)
                    throw new FormularioInvalidoException("La imagen es demasiado grande. Elegí una de hasta 25 megapíxeles.");
                var imagen = lector.read(0);
                Files.createDirectories(carpeta());
                String nombre = UUID.randomUUID() + (formato.equals("png") ? ".png" : ".jpg");
                if (!ImageIO.write(imagen, formato, carpeta().resolve(nombre).toFile()))
                    throw new IOException("Formato no disponible");
                return nombre;
            } finally { lector.dispose(); }
        } catch (IOException exception) {
            throw new FormularioInvalidoException("No se pudo guardar la imagen. Volvé a intentarlo.");
        }
    }
    public Path buscar(String nombre) {
        if (!nombre.matches("[0-9a-f-]{36}\\.(png|jpg)")) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        Path archivo = carpeta().resolve(nombre);
        if (!Files.isRegularFile(archivo)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        return archivo;
    }
    public void descartar(String nombre) {
        try { Files.deleteIfExists(carpeta().resolve(nombre)); } catch (IOException ignored) { }
    }
}

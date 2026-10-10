package ar.edu.utn.donatrack.services;
import ar.edu.utn.donatrack.mappers.AutenticacionMapper;
import org.springframework.stereotype.Service;
import java.util.*;
/** Navegación demostrativa. Pendiente integración con autenticación real. */
@Service
public class AutenticacionService {
    private final AutenticacionMapper mapper;
    public AutenticacionService(AutenticacionMapper mapper) { this.mapper=mapper; }
    public Map<String,Object> register() { return mapper.opcionesRegistro(); }
    /** No verifica identidad, contraseñas ni genera tokens. */
    public String destinoDemostracion(String email) {
        String lower=email.toLowerCase(Locale.ROOT);
        if(lower.contains("admin")) return "/admin/dashboard";
        if(lower.contains("entidad") || lower.contains("beneficiaria")) return "/entidad/dashboard";
        return "/donante/dashboard";
    }
}

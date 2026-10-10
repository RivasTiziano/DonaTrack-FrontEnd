package ar.edu.utn.donatrack.mappers;
import java.util.*;
import org.springframework.stereotype.Component;
/** Opciones de navegación del registro; no crea cuentas. */
@Component
public class AutenticacionMapper {
public Map<String,Object> opcionesRegistro() {
        Map<String,Object> model = new java.util.LinkedHashMap<>(); 
        List<Map<String, String>> registrationOptions = List.of(
            Map.of(
                "id", "donor-human",
                "title", "Soy una persona donante",
                "description", "Registro como persona humana para aportar bienes materiales",
                "icon", "heart",
                "path", "/registro/donante-humano"
            ),
            Map.of(
                "id", "donor-organization",
                "title", "Soy una organización donante",
                "description", "Registro como empresa, ONG, institución o entidad jurídica",
                "icon", "building-2",
                "path", "/registro/donante-organizacion"
            ),
            Map.of(
                "id", "beneficiary",
                "title", "Soy una entidad beneficiaria",
                "description", "Registro como comedor, escuela rural u hogar para solicitar donaciones",
                "icon", "users",
                "path", "/registro/entidad-beneficiaria"
            )
        );
        model.put("registrationOptions", registrationOptions);
        return model; 
    }
}

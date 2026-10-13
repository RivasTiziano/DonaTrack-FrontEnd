package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.ApiDtos;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Datos de las páginas públicas (inicio, mapa y detalle).
 * El backend todavía no expone endpoints públicos (pedido D1) y GET /donaciones exige token de administrador,
 * así que hasta entonces no hay datos reales: las páginas muestran estados vacíos. No llamar a la API desde aquí.
 */
@Service
public class PublicoService {

    // PROVISORIO: conectar a GET /publico/entregas (pedido backend D1)
    public List<ApiDtos.Donacion> donacionesDestacadas() {
        return List.of();
    }

    // PROVISORIO: conectar a GET /publico/entregas (pedido backend D1)
    public List<ApiDtos.Donacion> entregasEnMapa() {
        return List.of();
    }

    // PROVISORIO: conectar a GET /publico/entregas/{id} (pedido backend D1)
    public ApiDtos.Donacion detallePublico(String id) {
        return null;
    }
}

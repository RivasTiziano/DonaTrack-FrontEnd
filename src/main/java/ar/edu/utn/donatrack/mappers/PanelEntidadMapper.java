package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import ar.edu.utn.donatrack.dto.LogisticaDetalles;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static ar.edu.utn.donatrack.mappers.PanelFormato.*;

/** Adapta los DTO de la API a las claves que usan las plantillas del panel de la entidad. Puro: no hace HTTP. */
@Component
public class PanelEntidadMapper {

    public Map<String, Object> perfil(DonacionesDetalles.Beneficiario b) {
        var representantes = b.representantes();
        String representante = representantes == null || representantes.isEmpty() ? null : representantes.get(0).nombre();
        var medio = b.medioDeContactoPredeterminado();
        return mapa("name", b.razonSocial(),
                "address", direccion(b.direccion()),
                "representative", representante,
                "email", medio == null ? null : medio.formaContacto());
    }

    public Map<String, Object> necesidad(ApiDtos.Necesidad n) {
        ApiDtos.Bien bien = n.bien();
        ApiDtos.CategoriaBien categoria = bien == null ? null : bien.categoria();
        String subcategoria = categoria == null || categoria.subCategoria() == null ? null : categoria.subCategoria().nombreSubCategoria();
        return mapa("id", n.necesidadId(),
                "title", n.descripcion(),
                "description", n.descripcion(),
                "type", n.tipoNecesidad(),
                "category", categoria == null ? null : categoria.nombreCategoria(),
                "subcategory", subcategoria,
                "status", n.estado(),
                "status_label", etiquetaEstado(n.estado()),
                "quantity", bien == null ? null : numero(bien.cantidad()),
                "unit", bien == null ? null : minusculas(bien.unidadMedida()),
                "deadline", n.periodo(),
                "progress", progreso(n),
                "remaining", numero(n.cantidadRestante()));
    }

    /** Asignación de una donación a la entidad, para la lista de donaciones asignadas. */
    public Map<String, Object> asignacion(ApiDtos.Asignacion a) {
        ApiDtos.Bien bien = a.bien();
        return mapa("id", a.id(),
                "donationId", a.donacionId(),
                "title", titulo(a),
                "status", a.estado(),
                "status_label", etiquetaEstado(a.estado()),
                "status_badge", claseEstado(a.estado()),
                "items_count", numero(a.cantidadAsignada()),
                "unit", bien == null ? null : minusculas(bien.unidadMedida()),
                "assigned_date", fecha(a.fechaAsignacion()),
                "expected_delivery", fecha(a.fechaEntrega()),
                "failure_reason", a.justificacionFallo());
    }

    /**
     * Entrega en camino de la entidad: el destino es su propia sede y el camión, el que reporta el seguimiento.
     * Sin asignación (el listado no la trae), la fila muestra lo que informa el seguimiento.
     */
    public Map<String, Object> entrega(LogisticaDetalles.SeguimientoEntrega s, ApiDtos.Asignacion a, LocalDateTime ahora) {
        ApiDtos.Bien bien = a == null ? null : a.bien();
        String titulo = a == null ? null : titulo(a);
        String unidad = bien == null ? null : minusculas(bien.unidadMedida());
        String cantidad = a == null || a.cantidadAsignada() == null ? null
                : numero(a.cantidadAsignada()) + (unidad == null ? "" : " " + unidad);
        return mapa("id", s.entregaId(),
                "asignacionId", s.asignacionId(),
                "donationId", a == null ? null : a.donacionId(),
                "title", titulo,
                "status", s.estado(),
                "status_label", etiquetaEstado(s.estado()),
                "status_badge", claseEstado(s.estado()),
                "destinationAddress", ubicacion(s.destino()),
                "destinationCoords", coordenadas(s.destino()),
                "goods", List.of(mapa("name", bien == null ? titulo : bien.descripcion(), "quantity", cantidad)),
                "truck", camion(s.camion(), ahora),
                "isPerishable", bien != null && bien.fechaDeVencimiento() != null);
    }

    /** Asignación en camino o lista para entregar (resumen). El camión no se informa: el resumen no consulta el seguimiento. */
    public Map<String, Object> entrega(ApiDtos.Asignacion a) {
        ApiDtos.Bien bien = a.bien();
        String unidad = bien == null ? null : minusculas(bien.unidadMedida());
        String cantidad = a.cantidadAsignada() == null ? null
                : numero(a.cantidadAsignada()) + (unidad == null ? "" : " " + unidad);
        return mapa("id", a.id(),
                "donationId", a.donacionId(),
                "title", titulo(a),
                "status", a.estado(),
                "status_label", etiquetaEstado(a.estado()),
                "status_badge", claseEstado(a.estado()),
                "goods", List.of(mapa("name", bien == null ? titulo(a) : bien.descripcion(), "quantity", cantidad)),
                "truck", null,
                "isPerishable", bien != null && bien.fechaDeVencimiento() != null);
    }

    private static String titulo(ApiDtos.Asignacion a) {
        if (a.descripcionGeneralDonacion() != null && !a.descripcionGeneralDonacion().isBlank()) return a.descripcionGeneralDonacion();
        return a.bien() == null ? null : a.bien().descripcion();
    }

    /** Porcentaje cubierto con tope 100; si falta algún dato, 0. */
    private static int progreso(ApiDtos.Necesidad n) {
        Float recibida = n.cantidadRecibida();
        Float cantidad = n.bien() == null ? null : n.bien().cantidad();
        if (recibida == null || cantidad == null || cantidad <= 0) return 0;
        return Math.min(100, Math.round(recibida / cantidad * 100));
    }
}

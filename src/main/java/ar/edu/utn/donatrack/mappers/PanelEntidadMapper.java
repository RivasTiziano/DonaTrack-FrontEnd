package ar.edu.utn.donatrack.mappers;

import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.DonacionesDetalles;
import org.springframework.stereotype.Component;
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

    /** Asignación en camino o lista para entregar. El camión no se informa: la API no lo permite por dueño. */
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

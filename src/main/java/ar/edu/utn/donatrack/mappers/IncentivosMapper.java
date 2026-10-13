package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.IncentivosDtos;
import ar.edu.utn.donatrack.dto.ApiDtos;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class IncentivosMapper {
    /** Ranking mensual con nombres. La posición es el orden que devuelve la API. */
    public List<IncentivosDtos.FilaRanking> ranking(List<ApiDtos.Ranking> ranking, Map<Long,String> nombres) {
        if (ranking == null) return List.of();
        List<IncentivosDtos.FilaRanking> filas = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            var r = ranking.get(i);
            filas.add(new IncentivosDtos.FilaRanking(i + 1, r.idDonante(), nombre(nombres, r.idDonante()), r.misionesResueltas()));
        }
        return filas;
    }

    /** Historial de rankings con nombres en cada posición. */
    public List<IncentivosDtos.FilaHistorial> historial(List<IncentivosDtos.RankingHistorico> historial, Map<Long,String> nombres) {
        if (historial == null) return List.of();
        List<IncentivosDtos.FilaHistorial> filas = new ArrayList<>();
        for (var h : historial) {
            List<IncentivosDtos.FilaRanking> posiciones = new ArrayList<>();
            if (h.posiciones() != null) for (var p : h.posiciones()) {
                Long id = p.donante() != null ? p.donante().idReferencia() : null;
                posiciones.add(new IncentivosDtos.FilaRanking(p.posicion(), id, nombre(nombres, id), p.misionesCompletadas()));
            }
            filas.add(new IncentivosDtos.FilaHistorial(h.id(), h.periodo(), posiciones));
        }
        return filas;
    }

    private static String nombre(Map<Long,String> nombres, Long id) {
        if (id == null) return "Sin referencia";
        return nombres.getOrDefault(id, "Donante #" + id);
    }

    public IncentivosDtos.MisionRequest mision(String tipo,Integer objetivo) { return new IncentivosDtos.MisionRequest(tipo,objetivo); }
    public IncentivosDtos.CategoriaRequest categoria(String nombre,Long siguiente,String tipo,Integer objetivo) {
        return new IncentivosDtos.CategoriaRequest(nombre,siguiente,mision(tipo,objetivo));
    }
}

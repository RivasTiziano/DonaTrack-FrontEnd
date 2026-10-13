package ar.edu.utn.donatrack.mappers;
import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.models.PanelAdminDatos;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
@Component
public class PanelAdminMapper {
 public Map<String,Object> mapear(PanelAdminDatos d,LocalDate hoy) {
  Map<String,Object> m=new LinkedHashMap<>();
  m.put("hoy",hoy);m.put("panelAvisos",d.avisos());
  m.put("kpiDeposito",cantidad(d.donaciones(),x->"EN_DEPOSITO".equals(x.estado())));
  m.put("kpiVencer",cantidad(d.donaciones(),x->porVencer(x,hoy)));
  m.put("kpiNecesidades",cantidad(d.necesidades(),x->!"SATISFECHA".equals(x.estado())));
  m.put("extraordinarias",cantidad(d.necesidades(),x->!"SATISFECHA".equals(x.estado()) && "EXTRAORDINARIA".equals(x.tipoNecesidad())));
  m.put("kpiFlota",cantidad(d.camiones(),x->true));
  m.put("disponibles",cantidad(d.camiones(),x->"DISPONIBLE".equals(x.estado())));
  m.put("enRuta",cantidad(d.camiones(),x->"EN_RUTA".equals(x.estado())));
  m.put("fueraServicio",cantidad(d.camiones(),x->"FUERA_DE_SERVICIO".equals(x.estado())));
  m.put("kpiTraslado",cantidad(d.entregas(),x->"EN_TRASLADO".equals(x.estado())));
  m.put("kpiNoRecibidas",cantidad(d.entregas(),x->"NO_RECIBIDA".equals(x.estado())));
  m.put("noRecibidas",d.entregas()==null?null:d.entregas().stream().filter(x->"NO_RECIBIDA".equals(x.estado()))
   .sorted(Comparator.comparing(ApiDtos.Entrega::id,Comparator.nullsLast(Comparator.reverseOrder()))).limit(5).toList());
  m.put("porVencer",d.donaciones()==null?null:d.donaciones().stream().filter(x->porVencer(x,hoy))
   .sorted(Comparator.comparing(x->x.bien().fechaDeVencimiento())).limit(5).toList());
  Set<Long> sugeridas=new HashSet<>();
  if(d.sugerencias()!=null) d.sugerencias().forEach(x->sugeridas.add(x.donacionId()));
  m.put("sinSugerencia",d.donaciones()==null || d.sugerencias()==null?null:d.donaciones().stream()
   .filter(x->"EN_DEPOSITO".equals(x.estado()) && !sugeridas.contains(x.id())).limit(5).toList());
  m.put("rutasHoy",d.rutas()==null?null:d.rutas().stream().filter(x->hoy.equals(x.fecha())).toList());
  m.put("panelUbicaciones",d.ubicaciones());
  m.put("podio",d.ranking()==null?null:d.ranking().stream().limit(3).toList());
  return m;
 }
 private boolean porVencer(ApiDtos.Donacion d,LocalDate hoy) {
  return "EN_DEPOSITO".equals(d.estado()) && d.bien()!=null && d.bien().fechaDeVencimiento()!=null
   && !d.bien().fechaDeVencimiento().isAfter(hoy.plusDays(7));
 }
 private <T> Long cantidad(List<T> values,Predicate<T> filter) { return values==null?null:values.stream().filter(filter).count(); }
}

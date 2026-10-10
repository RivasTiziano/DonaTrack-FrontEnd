package ar.edu.utn.donatrack.models;
import ar.edu.utn.donatrack.dto.ApiDtos;
import ar.edu.utn.donatrack.dto.donante.*;
import java.util.List;
/** Datos obtenidos por el servicio antes de adaptarlos a una vista. */
public record WebDatos(List<ApiDtos.Donacion> donaciones,ApiDtos.Donacion donacion,
        List<DonanteHumanoResponse> humanos,List<DonanteJuridicoResponse> juridicos,
        List<ApiDtos.Beneficiario> beneficiarios,List<ApiDtos.Necesidad> necesidades,
        List<ApiDtos.Ranking> ranking,List<ApiDtos.Entrega> entregas) {}

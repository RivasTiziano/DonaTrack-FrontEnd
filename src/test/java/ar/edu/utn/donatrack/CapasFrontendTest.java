package ar.edu.utn.donatrack;

import ar.edu.utn.donatrack.controllers.*;
import ar.edu.utn.donatrack.dto.*;
import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.*;
import ar.edu.utn.donatrack.exceptions.FormularioInvalidoException;
import ar.edu.utn.donatrack.forms.*;
import ar.edu.utn.donatrack.mappers.*;
import ar.edu.utn.donatrack.services.*;
import ar.edu.utn.donatrack.validators.*;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Sin HTTP, Gmail ni bases reales: prueba los límites de cada capa. */
class CapasFrontendTest {
    DonacionesApiService api=mock(DonacionesApiService.class);
    DonacionesMapper mapper=new DonacionesMapper();
    DonacionesValidator validator=new DonacionesValidator();
    DonacionesService service=new DonacionesService(api,mapper,validator,new WebMapper(),new WebValidator(),new DonanteDashboardMapper(),new BeneficiarioDashboardMapper());

    DonanteForm form() {
        var f=mock(DonanteForm.class);
        when(f.nombre()).thenReturn("Ana"); when(f.apellido()).thenReturn("Real");
        when(f.fechaNacimiento()).thenReturn(LocalDate.of(1990,1,1));
        when(f.numeroDocumento()).thenReturn("12345678"); when(f.genero()).thenReturn("OTRO");
        when(f.calle()).thenReturn("Calle"); when(f.numero()).thenReturn("123");
        when(f.ciudad()).thenReturn("CABA"); when(f.provincia()).thenReturn("BA");
        when(f.email()).thenReturn("ana@example.com");
        when(f.razonSocial()).thenReturn("Empresa Real"); when(f.tipoEntidad()).thenReturn("EMPRESA");
        when(f.rubro()).thenReturn("Alimentos");
        return f;
    }

    @Test void humanoSeMapeaYDelegaSinCrearUnaCuenta() {
        service.crear("humanos",form());
        verify(api).crearHumano(argThat(r -> "Ana".equals(r.nombre())
                && r.fechaNacimiento().equals(LocalDate.of(1990,1,1))
                && r.mediosDeContacto().size()==1));
        verifyNoMoreInteractions(api);
    }

    @Test void juridicoSinRepresentantesNoSeCompletaConDatosFicticios() {
        assertThrows(FormularioInvalidoException.class,()->service.crear("juridicos",form()));
        verifyNoInteractions(api);
    }

    @Test void representanteSinFechaSeRechazaEnLugarDeInventarla() {
        var f=form();
        when(f.representanteNombre()).thenReturn("Referente");
        when(f.representanteApellido()).thenReturn("Real");
        when(f.representanteDocumento()).thenReturn("87654321");
        when(f.representanteGenero()).thenReturn("OTRO");
        assertThrows(FormularioInvalidoException.class,()->service.crear("juridicos",f));
        verifyNoInteractions(api);
    }

    @Test void representanteValidoConservaLosDatosReales() {
        var f=form();
        when(f.representanteNombre()).thenReturn("Referente");
        when(f.representanteApellido()).thenReturn("Real");
        when(f.representanteDocumento()).thenReturn("87654321");
        when(f.representanteNacimiento()).thenReturn(LocalDate.of(1978,2,3));
        when(f.representanteGenero()).thenReturn("OTRO");
        service.crear("juridicos",f);
        verify(api).crearJuridico(argThat(r -> r.representantesJuridicos().size()==1
                && r.representantesJuridicos().get(0).fechaNacimiento().equals(LocalDate.of(1978,2,3))));
    }

    @Test void contactoPredeterminadoAjenoSeRechazaAntesDelHttp() {
        var f=form();
        when(f.contactoPredeterminadoTipo()).thenReturn("EMAIL");
        when(f.contactoPredeterminadoValor()).thenReturn("otra@example.com");
        assertThrows(FormularioInvalidoException.class,()->service.crear("humanos",f));
        verifyNoInteractions(api);
    }

    @Test void contactoAdicionalParcialNoSeDescartaSilenciosamente() {
        var f=form();
        when(f.contactoTipo()).thenReturn(List.of("EMAIL"));
        when(f.contactoValor()).thenReturn(List.of(""));
        assertThrows(FormularioInvalidoException.class,()->service.crear("humanos",f));
        verifyNoInteractions(api);
    }

    @Test void representanteAdicionalParcialSeRechaza() {
        var f=form();
        when(f.repNombre()).thenReturn(List.of("Laura"));
        when(f.repDocumento()).thenReturn(List.of("123"));
        assertThrows(FormularioInvalidoException.class,()->service.crear("juridicos",f));
        verifyNoInteractions(api);
    }

    @Test void mapperNoInventaRepresentantesNiFechas() {
        assertTrue(mapper.representantes(form()).isEmpty());
        var f=form(); when(f.representanteNombre()).thenReturn("Real");
        assertNull(mapper.representantes(f).get(0).fechaNacimiento());
    }

    @Test void cantidadAsignadaInvalidaNuncaLlegaALaApi() {
        assertThrows(FormularioInvalidoException.class,()->service.asignar(1L,2L,Float.NaN));
        assertThrows(FormularioInvalidoException.class,()->service.asignar(1L,2L,0f));
        verifyNoInteractions(api);
    }

    @Test void bienesAdicionalesSinCantidadNoRecibenCantidadFicticia() {
        var first=new BienForm(null,null,null,null,null,null,null,null);
        var extras=new BienesAdicionalesForm(List.of("Arroz"),null,null,List.of(1L),
                List.of("KILOGRAMO"),List.of("PERECEDERO"),null,null);
        assertThrows(FormularioInvalidoException.class,()->service.donar(1L,"Campaña",first,extras));
        verifyNoInteractions(api);
    }

    @Test void validatorEscribeErroresDelFormulario() {
        var f=form(); when(f.email()).thenReturn("incorrecto");
        var errors=new BeanPropertyBindingResult(f,"form");
        validator.validar("humanos",f,errors);
        assertTrue(errors.hasFieldErrors("email"));
    }

    @Test void logisticaValidaChoferYNoConfundeAceptacionConRutasTerminadas() {
        var api=mock(LogisticaApiService.class);
        var service=new LogisticaService(api,new LogisticaMapper(),new LogisticaValidator(),mock(DonacionesService.class),new DonanteDashboardMapper(),new BeneficiarioDashboardMapper());
        assertThrows(FormularioInvalidoException.class,()->service.iniciarRuta(1L,""));
        verifyNoInteractions(api);
        var fecha=LocalDate.of(2026,10,10);
        var accepted=new LogisticaDetalles.Planificacion(2,1,List.of("request"));
        when(api.planificar(fecha)).thenReturn(accepted);
        assertSame(accepted,service.planificar(fecha));
    }

    @Test void incentivosValidaAntesDeConstruirYSolicitar() {
        var api=mock(IncentivosApiService.class);
        var service=new IncentivosService(api,new IncentivosMapper(),new IncentivosValidator(),mock(DonacionesService.class),new DonanteDashboardMapper());
        assertThrows(FormularioInvalidoException.class,()->service.agregarMision(1L,"RACHA",0));
        assertThrows(FormularioInvalidoException.class,()->service.crearCategoria("Real",null,"INVENTADA",2));
        verifyNoInteractions(api);
        service.crearCategoria("Real",null,"RACHA",3);
        verify(api).crearCategoria(new IncentivosDtos.CategoriaRequest("Real",null,new IncentivosDtos.MisionRequest("RACHA",3)));
    }

    @Test void notificacionesMapeaCanalYExplicaResultadoPendiente() {
        var api=mock(NotificacionesApiService.class);
        var service=new NotificacionesService(api,new NotificacionesMapper(),new NotificacionesValidator());
        when(api.enviar(any())).thenReturn(new Respuesta("success",Estado.PENDIENTE,null));
        var form=new NotificacionForm(Medio.EMAIL,"Ana","ana@example.com",null,"Asunto","Cuerpo");
        var result=service.enviar(form);
        assertTrue(result.exitoso()); assertTrue(result.mensaje().contains("no es confirmación"));
        verify(api).enviar(argThat(r -> r.destinatario().telefono()==null && "ana@example.com".equals(r.destinatario().email())));
    }

    @Test void notificacionRechazadaNoSeInformaComoExito() {
        var api=mock(NotificacionesApiService.class);
        var service=new NotificacionesService(api,new NotificacionesMapper(),new NotificacionesValidator());
        when(api.enviar(any())).thenReturn(new Respuesta("error",Estado.FALLIDA,"Rechazado"));
        var result=service.enviar(new NotificacionForm(Medio.EMAIL,"Ana","ana@example.com",null,"Asunto","Cuerpo"));
        assertFalse(result.exitoso()); assertEquals("Rechazado",result.mensaje());
    }

    @Test void capasNoSeMezclanPorDependencias() {
        for(var type:List.of(DonacionesController.class,LogisticaController.class,IncentivosController.class,
                NotificacionesController.class,WebController.class,AutenticacionController.class,AdminController.class))
            for(var field:type.getDeclaredFields())
                assertFalse(field.getType().getSimpleName().endsWith("ApiService"),type.getSimpleName());
        for(var type:List.of(DonacionesService.class,LogisticaService.class,IncentivosService.class,NotificacionesService.class,WebService.class,AutenticacionService.class))
            for(var method:type.getDeclaredMethods())
                for(var parameter:method.getParameterTypes())
                    assertFalse(parameter.getName().startsWith("org.springframework.ui.")
                            || parameter.getName().startsWith("org.springframework.web.servlet.")
                            || parameter.getName().startsWith("jakarta.servlet."),type.getSimpleName());
        for(var type:List.of(DonacionesMapper.class,LogisticaMapper.class,IncentivosMapper.class,NotificacionesMapper.class,WebMapper.class,AutenticacionMapper.class,DonanteDashboardMapper.class,BeneficiarioDashboardMapper.class))
            for(var field:type.getDeclaredFields())
                assertFalse(field.getType().getPackageName().startsWith("ar.edu.utn.donatrack.services"));
    }
}

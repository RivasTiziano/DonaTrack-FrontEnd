package ar.edu.utn.donatrack.services;

import ar.edu.utn.donatrack.dto.RegistroDtos;
import ar.edu.utn.donatrack.exceptions.RegistroRechazadoException;
import ar.edu.utn.donatrack.exceptions.RegistroRechazadoException.Motivo;
import ar.edu.utn.donatrack.forms.RegistroForm;
import ar.edu.utn.donatrack.mappers.RegistroMapper;
import ar.edu.utn.donatrack.services.internal.ApiErrorMessages;
import ar.edu.utn.donatrack.validators.RegistroValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import java.util.function.Supplier;

/**
 * Registro público de personas donantes, organizaciones y entidades beneficiarias.
 * Valida, arma el contrato de Donaciones y traduce los errores a mensajes para el usuario.
 * El registro no inicia sesión: la persona entra después con el login.
 */
@Service
public class RegistroService {
    static final String CUENTA_EXISTENTE = "Ya existe una cuenta con ese email";
    static final String AYUDA_DEPOSITO = "Si te registraron en el depósito, pedí tu acceso a la administración.";

    private final RegistroValidator validator;
    private final RegistroMapper mapper;
    private final RegistroApiService api;
    private final ApiErrorMessages errors;

    public RegistroService(RegistroValidator validator, RegistroMapper mapper, RegistroApiService api, ApiErrorMessages errors) {
        this.validator = validator;
        this.mapper = mapper;
        this.api = api;
        this.errors = errors;
    }

    public RegistroDtos.Registrado registrarHumano(RegistroForm form, Errors binding) {
        validator.validarHumano(form, binding);
        return ejecutar(binding, () -> api.registrarHumano(mapper.humano(form)));
    }

    public RegistroDtos.Registrado registrarJuridico(RegistroForm form, Errors binding) {
        validator.validarJuridico(form, binding);
        return ejecutar(binding, () -> api.registrarJuridico(mapper.juridico(form)));
    }

    public RegistroDtos.Registrado registrarEntidad(RegistroForm form, Errors binding) {
        validator.validarEntidad(form, binding);
        return ejecutar(binding, () -> api.registrarEntidad(mapper.entidad(form)));
    }

    // Si la validación del front falla, no se llama a la API: el formulario muestra los errores junto a cada campo.
    private RegistroDtos.Registrado ejecutar(Errors binding, Supplier<RegistroDtos.Registrado> llamada) {
        if (binding.hasErrors())
            throw new RegistroRechazadoException(Motivo.DATOS_INVALIDOS, "Revisá los datos marcados en el formulario.");
        try {
            return llamada.get();
        } catch (HttpClientErrorException.Conflict exception) {
            if (CUENTA_EXISTENTE.equals(errors.describe(exception)))
                throw new RegistroRechazadoException(Motivo.CUENTA_EXISTENTE, CUENTA_EXISTENTE);
            throw new RegistroRechazadoException(Motivo.DATOS_DUPLICADOS,
                    "Ya existe un registro con esos datos (email o documento). " + AYUDA_DEPOSITO);
        } catch (HttpClientErrorException exception) {
            throw new RegistroRechazadoException(Motivo.DATOS_INVALIDOS, errors.describe(exception));
        } catch (RestClientException exception) {
            // 503, servicio caído o respuesta vacía: no se confirmó la cuenta y no se reintenta solo.
            throw new RegistroRechazadoException(Motivo.NO_DISPONIBLE, "No se pudo crear la cuenta. Probá de nuevo más tarde.");
        }
    }
}

package ar.edu.utn.donatrack.forms;

import ar.edu.utn.donatrack.dto.notificacion.NotificacionDtos.Medio;

public record NotificacionForm(Medio medioContacto, String nombre, String email,
                               String telefono, String asunto, String cuerpo) {}

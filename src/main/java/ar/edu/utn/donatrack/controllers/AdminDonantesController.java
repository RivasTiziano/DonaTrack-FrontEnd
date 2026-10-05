package ar.edu.utn.donatrack.controllers;

import ar.edu.utn.donatrack.clients.DonantesApiClient;
import ar.edu.utn.donatrack.dto.donante.MedioDeContactoDto;
import ar.edu.utn.donatrack.models.DonanteFila;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class AdminDonantesController {

    private final DonantesApiClient donantesApiClient;

    public AdminDonantesController(DonantesApiClient donantesApiClient) {
        this.donantesApiClient = donantesApiClient;
    }

    @GetMapping("/admin/dashboard/donantes")
    public String listar(Model model) {
        // Identidad visual provisional: no representa autenticación real.
        model.addAttribute("user", Map.of(
                "name", "Administrador",
                "email", "",
                "role", "Administrador de Depósito"
        ));

        List<DonanteFila> filas = new ArrayList<>();

        try {
            var humanos = donantesApiClient.listarHumanos();
            var juridicos = donantesApiClient.listarJuridicos();

            humanos.forEach(donante -> filas.add(new DonanteFila(
                    donante.id(),
                    "Humana",
                    nombreCompleto(donante.nombre(), donante.apellido()),
                    donante.tipoDocumento(),
                    donante.numeroDocumento(),
                    contacto(donante.mediosDeContacto(), "EMAIL"),
                    contacto(donante.mediosDeContacto(), "SMS", "WHATSAPP"),
                    donante.estadoRegistro()
            )));

            juridicos.forEach(donante -> filas.add(new DonanteFila(
                    donante.id(),
                    "Jurídica",
                    donante.razonSocial(),
                    "CUIT",
                    donante.numeroDocumento(),
                    contacto(donante.mediosDeContacto(), "EMAIL"),
                    contacto(donante.mediosDeContacto(), "SMS", "WHATSAPP"),
                    donante.estadoRegistro()
            )));
        } catch (RestClientException exception) {
            filas.clear();
            model.addAttribute(
                    "errorDonantes",
                    "No se pudo obtener el listado de donantes. "
                            + "Verificá que el servicio de Donaciones esté disponible."
            );
        }

        model.addAttribute("donorsList", filas);
        return "dashboard-admin-donantes";
    }

    private String nombreCompleto(String nombre, String apellido) {
        return Stream.of(nombre, apellido)
                .filter(Objects::nonNull)
                .filter(valor -> !valor.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String contacto(
            List<MedioDeContactoDto> contactos,
            String... tipos
    ) {
        if (contactos == null) {
            return "—";
        }

        return contactos.stream()
                .filter(contacto ->
                        List.of(tipos).contains(contacto.tipoMedioContacto()))
                .map(MedioDeContactoDto::formaContacto)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("—");
    }
}

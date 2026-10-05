package ar.edu.utn.donatrack.models;

public record DonanteFila(
        Long id,
        String type,
        String name,
        String docType,
        String doc,
        String email,
        String phone,
        String estadoRegistro
) {}
package com.bibliotech.model;

import java.time.LocalDate;

public record Prestamo(
        String id,
        Socio socio,
        Recurso recurso,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento
) {
}
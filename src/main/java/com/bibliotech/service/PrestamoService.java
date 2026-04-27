package com.bibliotech.service;

import com.bibliotech.exception.LibroNoDisponibleException;
import com.bibliotech.model.Prestamo;
import com.bibliotech.model.Recurso;
import com.bibliotech.model.Socio;
import com.bibliotech.repository.Repository;

import java.time.LocalDate;
import java.util.UUID;

public class PrestamoService {

    // Inyección de dependencias por constructor
    private final Repository<Recurso, String> recursoRepository;
    private final Repository<Prestamo, String> prestamoRepository;

    public PrestamoService(Repository<Recurso, String> recursoRepository, Repository<Prestamo, String> prestamoRepository) {
        this.recursoRepository = recursoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public Prestamo registrarPrestamo(Socio socio, String isbn) throws LibroNoDisponibleException {
        // 1. Buscar si el recurso existe
        Recurso recurso = recursoRepository.buscarPorId(isbn)
                .orElseThrow(() -> new LibroNoDisponibleException("El recurso con ISBN " + isbn + " no existe."));

        // 2. Verificar disponibilidad
        boolean estaPrestado = prestamoRepository.buscarTodos().stream()
                .anyMatch(p -> p.recurso().isbn().equals(isbn));

        if (estaPrestado) {
            throw new LibroNoDisponibleException("El recurso con ISBN " + isbn + " ya se encuentra prestado en este momento.");
        }

        // 3. Verificar límite del socio
        long librosActuales = prestamoRepository.buscarTodos().stream()
                .filter(p -> p.socio().dni().equals(socio.dni()))
                .count();

        if (librosActuales >= socio.obtenerTopePrestamos()) {
            throw new LibroNoDisponibleException("El socio " + socio.nombre() + " alcanzó su límite de " + socio.obtenerTopePrestamos() + " préstamos.");
        }

        // 4. Registrar y guardar
        Prestamo nuevoPrestamo = new Prestamo(
                UUID.randomUUID().toString(),
                socio,
                recurso,
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        prestamoRepository.guardar(nuevoPrestamo);
        return nuevoPrestamo;
    }
}
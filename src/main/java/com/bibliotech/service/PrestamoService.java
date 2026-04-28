package com.bibliotech.service;

import com.bibliotech.exception.BibliotecaException;
import com.bibliotech.exception.LibroNoDisponibleException;
import com.bibliotech.model.Prestamo;
import com.bibliotech.model.Recurso;
import com.bibliotech.model.Socio;
import com.bibliotech.repository.Repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrestamoService {

    private final Repository<Recurso, String> recursoRepository;
    private final Repository<Prestamo, String> prestamoRepository;

    // Mapa para registrar sanciones: DNI -> Fecha en que termina la sanción
    private final Map<String, LocalDate> sanciones = new HashMap<>();

    public PrestamoService(Repository<Recurso, String> recursoRepository, Repository<Prestamo, String> prestamoRepository) {
        this.recursoRepository = recursoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public Prestamo registrarPrestamo(Socio socio, String isbn) throws BibliotecaException {
        // 0. Verificar si el socio está sancionado
        if (sanciones.containsKey(socio.dni())) {
            LocalDate finSancion = sanciones.get(socio.dni());
            if (LocalDate.now().isBefore(finSancion) || LocalDate.now().isEqual(finSancion)) {
                throw new BibliotecaException("El socio " + socio.nombre() + " se encuentra suspendido hasta el " + finSancion);
            } else {
                // Si ya pasó la fecha, le levantamos la sanción
                sanciones.remove(socio.dni());
            }
        }

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

    public long gestionarDevolucion(String isbn) throws LibroNoDisponibleException {
        // 1. Buscar el préstamo activo para ese recurso
        Prestamo prestamoActivo = prestamoRepository.buscarTodos().stream()
                .filter(p -> p.recurso().isbn().equals(isbn))
                .findFirst()
                .orElseThrow(() -> new LibroNoDisponibleException("No se encontró un préstamo activo para el ISBN: " + isbn));

        // 2. Calcular días de retraso
        LocalDate hoy = LocalDate.now();
        long diasRetraso = 0;

        if (hoy.isAfter(prestamoActivo.fechaVencimiento())) {
            diasRetraso = ChronoUnit.DAYS.between(prestamoActivo.fechaVencimiento(), hoy);

            // Aplicar sanción: 2 días de bloqueo por cada día de retraso
            LocalDate finSancion = hoy.plusDays(diasRetraso * 2);
            sanciones.put(prestamoActivo.socio().dni(), finSancion);
        }

        // 3. Finalizar el préstamo de forma polimórfica usando la interfaz
        prestamoRepository.eliminar(prestamoActivo.id());

        return diasRetraso;
    }
}
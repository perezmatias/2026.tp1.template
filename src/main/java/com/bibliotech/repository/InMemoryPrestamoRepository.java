package com.bibliotech.repository;

import com.bibliotech.model.Prestamo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryPrestamoRepository implements Repository<Prestamo, String> {
    private final List<Prestamo> prestamos = new ArrayList<>();

    @Override
    public void guardar(Prestamo entidad) {
        prestamos.removeIf(p -> p.id().equals(entidad.id()));
        prestamos.add(entidad);
    }

    @Override
    public Optional<Prestamo> buscarPorId(String id) {
        return prestamos.stream()
                .filter(p -> p.id().equals(id))
                .findFirst();
    }

    @Override
    public List<Prestamo> buscarTodos() {
        return new ArrayList<>(prestamos);
    }

    @Override
    public void eliminar(String id) {
        prestamos.removeIf(p -> p.id().equals(id));
    }
}
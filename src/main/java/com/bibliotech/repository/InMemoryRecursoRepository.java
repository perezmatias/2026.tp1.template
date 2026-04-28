package com.bibliotech.repository;

import com.bibliotech.model.Recurso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryRecursoRepository implements Repository<Recurso, String> {
    private final List<Recurso> recursos = new ArrayList<>();

    @Override
    public void guardar(Recurso entidad) {
        recursos.removeIf(r -> r.isbn().equals(entidad.isbn()));
        recursos.add(entidad);
    }

    @Override
    public Optional<Recurso> buscarPorId(String id) {
        return recursos.stream()
                .filter(r -> r.isbn().equals(id))
                .findFirst();
    }

    @Override
    public List<Recurso> buscarTodos() {
        return new ArrayList<>(recursos);
    }

    @Override
    public void eliminar(String id) {
        recursos.removeIf(r -> r.isbn().equals(id));
    }

    public List<Recurso> buscarAvanzada(String termino) {
        String terminoLower = termino.toLowerCase();

        return recursos.stream()
                .filter(r -> r.titulo().toLowerCase().contains(terminoLower) ||
                        r.autor().toLowerCase().contains(terminoLower) ||
                        r.categoria().toLowerCase().contains(terminoLower))
                .collect(Collectors.toList());
    }
}
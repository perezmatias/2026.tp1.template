package com.bibliotech.repository;

import com.bibliotech.model.Recurso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryRecursoRepository implements Repository<Recurso, String> {
    // colección para almacenar los datos en memoria
    private final List<Recurso> recursos = new ArrayList<>();

    @Override
    public void guardar(Recurso entidad) {
        // Removemos si ya existe para simular un "update", y luego lo agregamos
        recursos.removeIf(r -> r.isbn().equals(entidad.isbn()));
        recursos.add(entidad);
    }

    @Override
    public Optional<Recurso> buscarPorId(String id) {
        // Streams y findFirst que retorna un Optional directamente
        return recursos.stream()
                .filter(r -> r.isbn().equals(id))
                .findFirst();
    }

    @Override
    public List<Recurso> buscarTodos() {
        // Devolvemos una copia de la lista para proteger la encapsulación
        return new ArrayList<>(recursos);
    }

    // Búsqueda Avanzada
    public List<Recurso> buscarAvanzada(String termino) {
        String terminoLower = termino.toLowerCase();

        return recursos.stream()
                .filter(r -> r.titulo().toLowerCase().contains(terminoLower) ||
                        r.autor().toLowerCase().contains(terminoLower) ||
                        r.categoria().toLowerCase().contains(terminoLower))
                .collect(Collectors.toList());
    }
}
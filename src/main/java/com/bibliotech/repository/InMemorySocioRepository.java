package com.bibliotech.repository;

import com.bibliotech.model.Socio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemorySocioRepository implements Repository<Socio, String> {
    private final List<Socio> socios = new ArrayList<>();

    @Override
    public void guardar(Socio entidad) {
        socios.removeIf(s -> s.dni().equals(entidad.dni()));
        socios.add(entidad);
    }

    @Override
    public Optional<Socio> buscarPorId(String dni) {
        return socios.stream().filter(s -> s.dni().equals(dni)).findFirst();
    }

    @Override
    public List<Socio> buscarTodos() {
        return new ArrayList<>(socios);
    }

    @Override
    public void eliminar(String dni) {
        socios.removeIf(s -> s.dni().equals(dni));
    }
}
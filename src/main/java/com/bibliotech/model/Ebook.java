package com.bibliotech.model;

public record Ebook(
        String isbn,
        String titulo,
        String autor,
        int anio,
        String categoria,
        double tamanioMb,
        String formato
) implements Recurso {}